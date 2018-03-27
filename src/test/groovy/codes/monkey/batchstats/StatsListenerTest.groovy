package codes.monkey.batchstats

import com.codahale.metrics.MetricRegistry
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.batch.core.BatchStatus
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.JobParameters
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.batch.core.listener.JobListenerFactoryBean
import org.springframework.batch.item.ItemReader
import org.springframework.batch.item.support.ListItemReader
import org.springframework.batch.item.support.ListItemWriter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

import java.util.concurrent.ThreadLocalRandom
import java.util.function.Function

import static codes.monkey.batchstats.StatsEventsGrabber.lastEvent
import static org.hamcrest.Matchers.allOf
import static org.hamcrest.Matchers.hasEntry
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertThat

/**
 * @author Johan Zietsman (jzietsman@thoughtworks.com.au).
 * https://github.com/spring-projects/spring-batch/blob/4.0.1.RELEASE/spring-batch-core-tests/src/test/java/org/springframework/batch/core/test/concurrent/ConcurrentTransactionTests.java
 */
@RunWith(SpringJUnit4ClassRunner)
@ContextConfiguration(classes = StatsListenerTest.ListenerJobConfig)
class StatsListenerTest {

    @Autowired
    private Job job

    @Autowired
    private JobLauncher jobLauncher
    private StatsEventsGrabber statsEventsGrabber

    @Before
    void setup() {
        statsEventsGrabber = new StatsEventsGrabber()
    }

    @After
    void tearDown() {
        statsEventsGrabber.stop()
    }

    @DirtiesContext
    @Test
    void testSingleThreadedJobWithStatsListener() throws Exception {

        JobExecution jobExecution = jobLauncher.run(job, new JobParameters())
        assertEquals(jobExecution.status, BatchStatus.COMPLETED)
        assertThat(statsEventsGrabber,
                allOf(
                        lastEvent('job.step1.chunk.read', hasEntry('count', '101')),
                        lastEvent('job.step1.chunk.process', hasEntry('count', '101')),
                        lastEvent('job.step1.chunk.write', hasEntry('count', '11'))
                )
        )
    }


    @Configuration
    static class ListenerJobConfig extends ListenerTestConfig {

        @Bean
        Job job(MetricRegistry metricRegistry) {
            def statsListener = new StatsListener(metricRegistry)
            jobBuilderFactory
                    .get("job")
                    .listener(JobListenerFactoryBean.getListener(statsListener))
                    .start(
                    stepBuilderFactory.get("step1")
                            .chunk(10)
//                            .reader(new VariableRateReaderDecorator<>(new ListItemReader((1..100).collect())))
                            .reader(new ListItemReader((1..101).collect()))
                            .processor({ it -> it * 2 } as Function)
                            .writer(new ListItemWriter())
                            .listener(statsListener as Object)
                            .build()
            ).build()

        }

    }

    static class VariableRateReaderDecorator<T> implements ItemReader<T> {
        private ItemReader<T> delegate

        VariableRateReaderDecorator(ItemReader<T> delegate) {
            this.delegate = delegate
        }

        @Override
        T read() throws Exception {
            Thread.sleep(ThreadLocalRandom.current().nextInt(50, 100))
            delegate.read()
        }
    }

}