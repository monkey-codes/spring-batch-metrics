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
class ParallelProcessingStatsListener {

    private static Logger LOG = LoggerFactory.getLogger(ParallelProcessingStatsListener.class.name)

    private MetricRegistry metricRegistry
    private volatile StatsNamespace namespace = new StatsNamespace()


    ParallelProcessingStatsListener(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry
//        namespace = new StatsNamespace()
    }

    private Deque<Timer.Context> timerStack = new ArrayDeque<>()
    private ThreadLocal<Deque<Timer.Context>> threadTimerStack = ThreadLocal.withInitial({new ArrayDeque<>()})
    private ThreadLocal<StatsNamespace> threadNamespace = ThreadLocal.withInitial({new StatsNamespace(namespace)})

    @BeforeJob
    void beforeJob(JobExecution jobExecution) {
        timerStack.push(metricRegistry.timer(namespace.push(jobExecution.jobInstance.jobName).name()).time())

    }

    @AfterJob
    void afterJob(JobExecution jobExecution) {
        namespace.pop()
        def stop = timerStack.pop().stop()
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
//        namespace.push("chunk")
//        timerStack.push(metricRegistry.timer(namespace.name()).time())
        def tns = threadNamespace.get()
        tns.push("chunk")
        threadTimerStack.get().push(metricRegistry.timer(tns.name()).time())
        LOG.info("beforeChunk")
    }

    @AfterChunk
    void afterChunk(ChunkContext context){
        popNullRead()
        threadNamespace.get().pop()
        threadTimerStack.get().pop().stop()
//        namespace.pop()
//        timerStack.pop().stop()
        LOG.info("afterChunk")
    }

    private void popNullRead() {
        if(threadNamespace.get().leaf().equals("read")){
            threadNamespace.get().pop() //null read - nasty
            threadTimerStack.get().pop() // null read timer
        }
    }


    @BeforeRead
    void beforeRead() {
        def tns = threadNamespace.get()
        tns.push("read")
        threadTimerStack.get().push(metricRegistry.timer(tns.name()).time())
    }

    @AfterRead
    void afterRead(Object item) {
        threadNamespace.get().pop()
        threadTimerStack.get().pop().stop()
    }

    @BeforeProcess
    void beforeProcess(Object item) {
        def tns = threadNamespace.get()
        tns.push("process")
        threadTimerStack.get().push(metricRegistry.timer(tns.name()).time())
    }

    @AfterProcess
    void afterProcess(Object item, Object result){
        threadNamespace.get().pop()
        threadTimerStack.get().pop().stop()
    }

}
