package codes.monkey.batchstats.eventdriven

import org.springframework.batch.item.ItemProcessor

/**
 * @author Johan Zietsman (jzietsman@thoughtworks.com.au).
 */
class InterceptingItemProcessor<I, O> implements ItemProcessor<I, O> {

    private ItemProcessor<I, O> delegate

    def transform = { it }

    InterceptingItemProcessor(ItemProcessor<I, O> delegate) {
        this.delegate = delegate
    }

    @Override
    O process(I item) throws Exception {
        return transform(delegate.process(item))
    }
}
