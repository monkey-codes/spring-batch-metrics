package codes.monkey.batchstats

import org.springframework.batch.core.*
import org.springframework.batch.core.scope.context.ChunkContext

/**
 * @author Johan Zietsman (jzietsman@thoughtworks.com.au).
 */
class JobStateMachine implements JobState {

    private JobStateListener listener
    @Delegate
    private JobState currentState

    JobStateMachine(JobStateListener listener) {
        this.listener = listener
    }

    static JobStateMachine idle(JobStateListener listener) {
        def machine = new JobStateMachine(listener)
        machine.currentState = new Idle(machine)
        machine
    }


    class Idle extends JobStateSupport {

        @Override
        void beforeJob(JobExecution jobExecution) {
            listener.beforeJob(jobExecution)
            currentState = new JobRunning()
        }
    }

    class JobRunning extends JobStateSupport {
        @Override
        void beforeStep(StepExecution stepExecution) {
            listener.beforeStep(stepExecution)
        }

        @Override
        void afterJob(JobExecution jobExecution) {
            listener.afterJob(jobExecution)
            currentState = new Idle()
        }

        @Override
        void beforeChunk(ChunkContext context) {
            listener.beforeChunk(context)
        }

        @Override
        ExitStatus afterStep(StepExecution stepExecution) {
            def exitStatus = listener.afterStep(stepExecution)
            exitStatus
        }

        @Override
        void beforeRead() {
            listener.beforeRead()
            currentState = new Reading()
        }
    }

    class Reading extends JobStateSupport {

        @Override
        void afterRead(Object item) {
            listener.afterRead(item)
            currentState = new JobRunning()
        }

        @Override
        void afterChunk(ChunkContext context) {
            listener.onAfterLastRead()
            listener.afterChunk(context)
            currentState = new JobRunning()
        }

        @Override
        void beforeProcess(Object item) {
            listener.onAfterLastRead()
            listener.beforeProcess(item)
        }

        @Override
        void onReadError(Exception ex) {
            listener.onReadError(ex)
            currentState = new JobRunning()
        }
    }

    interface JobState extends JobExecutionListener,
            StepExecutionListener, ChunkListener, ItemReadListener,
            ItemProcessListener, ItemWriteListener, SkipListener {
    }

    static class JobStateSupport implements JobState {

        @Override
        void beforeJob(JobExecution jobExecution) {
            illegal("beforeJob")
        }

        @Override
        void afterJob(JobExecution jobExecution) {
            illegal("afterJob")
        }

        @Override
        void beforeChunk(ChunkContext context) {
            illegal("beforeChunk")
        }

        @Override
        void afterChunk(ChunkContext context) {
            illegal("afterChunk")
        }

        @Override
        void afterChunkError(ChunkContext context) {
            illegal("afterChunkError")
        }

        @Override
        void beforeProcess(Object item) {
            illegal("beforeProcess")
        }

        @Override
        void afterProcess(Object item, Object result) {
            illegal("afterProcess")
        }

        @Override
        void onProcessError(Object item, Exception e) {
            illegal("onProcessError")
        }

        @Override
        void beforeRead() {
            illegal("beforeRead")
        }

        @Override
        void afterRead(Object item) {
            illegal("afterRead")
        }

        @Override
        void onReadError(Exception ex) {
            illegal("onReadError")
        }

        @Override
        void beforeWrite(List items) {
            illegal("beforeWrite")
        }

        @Override
        void afterWrite(List items) {
            illegal("afterWrite")
        }

        @Override
        void onWriteError(Exception exception, List items) {
            illegal("onWriteError")
        }

        @Override
        void onSkipInRead(Throwable t) {
            illegal("onSkipInRead")
        }

        @Override
        void onSkipInWrite(Object item, Throwable t) {
            illegal("onSkipInWrite")
        }

        @Override
        void onSkipInProcess(Object item, Throwable t) {
            illegal("onSkipInProcess")
        }

        @Override
        void beforeStep(StepExecution stepExecution) {
            illegal("beforeStep")
        }

        @Override
        ExitStatus afterStep(StepExecution stepExecution) {
            illegal("afterStep")
        }

        private void illegal(String beforeJob) {
            throw new IllegalStateException("${getClass().name}: $beforeJob")
        }
    }
}
