package codes.monkey.batchstats.statemachine

import codes.monkey.batchstats.StatsListener
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.batch.core.ExitStatus
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.StepExecution
import org.springframework.batch.core.scope.context.ChunkContext

/**
 * @author Johan Zietsman (jzietsman@thoughtworks.com.au).
 */
class JobStateMachine implements BatchListener {

    private static Logger LOG = LoggerFactory.getLogger(JobStateMachine.class.name)

    private final JobStateListener listener
    @Delegate
    private BatchListener currentState

    JobStateMachine(JobStateListener listener) {
        this.listener = listener
    }

    private void updateState(BatchListener newState) {
        def from = currentState == null ? 'null' : currentState.getClass().simpleName
        def to = newState.getClass().simpleName
        LOG.debug "$from -> $to"
        currentState = newState
    }

    static JobStateMachine idle(JobStateListener listener) {
        def machine = new JobStateMachine(listener)
        machine.updateState(new Idle(machine))
        machine
    }

    static def jobRunning(StatsListener listener) {
        def machine = new JobStateMachine(listener)
        machine.updateState(new JobRunning(machine))
        machine
    }

    class Idle extends BatchListenerSupport {

        @Override
        void beforeJob(JobExecution jobExecution) {
            listener.beforeJob(jobExecution)
            updateState(new JobRunning())
        }
    }

    class JobRunning extends BatchListenerSupport {

        @Override
        void beforeStep(StepExecution stepExecution) {
            listener.beforeStep(stepExecution)
        }

        @Override
        void beforeChunk(ChunkContext context) {
            listener.beforeChunk(context)
            updateState(new ChunkRunning())
        }

        @Override
        ExitStatus afterStep(StepExecution stepExecution) {
            listener.afterStep(stepExecution)
        }

        @Override
        void afterJob(JobExecution jobExecution) {
            listener.afterJob(jobExecution)
            updateState(new Idle())
        }

//        @Override
//        void beforeRead() {
//            listener.beforeRead()
//            updateState(new Reading())
//        }
//
//        @Override
//        void onWriteError(Exception exception, List items) {
//            listener.onWriteError(exception, items)
//            updateState(new WriteError())
//        }

    }

    class ChunkRunning extends BatchListenerSupport {
        @Override
        void beforeRead() {
            listener.beforeRead()
            updateState(new Reading())
        }

        @Override
        void beforeProcess(Object item) {
            listener.beforeChunkWriteErrorReProcess()
            listener.beforeProcess(item)
            updateState(new ChunkReprocess())
        }

        @Override
        void beforeWrite(List items) {
            listener.beforeChunkWriteErrorReProcess()
            updateState(new ChunkReprocess())
        }

        @Override
        void afterChunk(ChunkContext context) {
            listener.afterChunk(context)
            updateState(new JobRunning())
        }

        @Override
        void afterChunkError(ChunkContext context) {
            listener.afterChunkError(context)
            updateState(new JobRunning())
        }

        @Override
        void onSkipInRead(Throwable t) {
            listener.onSkipInRead(t)
        }

        @Override
        void onSkipInWrite(Object item, Throwable t) {
            listener.onSkipInWrite(item, t)
        }
    }

    class Reading extends BatchListenerSupport {

        @Override
        void afterRead(Object item) {
            listener.afterRead(item)
            updateState(new AfterReading())
        }

        @Override
        void afterChunk(ChunkContext context) {
            listener.afterLastRead()
            listener.afterChunk(context)
            updateState(new JobRunning())
        }

        @Override
        void onSkipInRead(Throwable t) {
            listener.onSkipInRead(t)
        }

        @Override
        void beforeProcess(Object item) {
            listener.afterLastRead()
            listener.beforeProcess(item)
            updateState(new Processing())
        }

        @Override
        void onReadError(Exception ex) {
            listener.onReadError(ex)
            updateState(new ChunkRunning())
        }
    }

