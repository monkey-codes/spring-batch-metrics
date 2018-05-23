package codes.monkey.batchstats.decoratordriven

import codes.monkey.batchstats.StatsNamespace
import com.codahale.metrics.MetricRegistry
import org.springframework.batch.item.ItemWriter


class MetricWriter<T> implements ItemWriter<T> {
    private ItemWriter<T> delegate
    private MetricRegistry metricRegistry
    private StatsNamespace namespace

    MetricWriter(ItemWriter<T> writer, MetricRegistry metricRegistry) {
        this(writer, metricRegistry, new StatsNamespace())
    }

    MetricWriter(ItemWriter<T> writer, MetricRegistry metricRegistry, StatsNamespace parent) {
        this.delegate = writer
        this.metricRegistry = metricRegistry
        this.namespace = new StatsNamespace(parent)
    }

    @Override
    void write(List<? extends T> items) throws Exception {
        namespace.push("write")
        def timer = metricRegistry.timer(namespace.name()).time()
        try {

            delegate.write(items)
            timer.stop()
        } catch (all) {
            metricRegistry.counter("${namespace.name()}.error").inc()
            throw all
        } finally {
            namespace.pop()
        }
    }
}
