package codes.monkey.batchstats.eventdriven.statemachine

import org.springframework.batch.core.ExitStatus
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.StepExecution
import org.springframework.batch.core.annotation.*
import org.springframework.batch.core.scope.context.ChunkContext


interface BatchListener {


    @BeforeJob
    void beforeJob(JobExecution jobExecution)

    @AfterJob
    void afterJob(JobExecution jobExecution)

    @BeforeChunk
    void beforeChunk(ChunkContext context)

    @AfterChunk
    void afterChunk(ChunkContext context)

    @AfterChunkError
    void afterChunkError(ChunkContext context)

    @BeforeProcess
    void beforeProcess(Object item)

    @AfterProcess
    void afterProcess(Object item, Object result)

    @OnProcessError
    void onProcessError(Object item, Exception e)

    @BeforeRead
    void beforeRead()

    @AfterRead
    void afterRead(Object item)

    @OnReadError
    void onReadError(Exception ex)

    @BeforeWrite
    void beforeWrite(List items)

    @AfterWrite
    void afterWrite(List items)

    @OnWriteError
    void onWriteError(Exception exception, List items)

    @OnSkipInRead
    void onSkipInRead(Throwable t)

    @OnSkipInWrite
    void onSkipInWrite(Object item, Throwable t)

    @OnSkipInProcess
    void onSkipInProcess(Object item, Throwable t)

    @BeforeStep
    void beforeStep(StepExecution stepExecution)

    @AfterStep
    ExitStatus afterStep(StepExecution stepExecution)

}