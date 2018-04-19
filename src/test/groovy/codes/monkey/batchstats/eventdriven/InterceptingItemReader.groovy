package codes.monkey.batchstats.eventdriven

import org.springframework.batch.item.ItemReader
import org.springframework.batch.item.NonTransientResourceException
import org.springframework.batch.item.ParseException
import org.springframework.batch.item.UnexpectedInputException

/**
 * @author Johan Zietsman (jzietsman@thoughtworks.com.au).
 */
class InterceptingItemReader<T> implements ItemReader<T> {

    private ItemReader<T> delegate
    def transform = { it }

    InterceptingItemReader(ItemReader<T> delegate) {
        this.delegate = delegate
    }

    @Override
    T read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        return transform(delegate.read())
    }
}
