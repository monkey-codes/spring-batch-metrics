package codes.monkey.batchstats.eventdriven

import org.springframework.batch.item.ItemReader
import org.springframework.batch.item.NonTransientResourceException
import org.springframework.batch.item.ParseException
import org.springframework.batch.item.UnexpectedInputException

/**
 * @author Johan Zietsman (jzietsman@thoughtworks.com.au).
 */
class MutableListItemReader<T> implements ItemReader<T> {

    List<T> list

    MutableListItemReader(List<T> list) {
        this.list = list
    }

    @Override
    T read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        if (!list.isEmpty()) {
            return list.remove(0)
        }
        return null
    }
}