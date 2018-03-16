package codes.monkey.batchstats

import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.batch.core.BatchStatus
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.JobParameters
import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean
import org.springframework.batch.item.file.FlatFileItemReader
import org.springframework.batch.item.support.ListItemReader
import org.springframework.batch.item.support.ListItemWriter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.TaskExecutor
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.scheduling.config.TaskExecutorFactoryBean
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

import javax.sql.DataSource
import java.util.function.Function

/**
 * @author Johan Zietsman (jzietsman@thoughtworks.com.au).
 * https://github.com/spring-projects/spring-batch/blob/4.0.1.RELEASE/spring-batch-core-tests/src/test/java/org/springframework/batch/core/test/concurrent/ConcurrentTransactionTests.java
 */
@RunWith(SpringJUnit4ClassRunner)
@ContextConfiguration(classes = ListenerTest.ListenerJobConfig)
class ListenerTest {

    @Autowired
    private Job job

    @Autowired
    private JobLauncher jobLauncher

    @DirtiesContext
    @Test
    void testConcurrentLongRunningJobExecutions() throws Exception {

        JobExecution jobExecution = jobLauncher.run(job, new JobParameters())
        Assert.assertEquals(jobExecution.status, BatchStatus.COMPLETED)
    }


    @Configuration
    @EnableBatchProcessing
    static class ListenerJobConfig extends DefaultBatchConfigurer {

        @Autowired
        private JobBuilderFactory jobBuilderFactory

        @Autowired
        private StepBuilderFactory stepBuilderFactory


        @Bean
        TaskExecutor taskExecutor() {
            ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor()
            executor.with {
                corePoolSize = 5
                maxPoolSize = 10
                queueCapacity = 25
            }
            executor
        }

        @Bean
        Job job(TaskExecutor taskExecutor){
            def listener = new StatsListener()
            jobBuilderFactory
                    .get("job")
            .start(
                    stepBuilderFactory.get("step1")
                    .chunk(10)
                    .reader(new ListItemReader((1..100).collect()))
                    .processor ({it -> it * 2} as Function)
                    .writer(new ListItemWriter())
                    .listener(listener as Object)
                    .taskExecutor(taskExecutor)
                    .build()
            ).build()

        }

        @Bean
        DataSource dataSource() {
            new EmbeddedDatabaseBuilder()
                    .setType(EmbeddedDatabaseType.HSQL)
                    .addScript("classpath:org/springframework/batch/core/schema-drop-hsqldb.sql")
                    .addScript("classpath:org/springframework/batch/core/schema-hsqldb.sql")
                    .build()
        }

        @Override
        protected JobRepository createJobRepository() throws Exception {
            JobRepositoryFactoryBean factory = new JobRepositoryFactoryBean()
            factory.setDataSource(dataSource())
            factory.setIsolationLevelForCreate("ISOLATION_READ_COMMITTED")
            factory.setTransactionManager(getTransactionManager())
            factory.afterPropertiesSet()
            factory.getObject()
        }
    }
}