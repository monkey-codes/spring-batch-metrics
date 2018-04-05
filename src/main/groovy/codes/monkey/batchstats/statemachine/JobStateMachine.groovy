package codes.monkey.batchstats.statemachine

import org.springframework.batch.core.ExitStatus
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.StepExecution
import org.springframework.batch.core.scope.context.ChunkContext

/**
 * @author Johan Zietsman (jzietsman@thoughtworks.com.au).
 */
class JobStateMachine implements BatchListener {

    private final JobStateListener listener
    @Delegate
    private BatchListener currentState

    JobStateMachine(JobStateListener listener) {
        this.listener = listener
    }

    static JobStateMachine idle(JobStateListener listener) {
        def machine = new JobStateMachine(listener)
        machine.currentState = new Idle(machine)
        machine
    }


    class Idle extends BatchListenerSupport {

        @Override
        void beforeJob(JobExecution jobExecution) {
            listener.beforeJob(jobExecution)
            currentState = new JobRunning()
        }
    }

    class JobRunning implements BatchListener {

        @Delegate
        private final JobStateListener delegate

        JobRunning() {
            this.delegate = listener
        }

        @Override
        void afterJob(JobExecution jobExecution) {
            listener.afterJob(jobExecution)
            currentState = new Idle()
        }

        @Override
        void beforeRead() {
            listener.beforeRead()
            currentState = new Reading()
        }

        @Override
        void onWriteError(Exception exception, List items) {
            listener.onWriteError(exception, items)
            currentState = new WriteError()
        }

    }

    class Reading extends BatchListenerSupport {

        @Override
        void afterRead(Object item) {
            listener.afterRead(item)
            currentState = new JobRunning()
        }

        @Override
        void afterChunk(ChunkContext context) {
            listener.afterLastRead()
            listener.afterChunk(context)
            currentState = new JobRunning()
        }

        @Override
        void beforeProcess(Object item) {
            listener.afterLastRead()
            listener.beforeProcess(item)
            currentState = new JobRunning()
        }

        @Override
        void onReadError(Exception ex) {
            listener.onReadError(ex)
            currentState = new JobRunning()
        }
    }

    class WriteError extends BatchListenerSupport {
        @Override
        void afterChunkError(ChunkContext context) {
            listener.afterChunkError(context)
            listener.beforeChunkWriteErrorReProcess()
            currentState = new WriteErrorChunkReprocess()
        }
    }

    class WriteErrorChunkReprocess extends BatchListenerSupport {

        ChunkContext lastChunkContext

        @Override
        void afterChunkError(ChunkContext context) {
            swallowEvent('afterChunkError', context)
        }

        @Override
        void beforeChunk(ChunkContext context) {
            swallowEvent('beforeChunk', context)
        }

        @Override
        void beforeProcess(Object item) {
            swallowEvent('beforeProcess', item)
        }

        @Override
        void afterProcess(Object item, Object result) {
            swallowEvent('afterProcess', item, result)
        }

        @Override
        void onWriteError(Exception exception, List items) {
            swallowEvent('onWriteError', exception, items)
        }

        @Override
        void afterWrite(List items) {
            swallowEvent('afterWrite', items)
        }

        @Override
        void afterChunk(ChunkContext context) {
            //cache the last context since we have to emit that once reprocess is done.
            lastChunkContext = context
            swallowEvent('afterChunk', context)
        }

        @Override
        void onSkipInWrite(Object item, Throwable t) {
            swallowEvent('onSkipInWrite', item, t)
        }

        @Override
        void beforeRead() {
            listener.afterChunkWriteErrorReProcess()
            listener.beforeChunk(lastChunkContext)
            lastChunkContext = null
            listener.beforeRead()
            currentState = new JobRunning()
        }

        @Override
        ExitStatus afterStep(StepExecution stepExecution) {
            listener.afterChunkWriteErrorReProcess()
            def exitStatus = listener.afterStep(stepExecution)
            lastChunkContext = null
            currentState = new JobRunning()
            exitStatus
        }

        void swallowEvent(Object... args) {
            //do nothing
        }

    }

}
