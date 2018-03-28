package codes.monkey.batchstats

import com.codahale.metrics.MetricRegistry
import org.springframework.batch.core.BatchStatus
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.JobParameters
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.batch.core.listener.JobListenerFactoryBean
import org.springframework.batch.item.support.ListItemWriter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

import java.util.function.Function

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
        (1..100) | 100       | 100          | 10         | "items divisible by chunk size"
        (1..108) | 108       | 108          | 11         | "items not divisible by chunk size"
    }

    @TestConfiguration
    static class TestConfig extends ListenerTestConfig {

        @Bean
        MutableListItemReader mutableListItemReader() {
            new MutableListItemReader((1..100).collect())
        }

        @Bean
        Job job(MutableListItemReader reader, MetricRegistry metricRegistry) {
            def statsListener = new StatsListener(metricRegistry)
            jobBuilderFactory
                    .get("job")
                    .listener(JobListenerFactoryBean.getListener(statsListener))
                    .start(
                    stepBuilderFactory.get("step1")
                            .chunk(10)
                            .reader(reader)
                            .processor({ it -> it * 2 } as Function)
                            .writer(new ListItemWriter())
                            .listener(statsListener as Object)
                            .build()
            ).build()
        }
    }
}
