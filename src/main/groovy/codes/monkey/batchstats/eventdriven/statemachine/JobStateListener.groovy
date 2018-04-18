package codes.monkey.batchstats.eventdriven.statemachine
/**
 * @author Johan Zietsman (jzietsman@thoughtworks.com.au).
 */
interface JobStateListener extends BatchListener {

    void afterLastRead()

    void beforeChunkWriteErrorReProcess()

    void afterChunkWriteErrorReProcess()
}