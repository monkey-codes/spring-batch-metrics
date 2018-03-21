package codes.monkey.batchstats

import com.codahale.metrics.ConsoleReporter
import com.codahale.metrics.MetricRegistry
import com.codahale.metrics.Slf4jReporter
import com.codahale.metrics.Timer
import com.codahale.metrics.jvm.CachedThreadStatesGaugeSet
import com.codahale.metrics.jvm.GarbageCollectorMetricSet
import com.codahale.metrics.jvm.MemoryUsageGaugeSet
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.batch.core.annotation.AfterRead
import org.springframework.batch.core.annotation.BeforeRead

import java.util.concurrent.TimeUnit

/**
 * @author Johan Zietsman (jzietsman@thoughtworks.com.au).
 */
class StatsListener {

    private static Logger LOG = LoggerFactory.getLogger(StatsListener.class.name)

    private MetricRegistry metricRegistry

    StatsListener(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry
    }
//    private static final MetricRegistry METRICS = new MetricRegistry()
//    private static final ConsoleReporter REPORTER = ConsoleReporter.forRegistry(METRICS)
//            .convertRatesTo(TimeUnit.SECONDS)
//            .convertDurationsTo(TimeUnit.MILLISECONDS)
//            .build()

//    private static final Slf4jReporter REPORTER = Slf4jReporter.forRegistry(METRICS)
//    .outputTo(LoggerFactory.getLogger(StatsListener.class.name))
//    .convertRatesTo(TimeUnit.SECONDS)
//    .convertDurationsTo(TimeUnit.MILLISECONDS)
//    .build()

//    static {
//                METRICS.register("gc", new GarbageCollectorMetricSet())
//                METRICS.register("threads", new CachedThreadStatesGaugeSet(10, TimeUnit.SECONDS))
//                METRICS.register("memory", new MemoryUsageGaugeSet())
//                REPORTER.start(5, TimeUnit.SECONDS )
//    }
    private Timer.Context readTimer

    @BeforeRead
    void beforeRead() {
        readTimer = metricRegistry.timer("read").time()
    }

    @AfterRead
    void afterRead(Object item) {
        readTimer.stop()
//        log("afterRead")
    }

//    @BeforeProcess
//    void beforeProcess(Object item) {
//
//        log("beforeProcess")
//    }
//
//    @AfterProcess
//    void afterProcess(Object item, Object result){
//        log("afterProcess")
//    }

    def log(message) {
        LOG.info(message)
    }
}
