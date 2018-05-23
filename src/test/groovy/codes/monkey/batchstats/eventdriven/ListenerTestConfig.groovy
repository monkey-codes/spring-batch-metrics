package codes.monkey.batchstats.eventdriven

import codes.monkey.batchstats.StatsConfig
import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType

import javax.sql.DataSource


@Configuration
@Import(StatsConfig)
@EnableBatchProcessing
abstract class ListenerTestConfig extends DefaultBatchConfigurer {

    @Autowired
    protected JobBuilderFactory jobBuilderFactory

    @Autowired
    protected StepBuilderFactory stepBuilderFactory

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
