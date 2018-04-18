package codes.monkey.batchstats.eventdriven

import codes.monkey.batchstats.StatsMatchers
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
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.TaskExecutor
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

import static StatsEventsGrabber.combineLastEvents
import static StatsEventsGrabber.lastEvent
import static codes.monkey.batchstats.StatsMatchers.*
import static org.hamcrest.Matchers.allOf
import static org.hamcrest.Matchers.greaterThanOrEqualTo
import static spock.util.matcher.HamcrestSupport.expect

/**
 * @author Johan Zietsman (jzietsman@thoughtworks.com.au).
 */
@ContextConfiguration(classes = ListenerJobConfig)
class ParallelProcessingStatsListenerSpec extends Specification {

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
    def "it should capture job stats for parallel processing"() {
        given:
        reader.list = range.collect()

        when:
        JobExecution jobExecution = jobLauncher.run(job, new JobParameters())

        then:
        jobExecution.status == BatchStatus.COMPLETED
        expect statsEventsGrabber, allOf(
                lastEvent('job.step1.chunk.read', hasCount(readCount)),
                lastEvent('job.step1.chunk.process', hasCount(processCount)),
                /*
                * Because multiple threads are reading, you may end up with several chunks in the end that is not
                * filled to chunk size capacity, thus you could get more chunk writes than itemCount/chunkSize
                * */
                lastEvent('job.step1.chunk.write', hasCount(greaterThanOrEqualTo(writeCount)))
        )

        where:
        range    | readCount | processCount | writeCount | comment
        (1..100) | 100       | 100          | 10         | "items divisible by chunk size"
        (1..108) | 108       | 108          | 11         | "items not divisible by chunk size"
    }

    @DirtiesContext
    def "it should deal with errors"() {
        given:
        reader.list = (1..5).collect()
        this."$errorOn".transform = StatsMatchers.exceptionOn(*errorItem)

        when:
        JobExecution jobExecution = jobLauncher.run(job, new JobParameters())

        then:
        jobExecution.status == BatchStatus.COMPLETED
        expect statsEventsGrabber, allOf(
                lastEvent('job.step1.chunk.read', hasCount(readCount)),
                combineLastEvents('job.step1.chunk.process,job.step1.chunk.reprocess.process', hasCount(processCount)),
                lastEvent('job.step1.chunk.write', hasCount(greaterThanOrEqualTo(writeCount))),
                lastEvent("job.step1.chunk.${errorEvent}.error", hasCount(1))
        )

        where:
        errorOn                     | errorItem       | errorEvent | readCount | processCount                    | writeCount
        'interceptingItemReader'    | [1]             | 'read'     | 4         | 4                               | 1
        'interceptingItemProcessor' | [2]             | 'process'  | 5         | 4                               | 1
        'interceptingItemWriter'    | [2]             | 'write'    | 5         | greaterThanOrEqualTo(5)   | 0

        /*
        * Need state machine to deal with write errors, once chunks are reduced to lists of 1 after a write error
        * only afterWrite
        * Turns out on multi threaded processing, the same thread that detects the write error is not responsible
        * for the chunk reprocessing, these could be spread over several threads.  My next approach will be to change
        * how re processing is detected. onWriteError cannot be used anymore. The only solution I can think of is
        * to make the state machine states more finely grained and then detect a beforeChunk->beforeProcess and call
        * that reprocessing as suppose to a normal beforeChunk-beforeRead.
        * */

    }

    @Configuration
    static class ListenerJobConfig extends ListenerTestConfig {

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
        MutableListItemReader mutableListItemReader() {
            new MutableListItemReader((1..100).collect())
        }

        @Bean
        TaskExecutor taskExecutor() {
            new ThreadPoolTaskExecutor().with {
                corePoolSize = 5
                maxPoolSize = 10
                queueCapacity = 25
                it
            }
        }

        @Bean
        Job job(TaskExecutor taskExecutor, InterceptingItemReader reader, InterceptingItemProcessor processor,
                InterceptingItemWriter writer, MetricRegistry metricRegistry, ScheduledReporter reporter) {
            def statsListener =
                    new ParallelProcessingStatsListener(metricRegistry, { reporter.report() })

//            def statsListener = new ThreadDebugListener()
            jobBuilderFactory
                    .get("job")
                    .listener(JobListenerFactoryBean.getListener(statsListener))
                    .start(
                    stepBuilderFactory.get("step1")
                            .chunk(10)
                            .faultTolerant().skip(Throwable).skipLimit(Integer.MAX_VALUE).listener(statsListener as Object)
                            .reader(new SynchronizedItemReader(reader))
                            .processor(processor)
                            .writer(new SynchronizedItemWriter(writer))
                            .listener(statsListener as Object)
                            .taskExecutor(taskExecutor)
                            .build()
            ).build()
        }
    }
}
