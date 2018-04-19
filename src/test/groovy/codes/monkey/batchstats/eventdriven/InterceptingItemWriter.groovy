package codes.monkey.batchstats.eventdriven

import org.springframework.batch.item.ItemWriter

/**
 * @author Johan Zietsman (jzietsman@thoughtworks.com.au).
 */
class InterceptingItemWriter<T> implements ItemWriter<T> {

    private ItemWriter<T> delegate

    def transform = { it }


    InterceptingItemWriter(ItemWriter<T> delegate) {
        this.delegate = delegate
    }

    @Override
    void write(List<? extends T> items) throws Exception {
        delegate.write(transform(items))
    }
}
