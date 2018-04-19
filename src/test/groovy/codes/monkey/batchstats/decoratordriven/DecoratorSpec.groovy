package codes.monkey.batchstats.decoratordriven

import codes.monkey.batchstats.eventdriven.*
import com.codahale.metrics.MetricRegistry
import com.codahale.metrics.ScheduledReporter
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

import static codes.monkey.batchstats.StatsMatchers.exceptionOn
import static codes.monkey.batchstats.StatsMatchers.hasCount
import static codes.monkey.batchstats.eventdriven.StatsEventsGrabber.lastEvent
import static org.hamcrest.Matchers.allOf
import static spock.util.matcher.HamcrestSupport.expect

/**
 * @author Johan Zietsman (jzietsman@thoughtworks.com.au).
 */
@ContextConfiguration(classes = DecoratorSpec.TestConfig)
class DecoratorSpec extends Specification {

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
    ScheduledReporter scheduledReporter

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
        scheduledReporter.report()

        then:
        jobExecution.status == BatchStatus.COMPLETED
        expect statsEventsGrabber, allOf(
                lastEvent('job.step1.read', hasCount(readCount)),
                lastEvent('job.step1.process', hasCount(processCount)),
                lastEvent('job.step1.write', hasCount(writeCount))
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
        scheduledReporter.report()

        then:
        jobExecution.status == BatchStatus.COMPLETED
        expect statsEventsGrabber, allOf(
                lastEvent('job.step1.read', hasCount(readCount)),
                lastEvent('job.step1.process', hasCount(processCount)),
                lastEvent('job.step1.write', hasCount(writeCount)),
                lastEvent("job.step1.$errorEvent", hasCount(errorCount))
        )

        where:
        errorOn                     | errorItem | readCount | processCount | writeCount | errorEvent      | errorCount
        'interceptingItemReader'    | [1]       | 4         | 4            | 1          | 'read.error'    | 1
        // 1st item fails no chunk reprocess
        'interceptingItemProcessor' | [2]       | 5         | 4            | 1          | 'process.error' | 1
        //chunk reprocess, item 1 goes twice
        'interceptingItemProcessor' | [4]       | 5         | 5            | 1          | 'process.error' | 1
        /*on write error chunks of 1 is written and the items are reprocessed for each new chunk
          error count will also be 2 since it fires once on the initial chunk and once during reprocess
        */
        'interceptingItemWriter'    | [2]       | 5         | 10           | 4          | 'write.error'   | 2

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
                InterceptingItemWriter writer, MetricRegistry metricRegistry) {
            def factory = new DecoratorFactory(metricRegistry)
            jobBuilderFactory
                    .get("job")
                    .listener(JobListenerFactoryBean.getListener(factory))
                    .start(
                    stepBuilderFactory.get("step1")
                            .chunk(10)
                            .faultTolerant().skip(Throwable).skipLimit(Integer.MAX_VALUE)
                            .reader(factory.metricReader(reader))
                            .processor(factory.metricProcessor(processor))
                            .writer(factory.metricWriter(writer))
                            .listener(factory as Object)
                            .build()
            ).build()
        }
    }
}
