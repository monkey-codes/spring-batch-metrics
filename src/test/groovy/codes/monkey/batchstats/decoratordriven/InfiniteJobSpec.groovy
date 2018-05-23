package codes.monkey.batchstats.decoratordriven

import codes.monkey.batchstats.eventdriven.InterceptingItemProcessor
import codes.monkey.batchstats.eventdriven.InterceptingItemReader
import codes.monkey.batchstats.eventdriven.InterceptingItemWriter
import codes.monkey.batchstats.eventdriven.ListenerTestConfig
import codes.monkey.batchstats.htmlreport.HtmlReportJobExecutionListener
import com.codahale.metrics.MetricRegistry
import com.codahale.metrics.ScheduledReporter
import org.springframework.batch.core.BatchStatus
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.JobParameters
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.batch.core.listener.JobListenerFactoryBean
import org.springframework.batch.item.ItemReader
import org.springframework.batch.item.NonTransientResourceException
import org.springframework.batch.item.ParseException
import org.springframework.batch.item.UnexpectedInputException
import org.springframework.batch.item.function.FunctionItemProcessor
import org.springframework.batch.item.support.ListItemWriter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import spock.lang.Ignore
import spock.lang.Specification

/**
 * @author Johan Zietsman (jzietsman@thoughtworks.com.au).
 */
@ContextConfiguration(classes = TestConfig)
class InfiniteJobSpec extends Specification {

    @Autowired
    Job job

    @Autowired
    JobLauncher jobLauncher

    @Autowired
    InterceptingItemReader interceptingItemReader

    @Autowired
    InterceptingItemProcessor interceptingItemProcessor

    @Autowired
    ScheduledReporter scheduledReporter

    @Autowired
    InterceptingItemWriter interceptingItemWriter

    @Ignore
    @DirtiesContext
    def "run infinite job"() {
        given:
        def delay = randomDelay(100, 200)
        interceptingItemReader.transform = { delay(); if (it && it < 10) throw new Exception("Read Error"); it }
        interceptingItemProcessor.transform = {
            delay(); if (it > 10 && it < 20) throw new Exception("Process Prror"); it
        }
        interceptingItemWriter.transform = { delay(); if (it[0] < 10) throw new Exception("Write Error"); it }

        when:
        JobExecution jobExecution = jobLauncher.run(job, new JobParameters())
        then:
        jobExecution.status == BatchStatus.COMPLETED
    }

    def randomDelay(int min, int max) {
        Random random = new Random()
        return { ->
            Thread.sleep(random.nextInt((max - min) + 1) + min)
        }
    }

    @TestConfiguration
    static class TestConfig extends ListenerTestConfig {

        @Bean
        ItemReader infiniteReader() {
            new ItemReader<Integer>() {
                Random random = new Random()
                int counter = 0
                @Override
                Integer read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
                    if(counter++ == 300) {
                        return null
                    }
                    return random.nextInt(101)
                }
            }
        }

        @Bean
        InterceptingItemReader interceptingItemReader(ItemReader reader) {
            new InterceptingItemReader(reader)
        }

        @Bean
        InterceptingItemProcessor interceptingItemProcessor() {
            new InterceptingItemProcessor(new FunctionItemProcessor({ it }))
        }

        @Bean
        InterceptingItemWriter interceptingItemWriter() {
            new InterceptingItemWriter(new ListItemWriter())
        }

        @Bean
        HtmlReportJobExecutionListener htmlReportJobExecutionListener(){
            new HtmlReportJobExecutionListener("/Users/johanz/git/monkey/batch-stats/build/metrics.json",
            "/Users/johanz/git/monkey/batch-stats/build/report.html")
        }

        @Bean
        Job job(InterceptingItemReader reader, InterceptingItemProcessor processor,
                InterceptingItemWriter writer, MetricRegistry metricRegistry,
                HtmlReportJobExecutionListener htmlReportJobExecutionListener) {
            def factory = new DecoratorFactory(metricRegistry)
            jobBuilderFactory
                    .get("job")
                    .listener(JobListenerFactoryBean.getListener(factory))
                    .listener(htmlReportJobExecutionListener)
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
