package codes.monkey.batchstats

import codes.monkey.batchstats.statemachine.JobStateMachine
import com.codahale.metrics.MetricRegistry
import com.codahale.metrics.ScheduledReporter
import org.hamcrest.Description
import org.hamcrest.Matcher
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

import static codes.monkey.batchstats.StatsEventsGrabber.lastEvent
import static org.hamcrest.Matchers.allOf
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
                lastEvent('job.step1.chunk.read', hasEntry('count', String.valueOf(readCount))),
                lastEvent('job.step1.chunk.process', hasEntry('count', String.valueOf(processCount))),
                lastEvent('job.step1.chunk.write', hasEntry('count', String.valueOf(writeCount)))
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
//        reader.list = (1..2).collect()
        this."$errorOn".transform = {
            if (it instanceof List) {
                if (it.contains(errorOnItem))
                    throw new RuntimeException("fake writer error")
                return it
            }
            if (it == errorOnItem)
                throw new RuntimeException("fake error")
            it
        }

        when:
        JobExecution jobExecution = jobLauncher.run(job, new JobParameters())

        then:
        jobExecution.status == BatchStatus.COMPLETED
        expect statsEventsGrabber, allOf(
                lastEvent('job.step1.chunk.read', hasEntry('count', String.valueOf(readCount))),
                lastEvent('job.step1.chunk.process', hasEntry('count', String.valueOf(processCount))),
                lastEvent('job.step1.chunk.write', hasEntry('count', String.valueOf(writeCount))),
                lastEvent("job.step1.chunk.$errorEvent" as String, hasEntry('count', '1')),
                expectations
        )

        where:
        errorOn                     | errorOnItem | errorEvent      | readCount | processCount | writeCount | expectations
        'interceptingItemReader'    | 1           | 'read.error'    | 4         | 4            | 1          | noop()
        'interceptingItemProcessor' | 2           | 'process.error' | 5         | 4            | 1          | noop()
        'interceptingItemWriter'    | 2           | 'write.error'   | 5         | 5            | 0          | writeError()

        /*
        * Need state machine to deal with write errors, once chunks are reduced to lists of 1 after a write error
        * only afterWrite*/
    }

    static Matcher<StatsEventsGrabber> noop() {
        return new TypeSafeMatcher<StatsEventsGrabber>() {
            @Override
            protected boolean matchesSafely(StatsEventsGrabber item) {
                return true;
            }

            @Override
            void describeTo(Description description) {
            }
        }
    }

    static Matcher<StatsEventsGrabber> writeError() {
        allOf(
                lastEvent('job.step1.chunk.write.error.reprocess', hasEntry('count', '1'))
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
//            def statsListener = new ThreadDebugListener(new StatsListener(metricRegistry))
//            def statsListener = new ThreadDebugListener()
            def statsListener = new ThreadDebugListener(JobStateMachine.idle(
                    new StatsListener(metricRegistry, { reporter.report() }))
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
