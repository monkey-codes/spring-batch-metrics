package codes.monkey.batchstats

import codes.monkey.batchstats.statemachine.JobStateListener
import com.codahale.metrics.MetricRegistry
import com.codahale.metrics.Timer
import org.springframework.batch.core.ExitStatus
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.StepExecution
import org.springframework.batch.core.scope.context.ChunkContext

/**
 * @author Johan Zietsman (jzietsman@thoughtworks.com.au).
 */
class StatsListener implements JobStateListener {

    private MetricRegistry metricRegistry
    private StatsNamespace namespace = new StatsNamespace()
    def afterJobCallback


    StatsListener(MetricRegistry metricRegistry, Closure afterJobCallback) {
        this(metricRegistry, new StatsNamespace(), afterJobCallback)
    }

    StatsListener(MetricRegistry metricRegistry, StatsNamespace namespace, Closure afterJobCallback) {
        this.metricRegistry = metricRegistry
        this.afterJobCallback = afterJobCallback
        this.namespace = namespace
    }

    private Deque<Timer.Context> timerStack = new ArrayDeque<>()

    private void push(String name) {
        namespace.push(name)
        timerStack.push(metricRegistry.timer(namespace.name()).time())
    }

    private void pop() {
        namespace.pop()
        timerStack.pop().stop()
    }

    private void discardLastCounter() {
        namespace.pop()
        timerStack.pop()
    }

    @Override
    void afterLastRead() {
        discardLastCounter()
    }

    @Override
    void beforeChunkWriteErrorReProcess() {
        push('reprocess')
    }

    @Override
    void afterChunkWriteErrorReProcess() {
        pop()
    }

    @Override
    void beforeJob(JobExecution jobExecution) {
        push(jobExecution.jobInstance.jobName)
    }

    @Override
    void afterJob(JobExecution jobExecution) {
        pop()
        if (!timerStack.isEmpty()) {
            throw new IllegalStateException("timer stack is not empty!")
        }
        afterJobCallback(jobExecution)
    }

    @Override
    void beforeChunk(ChunkContext context) {
        push('chunk')
    }

    @Override
    void afterChunk(ChunkContext context) {
        pop()
    }

    @Override
    void afterChunkError(ChunkContext context) {
        metricRegistry.counter("${namespace.name()}.error").inc()
        discardLastCounter()
    }

    @Override
    void beforeProcess(Object item) {
        push('process')
    }

    @Override
    void afterProcess(Object item, Object result) {
        pop()
    }

    @Override
    void onProcessError(Object item, Exception e) {
        metricRegistry.counter("${namespace.name()}.error").inc()
        //pop process
        discardLastCounter()
    }

    @Override
    void beforeRead() {
        push('read')
    }

    @Override
    void afterRead(Object item) {
        pop()
    }

    @Override
    void onReadError(Exception ex) {
        metricRegistry.counter("${namespace.name()}.error").inc()
        //pop read
        discardLastCounter()
    }

    @Override
    void beforeWrite(List items) {
        push('write')
    }

    @Override
    void afterWrite(List items) {
        pop()
    }

    @Override
    void onWriteError(Exception exception, List items) {
        metricRegistry.counter("${namespace.name()}.error").inc()
        //pop write
        discardLastCounter()
    }

    @Override
    void onSkipInRead(Throwable t) {
        metricRegistry.counter("${namespace.name()}.read.skip").inc()
    }

    @Override
    void onSkipInWrite(Object item, Throwable t) {
        metricRegistry.counter("${namespace.name()}.write.skip").inc()
    }

    @Override
    void onSkipInProcess(Object item, Throwable t) {
        metricRegistry.counter("${namespace.name()}.process.skip").inc()
    }

    @Override
    void beforeStep(StepExecution stepExecution) {
        push(stepExecution.stepName)
    }

    @Override
    ExitStatus afterStep(StepExecution stepExecution) {
        pop()
        return stepExecution.exitStatus
    }
}
