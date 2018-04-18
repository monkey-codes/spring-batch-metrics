package codes.monkey.batchstats.decoratordriven

import com.codahale.metrics.MetricRegistry
import org.springframework.batch.item.ItemProcessor

/**
 * @author Johan Zietsman (jzietsman@thoughtworks.com.au).
 */
class MetricProcessor<I, O> implements ItemProcessor<I, O> {
    private ItemProcessor<I, O> delegate
    private MetricRegistry metricRegistry

    MetricProcessor(ItemProcessor<I, O> processor, MetricRegistry metricRegistry) {
        this.delegate = processor
        this.metricRegistry = metricRegistry
    }

    @Override
    O process(I item) throws Exception {
        def timer = metricRegistry.timer("process").time()
        try {

            O result = delegate.process(item)
            timer.stop()
            result
        }catch (all){
            metricRegistry.counter("process.error").inc()
            throw all
        }
    }
}
