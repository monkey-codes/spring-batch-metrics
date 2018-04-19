package codes.monkey.batchstats.eventdriven

import codes.monkey.batchstats.StatsNamespace
import codes.monkey.batchstats.eventdriven.statemachine.BatchListener
import codes.monkey.batchstats.eventdriven.statemachine.JobStateMachine
import com.codahale.metrics.MetricRegistry
import org.springframework.batch.core.ExitStatus
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.StepExecution
import org.springframework.batch.core.scope.context.ChunkContext

/**
 * @author Johan Zietsman (jzietsman@thoughtworks.com.au).
 */
class ParallelProcessingStatsListener implements BatchListener {

    private MetricRegistry metricRegistry
    private volatile StatsNamespace parentNamespace = new StatsNamespace()
    private JobStateMachine jobLevelState

    private ThreadLocal<JobStateMachine> chunkLevelState = ThreadLocal.withInitial {
        JobStateMachine.jobRunning(new StatsListener(metricRegistry, new StatsNamespace(parentNamespace), {}))
    }


    ParallelProcessingStatsListener(MetricRegistry metricRegistry, Closure afterJobCallback) {
        this.metricRegistry = metricRegistry
        jobLevelState = JobStateMachine.idle(
                new StatsListener(metricRegistry, parentNamespace, afterJobCallback)
        )
    }

    @Override
    void beforeJob(JobExecution jobExecution) {
        jobLevelState.beforeJob(jobExecution)
    }

    @Override
    void afterJob(JobExecution jobExecution) {
        jobLevelState.afterJob(jobExecution)
    }

    @Override
    void beforeChunk(ChunkContext context) {
        chunkLevelState.get().beforeChunk(context)
    }

    @Override
    void afterChunk(ChunkContext context) {
        chunkLevelState.get().afterChunk(context)
    }

    @Override
    void afterChunkError(ChunkContext context) {
        chunkLevelState.get().afterChunkError(context)
    }

    @Override
    void beforeProcess(Object item) {
        chunkLevelState.get().beforeProcess(item)
    }

    @Override
    void afterProcess(Object item, Object result) {
        chunkLevelState.get().afterProcess(item, result)
    }

    @Override
    void onProcessError(Object item, Exception e) {
        chunkLevelState.get().onProcessError(item, e)
    }

    @Override
    void beforeRead() {
        chunkLevelState.get().beforeRead()
    }

    @Override
    void afterRead(Object item) {
        chunkLevelState.get().afterRead(item)
    }

    @Override
    void onReadError(Exception ex) {
        chunkLevelState.get().onReadError(ex)
    }

    @Override
    void beforeWrite(List items) {
        chunkLevelState.get().beforeWrite(items)
    }

    @Override
    void afterWrite(List items) {
        chunkLevelState.get().afterWrite(items)
    }

    @Override
    void onWriteError(Exception exception, List items) {
        chunkLevelState.get().onWriteError(exception, items)
    }

    @Override
    void onSkipInRead(Throwable t) {
        chunkLevelState.get().onSkipInRead(t)
    }

    @Override
    void onSkipInWrite(Object item, Throwable t) {
        chunkLevelState.get().onSkipInWrite(item, t)
    }

    @Override
    void onSkipInProcess(Object item, Throwable t) {
        chunkLevelState.get().onSkipInProcess(item, t)
    }

    @Override
    void beforeStep(StepExecution stepExecution) {
        jobLevelState.beforeStep(stepExecution)
    }

    @Override
    ExitStatus afterStep(StepExecution stepExecution) {
        return jobLevelState.afterStep(stepExecution)
    }
}
