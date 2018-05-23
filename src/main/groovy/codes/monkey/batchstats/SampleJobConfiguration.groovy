package codes.monkey.batchstats

import codes.monkey.batchstats.decoratordriven.DecoratorFactory
import codes.monkey.batchstats.eventdriven.ParallelProcessingStatsListener
import codes.monkey.batchstats.eventdriven.StatsListener
import codes.monkey.batchstats.eventdriven.statemachine.JobStateMachine
import codes.monkey.batchstats.htmlreport.HtmlReportJobExecutionListener
import com.codahale.metrics.MetricRegistry
import com.codahale.metrics.ScheduledReporter
import org.springframework.batch.core.Job
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.listener.JobListenerFactoryBean
import org.springframework.batch.item.*
import org.springframework.batch.item.function.FunctionItemProcessor
import org.springframework.batch.item.support.ListItemWriter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.TaskExecutor
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor

/**
 * @author Johan Zietsman (jzietsman@thoughtworks.com.au).
 */
@EnableBatchProcessing
@Configuration
class SampleJobConfiguration {
    @Autowired
    JobBuilderFactory jobBuilderFactory

    @Autowired
    StepBuilderFactory stepBuilderFactory


    @Bean
    Closure randomDelay() {
        Random random = new Random()
        int min = 100
        int max = 300
        return { ->
            Thread.sleep(random.nextInt((max - min) + 1) + min)
        }
    }

    @Bean
    ItemReader randomReader(Closure randomDelay) {
        new ItemReader<Integer>() {
            Random random = new Random()
            int counter = 0

            @Override
            Integer read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
                if (counter++ >= 100) return null
                randomDelay()
                return random.nextInt(101)
            }
        }
    }

    @Bean
    ItemProcessor itemProcessor(Closure randomDelay) {
        new FunctionItemProcessor({ randomDelay(); it * 2 })
    }

    @Bean
    ItemWriter itemWriter(Closure randomDelay) {
        new ListItemWriter() {
            @Override
            void write(List items) throws Exception {
                randomDelay()
                super.write(items)
            }
        }
    }

    @Bean
    HtmlReportJobExecutionListener htmlReportJobExecutionListener(
            @Value('${codes.monkey.metric.logfile}') String dataFile,
            @Value('${codes.monkey.metric.htmlreport}') String outputFile) {
        new HtmlReportJobExecutionListener(dataFile, outputFile)
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
    Job singleThreadDecoratorDrivenJob(ItemReader reader, ItemProcessor processor,
                                       ItemWriter writer, MetricRegistry metricRegistry,
                                       HtmlReportJobExecutionListener htmlReportJobExecutionListener) {
        def factory = new DecoratorFactory(metricRegistry)
        jobBuilderFactory
                .get("single.thread.decorator.driven.job")
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

    @Bean
    Job singleThreadEventDrivenJob(ItemReader reader, ItemProcessor processor,
                                   ItemWriter writer, MetricRegistry metricRegistry,
                                   HtmlReportJobExecutionListener htmlReportJobExecutionListener,
                                   ScheduledReporter reporter) {
        def statsListener = JobStateMachine.idle(
                new StatsListener(metricRegistry, { reporter.report() })
        )
        jobBuilderFactory
                .get("single.thread.event.driven.job")
                .listener(JobListenerFactoryBean.getListener(statsListener))
                .listener(htmlReportJobExecutionListener)
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

    @Bean
    Job multiThreadedEventDrivenJob(ItemReader reader, ItemProcessor processor,
                                    ItemWriter writer, MetricRegistry metricRegistry,
                                    HtmlReportJobExecutionListener htmlReportJobExecutionListener,
                                    TaskExecutor taskExecutor,
                                    ScheduledReporter reporter) {
        def statsListener =
                new ParallelProcessingStatsListener(metricRegistry, { reporter.report() })
        jobBuilderFactory
                .get("multi.threaded.event.driven.job")
                .listener(JobListenerFactoryBean.getListener(statsListener))
                .listener(htmlReportJobExecutionListener)
                .start(
                stepBuilderFactory.get("step1")
                        .chunk(10)
                        .faultTolerant().skip(Throwable).skipLimit(Integer.MAX_VALUE).listener(statsListener as Object)
                        .reader(reader)
                        .processor(processor)
                        .writer(writer)
                        .listener(statsListener as Object)
                        .taskExecutor(taskExecutor)
                        .build()
        ).build()
    }

}
