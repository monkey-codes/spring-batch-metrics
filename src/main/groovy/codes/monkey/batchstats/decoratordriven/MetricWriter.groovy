package codes.monkey.batchstats.decoratordriven

import com.codahale.metrics.MetricRegistry
import org.springframework.batch.item.ItemWriter

/**
 * @author Johan Zietsman (jzietsman@thoughtworks.com.au).
 */
class MetricWriter<T> implements ItemWriter<T> {
    private ItemWriter<T> delegate
    private MetricRegistry metricRegistry

    MetricWriter(ItemWriter<T> interceptingItemWriter, MetricRegistry metricRegistry) {
        this.delegate = interceptingItemWriter
        this.metricRegistry = metricRegistry
    }

    @Override
    void write(List<? extends T> items) throws Exception {
        def timer = metricRegistry.timer("write").time()
        try{

            delegate.write(items)
            timer.stop()
        }catch(all){
            metricRegistry.counter("write.error").inc()
            throw all
        }
    }
}
