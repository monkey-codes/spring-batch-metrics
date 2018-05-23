package codes.monkey.batchstats.decoratordriven

import codes.monkey.batchstats.StatsNamespace
import com.codahale.metrics.MetricRegistry
import org.springframework.batch.item.ItemProcessor


class MetricProcessor<I, O> implements ItemProcessor<I, O> {
    private ItemProcessor<I, O> delegate
    private MetricRegistry metricRegistry
    private StatsNamespace namespace

    MetricProcessor(ItemProcessor<I, O> processor, MetricRegistry metricRegistry) {
        this(processor, metricRegistry, new StatsNamespace())
    }

    MetricProcessor(ItemProcessor<I, O> processor, MetricRegistry metricRegistry, StatsNamespace parent) {
        this.delegate = processor
        this.metricRegistry = metricRegistry
        this.namespace = new StatsNamespace(parent)
    }

    @Override
    O process(I item) throws Exception {
        namespace.push("process")
        def timer = metricRegistry.timer(namespace.name()).time()
        try {
            O result = delegate.process(item)
            timer.stop()
            result
        } catch (all) {
            metricRegistry.counter("${namespace.name()}.error").inc()
            throw all
        } finally {
            namespace.pop()
        }
    }
}
