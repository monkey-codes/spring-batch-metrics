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
import org.springframework.batch.item.support.ListItemReader
import org.springframework.batch.item.support.ListItemWriter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.TaskExecutor
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

import java.util.function.Function

import static codes.monkey.batchstats.StatsEventsGrabber.lastEvent
import static codes.monkey.batchstats.StatsEventsGrabber.lastEvent
import static codes.monkey.batchstats.StatsEventsGrabber.lastEvent
import static org.hamcrest.Matchers.allOf
import static org.hamcrest.Matchers.hasEntry
import static org.hamcrest.Matchers.hasEntry
import static org.hamcrest.Matchers.hasEntry
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertThat

/**
 * @author Johan Zietsman (jzietsman@thoughtworks.com.au).
 */
@RunWith(SpringJUnit4ClassRunner)
@ContextConfiguration(classes = ListenerJobConfig)
class ParallelProcessingStatsListenerTest {

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
    void testMultiThreadedJobWithStatsListener() throws Exception {
        /*
        Fails when a chunk size of 1 is created, the 2nd read does not complete but a process happenes
        before the after chunk is called:
        2018-03-26 13:28:12.693 [taskExecutor-3] INFO  - beforeRead
        2018-03-26 13:28:12.694 [taskExecutor-3] INFO  - afterRead
        2018-03-26 13:28:12.701 [taskExecutor-3] INFO  - beforeRead
        ..no afterChunk event to pop null read.
        2018-03-26 13:28:12.754 [taskExecutor-3] INFO  - beforeProcess
        * */
        JobExecution jobExecution = jobLauncher.run(job, new JobParameters())
        assertEquals(jobExecution.status, BatchStatus.COMPLETED)
        assertThat(statsEventsGrabber,
                allOf(
                        lastEvent('job.step1.chunk.read', hasEntry('count', '100')),
                        lastEvent('job.step1.chunk.process', hasEntry('count', '100'))
//                        ,
//                        lastEvent('job.step1.chunk.write', hasEntry('count', '10'))
                )
        )
    }


    @Configuration
    static class ListenerJobConfig extends ListenerTestConfig {

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
        Job job(TaskExecutor taskExecutor, MetricRegistry metricRegistry) {
            def statsListener = new ParallelProcessingStatsListener(metricRegistry)
            jobBuilderFactory
                    .get("job")
                    .listener(JobListenerFactoryBean.getListener(statsListener))
                    .start(
                    stepBuilderFactory.get("step1")
                            .chunk(10)
                            .reader(new SynchronizedItemReader(new ListItemReader((1..100).collect())))
                            .processor({ it -> it * 2 } as Function)
                            .writer(new SynchronizedItemWriter(new ListItemWriter()))
                            .listener(statsListener as Object)
                            .taskExecutor(taskExecutor)
                            .build()
            ).build()
        }
    }

}