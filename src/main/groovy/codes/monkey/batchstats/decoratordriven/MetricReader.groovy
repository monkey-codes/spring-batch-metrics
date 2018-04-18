package codes.monkey.batchstats.decoratordriven

import com.codahale.metrics.MetricRegistry
import org.springframework.batch.item.ItemReader
import org.springframework.batch.item.NonTransientResourceException
import org.springframework.batch.item.ParseException
import org.springframework.batch.item.UnexpectedInputException

/**
 * @author Johan Zietsman (jzietsman@thoughtworks.com.au).
 */
class MetricReader<T> implements ItemReader<T> {
    private ItemReader<T> delegate
    MetricRegistry metricRegistry

    MetricReader(ItemReader<T> itemReader, MetricRegistry metricRegistry) {
        this.delegate = itemReader
        this.metricRegistry = metricRegistry
    }

    @Override
    T read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        def timer = metricRegistry.timer("read").time()
        try {
            T result = delegate.read()
            if (result != null) timer.stop()
            result
        } catch (all) {
            metricRegistry.counter("read.error").inc()
            throw all
        }
    }
}
