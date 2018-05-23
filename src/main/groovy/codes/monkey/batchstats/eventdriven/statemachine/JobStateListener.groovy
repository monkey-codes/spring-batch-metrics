package codes.monkey.batchstats.eventdriven.statemachine

interface JobStateListener extends BatchListener {

    void afterLastRead()

    void beforeChunkWriteErrorReProcess()

    void afterChunkWriteErrorReProcess()
}