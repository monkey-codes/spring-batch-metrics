package codes.monkey.batchstats

import codes.monkey.batchstats.eventdriven.StatsListener
import com.codahale.metrics.MetricRegistry
import com.codahale.metrics.ScheduledReporter
import com.codahale.metrics.Slf4jReporter
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

import java.util.concurrent.TimeUnit


@Configuration
class StatsConfig {

    @Bean
    MetricRegistry metricRegistry() {
        return new MetricRegistry().with {
//            register("gc", new GarbageCollectorMetricSet())
//            register("threads", new CachedThreadStatesGaugeSet(10, TimeUnit.SECONDS))
//            register("memory", new MemoryUsageGaugeSet())
            it
        }
    }

    @Bean
    ScheduledReporter reporter(MetricRegistry metricRegistry) {
        def reporter = Slf4jReporter.forRegistry(metricRegistry)
                .outputTo(LoggerFactory.getLogger(StatsListener.class.name))
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build()
        reporter.start(5, TimeUnit.SECONDS)
        reporter
    }


}
