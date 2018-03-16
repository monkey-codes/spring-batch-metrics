package codes.monkey.batchstats

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.batch.core.annotation.AfterProcess
import org.springframework.batch.core.annotation.AfterRead
import org.springframework.batch.core.annotation.BeforeProcess
import org.springframework.batch.core.annotation.BeforeRead

/**
 * @author Johan Zietsman (jzietsman@thoughtworks.com.au).
 */
class StatsListener {

    private static Logger LOG = LoggerFactory.getLogger(StatsListener.class.name)

    @BeforeRead
    void beforeRead() {
        log("beforeRead")
    }

    @AfterRead
    void afterRead(Object item) {
        log("afterRead")
    }

    @BeforeProcess
    void beforeProcess(Object item) {

        log("beforeProcess")
    }

    @AfterProcess
    void afterProcess(Object item, Object result){
        log("afterProcess")
    }

    def log(message) {
        LOG.info(message)
    }
}
