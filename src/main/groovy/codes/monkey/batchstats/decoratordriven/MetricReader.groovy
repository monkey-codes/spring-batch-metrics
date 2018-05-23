package codes.monkey.batchstats.decoratordriven

import codes.monkey.batchstats.StatsNamespace
import com.codahale.metrics.MetricRegistry
import org.springframework.batch.item.ItemReader
import org.springframework.batch.item.NonTransientResourceException
import org.springframework.batch.item.ParseException
import org.springframework.batch.item.UnexpectedInputException


class MetricReader<T> implements ItemReader<T> {
    private ItemReader<T> delegate
    MetricRegistry metricRegistry
    StatsNamespace namespace

    MetricReader(ItemReader<T> itemReader, MetricRegistry metricRegistry) {
        this(itemReader, metricRegistry, new StatsNamespace())
    }

    MetricReader(ItemReader<T> itemReader, MetricRegistry metricRegistry, StatsNamespace parent) {
        this.delegate = itemReader
        this.metricRegistry = metricRegistry
        this.namespace = new StatsNamespace(parent)
    }

    @Override
    T read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        namespace.push("read")
        def timer = metricRegistry.timer(namespace.name()).time()
        try {
            T result = delegate.read()
            if (result != null) timer.stop()
            result
        } catch (all) {
            metricRegistry.counter("${namespace}.error").inc()
            throw all
        } finally {
            namespace.pop()
        }
    }
}
