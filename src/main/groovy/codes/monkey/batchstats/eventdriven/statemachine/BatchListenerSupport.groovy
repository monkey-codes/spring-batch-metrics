package codes.monkey.batchstats.eventdriven.statemachine

import org.springframework.batch.core.ExitStatus
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.StepExecution
import org.springframework.batch.core.scope.context.ChunkContext

/**
 * @author Johan Zietsman (jzietsman@thoughtworks.com.au).
 */
class BatchListenerSupport implements BatchListener {

    def defaultHandler = {Object... args -> throw new IllegalStateException("${this.getClass().name}: ${args[0]}")}

    @Override
    void beforeJob(JobExecution jobExecution) {
        defaultHandler("beforeJob", jobExecution)
    }

    @Override
    void afterJob(JobExecution jobExecution) {
        defaultHandler("afterJob", jobExecution)
    }

    @Override
    void beforeChunk(ChunkContext context) {
        defaultHandler("beforeChunk", context)
    }

    @Override
    void afterChunk(ChunkContext context) {
        defaultHandler("afterChunk", context)
    }

    @Override
    void afterChunkError(ChunkContext context) {
        defaultHandler("afterChunkError", context)
    }

    @Override
    void beforeProcess(Object item) {
        defaultHandler("beforeProcess", item)
    }

    @Override
    void afterProcess(Object item, Object result) {
        defaultHandler("afterProcess", item, result)
    }

    @Override
    void onProcessError(Object item, Exception e) {
        defaultHandler("onProcessError", item, e)
    }

    @Override
    void beforeRead() {
        defaultHandler("beforeRead")
    }

    @Override
    void afterRead(Object item) {
        defaultHandler("afterRead", item)
    }

    @Override
    void onReadError(Exception ex) {
        defaultHandler("onReadError", ex)
    }

    @Override
    void beforeWrite(List items) {
        defaultHandler("beforeWrite", items)
    }

    @Override
    void afterWrite(List items) {
        defaultHandler("afterWrite", items)
    }

    @Override
    void onWriteError(Exception exception, List items) {
        defaultHandler("onWriteError", exception, items)
    }

    @Override
    void onSkipInRead(Throwable t) {
        defaultHandler("onSkipInRead", t)
    }

    @Override
    void onSkipInWrite(Object item, Throwable t) {
        defaultHandler("onSkipInWrite", item, t)
    }

    @Override
    void onSkipInProcess(Object item, Throwable t) {
        defaultHandler("onSkipInProcess", item, t)
    }

    @Override
    void beforeStep(StepExecution stepExecution) {
        defaultHandler("beforeStep", stepExecution)
    }

    @Override
    ExitStatus afterStep(StepExecution stepExecution) {
        defaultHandler("afterStep", stepExecution)
    }
}
