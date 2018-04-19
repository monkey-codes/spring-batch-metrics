package codes.monkey.batchstats.decoratordriven

import codes.monkey.batchstats.StatsNamespace
import com.codahale.metrics.MetricRegistry
import org.springframework.batch.core.ExitStatus
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.StepExecution
import org.springframework.batch.core.annotation.AfterJob
import org.springframework.batch.core.annotation.AfterStep
import org.springframework.batch.core.annotation.BeforeJob
import org.springframework.batch.core.annotation.BeforeStep
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.ItemReader
import org.springframework.batch.item.ItemWriter

/**
 * @author Johan Zietsman (jzietsman@thoughtworks.com.au).
 */
class DecoratorFactory {
    private MetricRegistry metricRegistry
    private StatsNamespace namespace

    DecoratorFactory(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry
        this.namespace = new StatsNamespace()
    }

    @BeforeJob
    void beforeJob(JobExecution jobExecution) {
        namespace.push(jobExecution.jobInstance.jobName)
    }

    @BeforeStep
    void beforeStep(StepExecution stepExecution) {
        namespace.push(stepExecution.stepName)
    }

    @AfterStep
    ExitStatus afterStep(StepExecution stepExecution) {
        namespace.pop()
        return stepExecution.exitStatus
    }

    @AfterJob
    void afterJob(JobExecution jobExecution) {
        namespace.pop()
    }

    public <T> ItemReader<T> metricReader(ItemReader<T> reader) {
        new MetricReader<T>(reader, metricRegistry, namespace)
    }

    public <I, O> ItemProcessor<I, O> metricProcessor(ItemProcessor<I, O> processor) {
        new MetricProcessor<I, O>(processor, metricRegistry, namespace)
    }

    public <T> ItemWriter<T> metricWriter(ItemWriter<T> writer) {
        new MetricWriter<T>(writer, metricRegistry, namespace)
    }
}
