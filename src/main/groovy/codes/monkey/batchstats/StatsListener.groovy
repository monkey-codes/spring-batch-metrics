package codes.monkey.batchstats

import com.codahale.metrics.MetricRegistry
import com.codahale.metrics.Timer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.JobExecutionListener
import org.springframework.batch.core.StepExecution
import org.springframework.batch.core.annotation.*
import org.springframework.batch.core.scope.context.ChunkContext


/**
 * @author Johan Zietsman (jzietsman@thoughtworks.com.au).
 */
class StatsListener{

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
        timerStack.push(metricRegistry.timer(namespace.push(jobExecution.jobInstance.jobName).name()).time())

    }

    @AfterJob
    void afterJob(JobExecution jobExecution) {
        namespace.pop()
        timerStack.pop().stop()
        Thread.sleep(6000) //nasty allow the metrics reporter 1 last chance to report.
    }

    @BeforeStep
    void beforeStep(StepExecution stepExecution) {
        namespace.push(stepExecution.stepName)
        timerStack.push(metricRegistry.timer(namespace.name()).time())
    }

    @AfterStep
    void afterStep(StepExecution stepExecution){

        namespace.pop() //step
        timerStack.pop().stop()
    }

    @BeforeChunk
    void beforeChunk(ChunkContext context){
        namespace.push("chunk")
        timerStack.push(metricRegistry.timer(namespace.name()).time())
        LOG.info("beforeChunk")
    }

    @AfterChunk
    void afterChunk(ChunkContext context){
        popNullRead()
        namespace.pop()
        timerStack.pop().stop()
        LOG.info("afterChunk")
    }

    private void popNullRead() {
        if(namespace.leaf().equals("read")){
            namespace.pop() //null read - nasty
            timerStack.pop() // null read timer
        }
    }


    @BeforeRead
    void beforeRead() {
        namespace.push("read")
        timerStack.push(metricRegistry.timer(namespace.name()).time())
    }

    @AfterRead
    void afterRead(Object item) {
        namespace.pop()
        timerStack.pop().stop()
    }

    @BeforeProcess
    void beforeProcess(Object item) {
        namespace.push("process")
        timerStack.push(metricRegistry.timer(namespace.name()).time())
    }

    @AfterProcess
    void afterProcess(Object item, Object result){
        namespace.pop()
        timerStack.pop().stop()
    }

    @BeforeWrite
    void beforeWrite(List<?> items){
        namespace.push("write")
        timerStack.push(metricRegistry.timer(namespace.name()).time())
    }

    @AfterWrite
    void afterWrite(List<?> items){
        namespace.pop()
        timerStack.pop().stop()
    }

}
