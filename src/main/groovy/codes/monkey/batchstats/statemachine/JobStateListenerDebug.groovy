package codes.monkey.batchstats.statemachine

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.batch.core.ExitStatus
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.StepExecution
import org.springframework.batch.core.scope.context.ChunkContext

/**
 * @author Johan Zietsman (jzietsman@thoughtworks.com.au).
 */
class JobStateListenerDebug implements JobStateListener {

    private static Logger LOG = LoggerFactory.getLogger(JobStateListenerDebug.class.name)

    private JobStateListener delegate

    JobStateListenerDebug(JobStateListener delegate) {
        this.delegate = delegate
    }

    @Override
    void afterLastRead() {
        LOG.info('afterLastRead')
        delegate.afterLastRead()
    }

    @Override
    void beforeChunkWriteErrorReProcess() {
        LOG.info('beforeChunkWriteErrorReProcess')
        delegate.beforeChunkWriteErrorReProcess()
    }

    @Override
    void afterChunkWriteErrorReProcess() {
        LOG.info('afterChunkWriteErrorReProcess')
        delegate.afterChunkWriteErrorReProcess()

    }

    @Override
    void beforeJob(JobExecution jobExecution) {
        LOG.info('beforeJob')
        delegate.beforeJob(jobExecution)
    }

    @Override
    void afterJob(JobExecution jobExecution) {
        LOG.info('afterJob')
        delegate.afterJob(jobExecution)
    }

    @Override
    void beforeChunk(ChunkContext context) {
        LOG.info('beforeChunk')
        delegate.beforeChunk(context)
    }

    @Override
    void afterChunk(ChunkContext context) {
        LOG.info('afterChunk')
        delegate.afterChunk(context)
    }

    @Override
    void afterChunkError(ChunkContext context) {
        LOG.info('afterChunkError')
        delegate.afterChunkError(context)
    }

    @Override
    void beforeProcess(Object item) {
        LOG.info('beforeProcess')
        delegate.beforeProcess(item)
    }

    @Override
    void afterProcess(Object item, Object result) {
        LOG.info('afterProcess')
        delegate.afterProcess(item, result)
    }

    @Override
    void onProcessError(Object item, Exception e) {
        LOG.info('onProcessError')
        delegate.onProcessError(item, e)
    }

    @Override
    void beforeRead() {
        LOG.info('beforeRead')
        delegate.beforeRead()
    }

    @Override
    void afterRead(Object item) {
        LOG.info('afterRead')
        delegate.afterRead(item)
    }

    @Override
    void onReadError(Exception ex) {
        LOG.info('onReadError')
        delegate.onReadError(ex)
    }

    @Override
    void beforeWrite(List items) {
        LOG.info('beforeWrite')
        delegate.beforeWrite(items)
    }

    @Override
    void afterWrite(List items) {
        LOG.info('afterWrite')
        delegate.afterWrite(items)
    }

    @Override
    void onWriteError(Exception exception, List items) {
        LOG.info('onWriteError')
        delegate.onWriteError(exception, items)
    }

    @Override
    void onSkipInRead(Throwable t) {
        LOG.info('onSkipInRead')
        delegate.onSkipInRead(t)
    }

    @Override
    void onSkipInWrite(Object item, Throwable t) {
        LOG.info('onSkipInWrite')
        delegate.onSkipInWrite(item, t)
    }

    @Override
    void onSkipInProcess(Object item, Throwable t) {
        LOG.info('onSkipInProcess')
        delegate.onSkipInProcess(item, t)
    }

    @Override
    void beforeStep(StepExecution stepExecution) {
        LOG.info('beforeStep')
        delegate.beforeStep(stepExecution)
    }

    @Override
    ExitStatus afterStep(StepExecution stepExecution) {
        LOG.info('afterStep')
        delegate.afterStep(stepExecution)
    }
}
