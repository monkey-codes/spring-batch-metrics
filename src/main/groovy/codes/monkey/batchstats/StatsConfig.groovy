package codes.monkey.batchstats

import com.codahale.metrics.MetricRegistry
import com.codahale.metrics.Reporter
import com.codahale.metrics.ScheduledReporter
import com.codahale.metrics.Slf4jReporter
import com.codahale.metrics.jvm.CachedThreadStatesGaugeSet
import com.codahale.metrics.jvm.GarbageCollectorMetricSet
import com.codahale.metrics.jvm.MemoryUsageGaugeSet
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

import java.util.concurrent.TimeUnit

/**
 * @author Johan Zietsman (jzietsman@thoughtworks.com.au).
 */
@Configuration
class StatsConfig {

    @Bean
    MetricRegistry metricRegistry(){
        return new MetricRegistry().with {
//            register("gc", new GarbageCollectorMetricSet())
//            register("threads", new CachedThreadStatesGaugeSet(10, TimeUnit.SECONDS))
//            register("memory", new MemoryUsageGaugeSet())
            it
        }
    }

    @Bean
    ScheduledReporter reporter(MetricRegistry metricRegistry){
        def reporter = Slf4jReporter.forRegistry(metricRegistry)
                .outputTo(LoggerFactory.getLogger(StatsListener.class.name))
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build()
        reporter.start(5, TimeUnit.SECONDS)
        reporter
    }


}
