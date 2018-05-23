package codes.monkey.batchstats.eventdriven

import org.springframework.batch.item.ItemWriter


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