    class AfterReading extends BatchListenerSupport {
        //Interim state to detect if next action is more reading or the start or processing
        @Override
        void beforeProcess(Object item) {
            listener.beforeProcess(item)
            updateState(new Processing())
        }

        @Override
        void beforeRead() {
            listener.beforeRead()
            updateState(new Reading())
        }
    }

    class Processing extends BatchListenerSupport {

        @Override
        void beforeProcess(Object item) {
            listener.beforeProcess(item)
        }

        @Override
        void afterProcess(Object item, Object result) {
            listener.afterProcess(item, result)
        }

        @Override
        void onProcessError(Object item, Exception e) {
            listener.onProcessError(item, e)
            updateState(new ChunkRunning())
        }

        @Override
        void afterChunkError(ChunkContext context) {
            listener.afterChunkError(context)
        }

        @Override
        void beforeWrite(List items) {
            listener.beforeWrite(items)
            updateState(new Writing())
        }
    }

    class Writing extends BatchListenerSupport {
        @Override
        void afterWrite(List items) {
            listener.afterWrite(items)
            updateState(new ChunkRunning())
        }

        @Override
        void onWriteError(Exception exception, List items) {
            listener.onWriteError(exception, items)
            updateState(new ChunkRunning())
        }
    }

    class ChunkReprocess extends BatchListenerSupport {

//        ChunkContext lastAfterChunkContext
//        ChunkContext lastBeforeChunkContext
//        String afterChunkEvent

        @Override
        void afterChunkError(ChunkContext context) {
//            lastAfterChunkContext = context
//            swallowEvent('afterChunkError', context)
            listener.afterChunkWriteErrorReProcess()
            listener.afterChunkError(context)
            updateState(new JobRunning())
        }

        @Override
        void beforeChunk(ChunkContext context) {
//            this.lastBeforeChunkContext = context
            swallowEvent('beforeChunk', context)
        }

        @Override
        void beforeProcess(Object item) {
            listener.beforeProcess(item)
//            swallowEvent('beforeProcess', item)
        }

        @Override
        void afterProcess(Object item, Object result) {
            listener.afterProcess(item, result)
//            swallowEvent('afterProcess', item, result)
        }

        @Override
        void onProcessError(Object item, Exception e) {
            listener.onProcessError(item, e)
        }

        @Override
        void onWriteError(Exception exception, List items) {
            //we don't know if a beforeWrite was fired so this will unbalance the event interface
            swallowEvent('onWriteError', exception, items)
//            listener.onWriteError(exception, items)
        }

        @Override
        void beforeWrite(List items) {
            swallowEvent('beforeWrite', items)
        }

        @Override
        void afterWrite(List items) {
            swallowEvent('afterWrite', items)
        }

        @Override
        void afterChunk(ChunkContext context) {
            //cache the last context since we have to emit that once reprocess is done.
//            lastAfterChunkContext = context
            listener.afterChunkWriteErrorReProcess()
            listener.afterChunk(context)
            updateState(new JobRunning())
//            swallowEvent('afterChunk', context)
        }

        @Override
        void onSkipInWrite(Object item, Throwable t) {
            listener.onSkipInWrite(item, t)
        }

        @Override
        void onSkipInProcess(Object item, Throwable t) {
            listener.onSkipInProcess(item, t)
        }

//        @Override
//        void beforeRead() {
//            listener.afterChunkWriteErrorReProcess()
//            listener."$afterChunkEvent"(lastAfterChunkContext)
//            listener.beforeChunk(lastBeforeChunkContext)
//            lastAfterChunkContext = null
//            listener.beforeRead()
//            updateState(new Reading())
//        }
//
//        @Override
//        ExitStatus afterStep(StepExecution stepExecution) {
//            listener.afterChunkWriteErrorReProcess()
//            def exitStatus = listener.afterStep(stepExecution)
//            lastAfterChunkContext = null
//            lastBeforeChunkContext = null
//            updateState(new JobRunning())
//            exitStatus
//        }

        void swallowEvent(Object... args) {
        }

    }

}
