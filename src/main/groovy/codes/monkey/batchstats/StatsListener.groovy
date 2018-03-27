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
class StatsListener {

    private static Logger LOG = LoggerFactory.getLogger(StatsListener.class.name)

    private MetricRegistry metricRegistry
    private StatsNamespace namespace = new StatsNamespace()


    StatsListener(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry
        namespace = new StatsNamespace()
    }

    private Deque<Timer.Context> timerStack = new ArrayDeque<>()

    @BeforeJob
    void beforeJob(JobExecution jobExecution) {
        push(jobExecution.jobInstance.jobName)
    }

    @AfterJob
    void afterJob(JobExecution jobExecution) {
        pop()
        if(!timerStack.isEmpty()){
            throw new IllegalStateException("timer stack is not empty!")
        }
        Thread.sleep(6000) //nasty allow the metrics reporter 1 last chance to report.
    }

    @BeforeStep
    void beforeStep(StepExecution stepExecution) {
        push(stepExecution.stepName)
    }

    @AfterStep
    void afterStep(StepExecution stepExecution) {
        pop()
    }

    @BeforeChunk
    void beforeChunk(ChunkContext context) {
        push("chunk")
    }

    @AfterChunk
    void afterChunk(ChunkContext context) {
        popNullRead()
        pop()
    }

    @BeforeRead
    void beforeRead() {
        push("read")
    }


    @AfterRead
    void afterRead(Object item) {
        pop()
    }

    @BeforeProcess
    void beforeProcess(Object item) {
        popNullRead() // when chunk size is than configured, not enough reads left to fill the chunk. In this case
        // a beforeProcess is called after a beforeRead instead of an afterChunk after a beforeRead
        push("process")
    }

    @AfterProcess
    void afterProcess(Object item, Object result) {
        pop()
    }

    @BeforeWrite
    void beforeWrite(List<?> items) {
        push("write")
    }

    @AfterWrite
    void afterWrite(List<?> items) {
        pop()
    }

    private void push(String name) {
        namespace.push(name)
        timerStack.push(metricRegistry.timer(namespace.name()).time())
    }

    private void popNullRead() {
        if (namespace.leaf().equals("read")) {
            namespace.pop() //null read - nasty
            timerStack.pop() // null read timer
        }
    }

    private void pop() {
        namespace.pop()
        timerStack.pop().stop()
    }

}
