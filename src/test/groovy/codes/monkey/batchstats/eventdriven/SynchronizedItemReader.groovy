package codes.monkey.batchstats.eventdriven

import org.springframework.batch.item.ItemReader
import org.springframework.batch.item.NonTransientResourceException
import org.springframework.batch.item.ParseException
import org.springframework.batch.item.UnexpectedInputException


class SynchronizedItemReader<T> implements ItemReader<T> {

    private ItemReader<T> delegate

    SynchronizedItemReader(ItemReader<T> delegate) {
        this.delegate = delegate
    }

    @Override
    synchronized T read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        return delegate.read()
    }
}
