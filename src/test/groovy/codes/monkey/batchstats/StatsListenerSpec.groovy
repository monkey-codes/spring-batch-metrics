package codes.monkey.batchstats

import codes.monkey.batchstats.statemachine.JobStateMachine
import com.codahale.metrics.MetricRegistry
import com.codahale.metrics.ScheduledReporter
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import org.hamcrest.TypeSafeMatcher
import org.springframework.batch.core.BatchStatus
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.JobParameters
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.batch.core.listener.JobListenerFactoryBean
import org.springframework.batch.item.function.FunctionItemProcessor
import org.springframework.batch.item.support.ListItemWriter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

import static codes.monkey.batchstats.StatsEventsGrabber.combineLastEvents
import static codes.monkey.batchstats.StatsEventsGrabber.lastEvent
import static org.hamcrest.Matchers.allOf
import static org.hamcrest.Matchers.greaterThanOrEqualTo
import static org.hamcrest.Matchers.hasEntry
import static spock.util.matcher.HamcrestSupport.expect

/**
 * @author Johan Zietsman (jzietsman@thoughtworks.com.au).
 */
@ContextConfiguration(classes = StatsListenerSpec.TestConfig)
class StatsListenerSpec extends Specification {

    @Autowired
    Job job

    @Autowired
    JobLauncher jobLauncher

    @Autowired
    MutableListItemReader reader

    @Autowired
    InterceptingItemReader interceptingItemReader

    @Autowired
    InterceptingItemProcessor interceptingItemProcessor

    @Autowired
    InterceptingItemWriter interceptingItemWriter

    StatsEventsGrabber statsEventsGrabber


    def setup() {
        statsEventsGrabber = new StatsEventsGrabber()
    }

    def cleanup() {
        statsEventsGrabber.stop()
    }

    @DirtiesContext
    def "it should capture job stats"() {
        given:
        reader.list = range.collect()

        when:
        JobExecution jobExecution = jobLauncher.run(job, new JobParameters())

        then:
        jobExecution.status == BatchStatus.COMPLETED
        expect statsEventsGrabber, allOf(
                lastEvent('job.step1.chunk.read', hasCount(readCount)),
                lastEvent('job.step1.chunk.process', hasCount(processCount)),
                lastEvent('job.step1.chunk.write', hasCount(writeCount))
        )

        where:
        range    | readCount | processCount | writeCount | comment
        (1..2)   | 2         | 2            | 1          | "simple"
        (1..100) | 100       | 100          | 10         | "items divisible by chunk size"
        (1..108) | 108       | 108          | 11         | "items not divisible by chunk size"
    }

    @DirtiesContext
    def "it should deal with errors"() {
        given:

        reader.list = (1..5).collect()
        this."$errorOn".transform = exceptionOn(*errorItem)

        when:
        JobExecution jobExecution = jobLauncher.run(job, new JobParameters())

        then:
        jobExecution.status == BatchStatus.COMPLETED
        expect statsEventsGrabber, allOf(
                lastEvent('job.step1.chunk.read', hasCount(readCount)),
                combineLastEvents('job.step1.chunk.process,job.step1.chunk.reprocess.process', hasCount(processCount)),
                lastEvent('job.step1.chunk.write', hasCount(writeCount)),
                lastEvent("job.step1.chunk.${errorEvent}.error", hasCount(1)),
                expectations
        )

        where:
        errorOn                     | errorItem   | errorEvent | readCount | processCount                    | writeCount | expectations
        'interceptingItemReader'    | [1]           | 'read'     | 4         | 4                             | 1          | readError(1)
        'interceptingItemProcessor' | [4]           | 'process'  | 5         | greaterThanOrEqualTo(4) | 0          | processError(1)
        'interceptingItemWriter'    | [2]           | 'write'    | 5         | 10                            | 0          | writeError(1)

        /*
        * Need state machine to deal with write errors, once chunks are reduced to lists of 1 after a write error
        * only afterWrite*/
    }

    static Closure exceptionOn(...errorOnItem) {
        { item ->
            if (item instanceof List) {
                if (item.any {errorOnItem.contains(it)})
                    throw new RuntimeException("fake writer error")
                return item
            }
            if (errorOnItem.contains(item))
                throw new RuntimeException("fake error")
            item
        }
    }


    static Matcher<Map<? extends String, ? extends String>> hasCount(Matcher<Integer> valueMatcher) {
        def convertToInt = new TypeSafeMatcher<String>() {

            @Override
            protected boolean matchesSafely(String item) {
                valueMatcher.matches(Integer.valueOf(item))
            }

            @Override
            void describeTo(Description description) {
                valueMatcher.describeTo(description)
            }
        }
        hasEntry(Matchers.is('count'), convertToInt)
    }

    static Matcher<Map<? extends String, ? extends String>> hasCount(int count) {
        hasEntry('count', String.valueOf(count))
    }

    static Matcher<StatsEventsGrabber> writeError(count) {
        allOf(
                lastEvent('job.step1.chunk.write.error', hasCount(count))
        )
    }

    static Matcher<StatsEventsGrabber> readError(skipCount) {
        skipError('job.step1.chunk.read.skip', skipCount)
    }

    static Matcher<StatsEventsGrabber> processError(skipCount) {
        skipError('job.step1.chunk.reprocess.process.skip', skipCount)
    }

    private static Matcher<StatsEventsGrabber> skipError(String s, skipCount) {
        allOf(
                lastEvent(s, hasCount(skipCount))
        )
    }


    @TestConfiguration
    static class TestConfig extends ListenerTestConfig {

        @Bean
        MutableListItemReader mutableListItemReader() {
            new MutableListItemReader((1..100).collect())
        }

        @Bean
        InterceptingItemReader interceptingItemReader(MutableListItemReader reader) {
            new InterceptingItemReader(reader)
        }

        @Bean
        InterceptingItemProcessor interceptingItemProcessor() {
            new InterceptingItemProcessor(new FunctionItemProcessor({ it -> it * 2 }))
        }

        @Bean
        InterceptingItemWriter interceptingItemWriter() {
            new InterceptingItemWriter(new ListItemWriter())
        }

        @Bean
        Job job(InterceptingItemReader reader, InterceptingItemProcessor processor,
                InterceptingItemWriter writer, MetricRegistry metricRegistry, ScheduledReporter reporter) {
            def statsListener = JobStateMachine.idle(
                    new StatsListener(metricRegistry, { reporter.report() })
            )

            jobBuilderFactory
                    .get("job")
                    .listener(JobListenerFactoryBean.getListener(statsListener))
                    .start(
                    stepBuilderFactory.get("step1")
                            .chunk(10)
                            .faultTolerant().skip(Throwable).skipLimit(Integer.MAX_VALUE).listener(statsListener as Object)
                            .reader(reader)
                            .processor(processor)
                            .writer(writer)
                            .listener(statsListener as Object)
                            .build()
            ).build()
        }
    }
}
