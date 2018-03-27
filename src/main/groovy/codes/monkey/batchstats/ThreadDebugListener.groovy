package codes.monkey.batchstats

import com.codahale.metrics.MetricRegistry
import com.codahale.metrics.Timer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.StepExecution
import org.springframework.batch.core.annotation.*
import org.springframework.batch.core.scope.context.ChunkContext

/**
 * @author Johan Zietsman (jzietsman@thoughtworks.com.au).
 */
class ThreadDebugListener {

    private static Logger LOG = LoggerFactory.getLogger(ThreadDebugListener.class.name)

    def delegate

    ThreadDebugListener(delegate) {
        this.delegate = delegate
    }

    @BeforeJob
    void beforeJob(JobExecution jobExecution) {
        LOG.info("beforeJob")
        this.delegate.beforeJob(jobExecution)
    }

    @AfterJob
    void afterJob(JobExecution jobExecution) {
        LOG.info("afterJob")
        this.delegate.afterJob(jobExecution)

    }

    @BeforeStep
    void beforeStep(StepExecution stepExecution) {
        LOG.info("beforeStep")
        this.delegate.beforeStep(stepExecution)
    }

    @AfterStep
    void afterStep(StepExecution stepExecution){
        LOG.info("afterStep")
        this.delegate.afterStep(stepExecution)
    }

    @BeforeChunk
    void beforeChunk(ChunkContext context){
        LOG.info("beforeChunk")
        this.delegate.beforeChunk(context)
    }

    @AfterChunk
    void afterChunk(ChunkContext context){
        LOG.info("afterChunk")
        this.delegate.afterChunk(context)
    }


    @BeforeRead
    void beforeRead() {
        LOG.info("beforeRead")
        this.delegate.beforeRead()
    }

    @AfterRead
    void afterRead(Object item) {
        LOG.info("afterRead")
        this.delegate.afterRead(item)
    }

    @BeforeProcess
    void beforeProcess(Object item) {
        LOG.info("beforeProcess")
        this.delegate.beforeProcess(item)
    }

    @AfterProcess
    void afterProcess(Object item, Object result){
        LOG.info("afterProcess")
        this.delegate.afterProcess(item, result)
    }

    @BeforeWrite
    void beforeWrite(List<?> items) {
        LOG.info("beforeWrite")
        this.delegate.beforeWrite(items)

    }

    @AfterWrite
    void afterWrite(List<?> items) {
        LOG.info("afterWrite")
        this.delegate.afterWrite(items)

    }

}
