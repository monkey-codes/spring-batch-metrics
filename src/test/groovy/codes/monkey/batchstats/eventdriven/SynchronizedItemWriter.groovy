package codes.monkey.batchstats.eventdriven

import org.springframework.batch.item.ItemWriter

/**
 * @author Johan Zietsman (jzietsman@thoughtworks.com.au).
 */
class SynchronizedItemWriter<T> implements ItemWriter<T> {

    private ItemWriter<T> delegate

    SynchronizedItemWriter(ItemWriter<T> delegate) {
        this.delegate = delegate
    }

    @Override
    synchronized void write(List<? extends T> items) throws Exception {
        delegate.write(items)
    }
}
