package codes.monkey.batchstats.statemachine

import org.springframework.batch.core.ExitStatus
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.StepExecution
import org.springframework.batch.core.annotation.AfterChunk
import org.springframework.batch.core.annotation.AfterChunkError
import org.springframework.batch.core.annotation.AfterJob
import org.springframework.batch.core.annotation.AfterProcess
import org.springframework.batch.core.annotation.AfterRead
import org.springframework.batch.core.annotation.AfterStep
import org.springframework.batch.core.annotation.AfterWrite
import org.springframework.batch.core.annotation.BeforeChunk
import org.springframework.batch.core.annotation.BeforeJob
import org.springframework.batch.core.annotation.BeforeProcess
import org.springframework.batch.core.annotation.BeforeRead
import org.springframework.batch.core.annotation.BeforeStep
import org.springframework.batch.core.annotation.BeforeWrite
import org.springframework.batch.core.annotation.OnProcessError
import org.springframework.batch.core.annotation.OnReadError
import org.springframework.batch.core.annotation.OnSkipInProcess
import org.springframework.batch.core.annotation.OnSkipInRead
import org.springframework.batch.core.annotation.OnSkipInWrite
import org.springframework.batch.core.annotation.OnWriteError
import org.springframework.batch.core.scope.context.ChunkContext

/**
 * @author Johan Zietsman (jzietsman@thoughtworks.com.au).
 */
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