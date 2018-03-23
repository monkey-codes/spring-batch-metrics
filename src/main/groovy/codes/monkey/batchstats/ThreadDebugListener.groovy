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





    @BeforeJob
    void beforeJob(JobExecution jobExecution) {
        LOG.info("beforeJob")
    }

    @AfterJob
    void afterJob(JobExecution jobExecution) {
        LOG.info("afterJob")

    }

    @BeforeStep
    void beforeStep(StepExecution stepExecution) {
        LOG.info("beforeStep")
    }

    @AfterStep
    void afterStep(StepExecution stepExecution){
        LOG.info("afterStep")
    }

    @BeforeChunk
    void beforeChunk(ChunkContext context){
        LOG.info("beforeChunk")
    }

    @AfterChunk
    void afterChunk(ChunkContext context){
        LOG.info("afterChunk")
    }


    @BeforeRead
    void beforeRead() {
        LOG.info("beforeRead")
    }

    @AfterRead
    void afterRead(Object item) {
        LOG.info("afterRead")
    }

    @BeforeProcess
    void beforeProcess(Object item) {
        LOG.info("beforeProcess")
    }

    @AfterProcess
    void afterProcess(Object item, Object result){
        LOG.info("afterProcess")
    }

}
