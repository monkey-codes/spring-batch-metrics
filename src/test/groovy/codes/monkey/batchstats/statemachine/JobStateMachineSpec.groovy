package codes.monkey.batchstats.statemachine

import spock.lang.Specification

/**
 * @author Johan Zietsman (jzietsman@thoughtworks.com.au).
 */
class JobStateMachineSpec extends Specification {

    JobStateListener jobStateListener = Mock()

    def "it should notify listener of null read - items divisible by chunk size"() {
        given:
        JobStateMachine stateMachine = JobStateMachine.idle(jobStateListener)

        when:
        play(stateMachine, ['beforeJob', 'beforeStep', 'beforeChunk', 'beforeRead', 'afterChunk'])

        //multiple then blocks enforces the order of expectations
        // @formatter:off
        then: 1 * jobStateListener.beforeJob(_)
        then: 1 * jobStateListener.beforeStep(_)
        then: 1 * jobStateListener.beforeChunk(_)
        then: 1 * jobStateListener.beforeRead()
        then: 1 * jobStateListener.afterLastRead()
        then: 1 * jobStateListener.afterChunk(_)
        // @formatter:on

    }

    def "it should notify listener of null read - items not divisible by chunk size"() {
        given:
        JobStateMachine stateMachine = JobStateMachine.idle(jobStateListener)

        when:
        play(stateMachine, ['beforeJob', 'beforeStep', 'beforeChunk', 'beforeRead', 'afterRead', 'beforeRead',
                            'beforeProcess'])

        //multiple then blocks enforces the order of expectations
        // @formatter:off
        then: 1 * jobStateListener.beforeJob(_)
        then: 1 * jobStateListener.beforeStep(_)
        then: 1 * jobStateListener.beforeChunk(_)
        then: 1 * jobStateListener.beforeRead()
        then: 1 * jobStateListener.afterRead(_)
        then: 1 * jobStateListener.beforeRead()
        then: 1 * jobStateListener.afterLastRead()
        then: 1 * jobStateListener.beforeProcess(_)
        // @formatter:on

    }

    def "it should notify listener of read error"() {
        given:
        JobStateMachine stateMachine = JobStateMachine.idle(jobStateListener)

        when:
        play(stateMachine, ['beforeJob', 'beforeStep', 'beforeChunk', 'beforeRead', 'onReadError', 'beforeRead',
                            'afterRead', 'beforeProcess', 'afterProcess', 'beforeWrite', 'afterWrite', 'onSkipInRead',
                            'afterChunk', 'beforeChunk', 'beforeRead', 'afterChunk', 'afterStep', 'afterJob']
        )

        //multiple then blocks enforces the order of expectations
        // @formatter:off
        then: 1 * jobStateListener.beforeJob(*_)
        then: 1 * jobStateListener.beforeStep(*_)
        then: 1 * jobStateListener.beforeChunk(*_)
        then: 1 * jobStateListener.beforeRead()
        then: 1 * jobStateListener.onReadError(*_)
        then: 1 * jobStateListener.beforeRead()
        then: 1 * jobStateListener.afterRead(*_)
        then: 1 * jobStateListener.beforeProcess(*_)
        then: 1 * jobStateListener.afterProcess(*_)
        then: 1 * jobStateListener.beforeWrite(*_)
        then: 1 * jobStateListener.afterWrite(*_)
        then: 1 * jobStateListener.onSkipInRead(*_)
        then: 1 * jobStateListener.afterChunk(*_)
        then: 1 * jobStateListener.beforeChunk(*_)
        then: 1 * jobStateListener.beforeRead(*_)
        then: 1 * jobStateListener.afterLastRead()
        then: 1 * jobStateListener.afterChunk(*_)
        then: 1 * jobStateListener.afterStep(*_)
        then: 1 * jobStateListener.afterJob(*_)
//        then:
//        _ * _
        // @formatter:on
    }

    def "it should notify listener of process error - chunk size 1"() {
        given:
        JobStateMachine stateMachine = JobStateMachine.idle(jobStateListener)

        when:
        play(stateMachine, ['beforeJob', 'beforeStep', 'beforeChunk', 'beforeRead', 'afterRead', 'beforeProcess',
                            'onProcessError', 'afterChunkError', 'beforeChunk', 'beforeWrite', 'afterWrite',
                            'onSkipInProcess', 'afterChunk', 'beforeChunk', 'beforeRead', 'afterRead', 'beforeProcess',
                            'afterProcess', 'beforeWrite', 'afterWrite', 'afterChunk', 'beforeChunk', 'beforeRead',
                            'afterChunk', 'afterStep', 'afterJob']

        )

        //multiple then blocks enforces the order of expectations
        // @formatter:off
        then: 1 * jobStateListener.beforeJob(*_)
        then: 1 * jobStateListener.beforeStep(*_)
        then: 1 * jobStateListener.beforeChunk(*_)
        then: 1 * jobStateListener.beforeRead()
        then: 1 * jobStateListener.afterRead(*_)
        then: 1 * jobStateListener.beforeProcess(*_)
        then: 1 * jobStateListener.onProcessError(*_)
        then: 1 * jobStateListener.afterChunkError(*_)
        then: 1 * jobStateListener.beforeChunk(*_)
        then: 1 * jobStateListener.beforeChunkWriteErrorReProcess()
        then: 0 * jobStateListener.beforeWrite(*_)
        then: 0 * jobStateListener.afterWrite(*_)
        then: 1 * jobStateListener.onSkipInProcess(*_)
        then: 1 * jobStateListener.afterChunkWriteErrorReProcess()
        then: 1 * jobStateListener.afterChunk(*_)
        then: 1 * jobStateListener.beforeChunk(*_)
        then: 1 * jobStateListener.beforeRead()
        then: 1 * jobStateListener.afterRead(*_)
        then: 1 * jobStateListener.beforeProcess(*_)
        then: 1 * jobStateListener.afterProcess(*_)
        then: 1 * jobStateListener.beforeWrite(*_)
        then: 1 * jobStateListener.afterWrite(*_)
        then: 1 * jobStateListener.afterChunk(*_) //<-- HERE
        then: 1 * jobStateListener.beforeChunk(*_)
        then: 1 * jobStateListener.beforeRead(*_)
        then: 1 * jobStateListener.afterLastRead()
        then: 1 * jobStateListener.afterChunk(*_)
        then: 1 * jobStateListener.afterStep(*_)
        then: 1 * jobStateListener.afterJob(*_)
        // @formatter:on
    }

    def "it should notify listener of process error - chunk size > 1"() {
        given:
        JobStateMachine stateMachine = JobStateMachine.idle(jobStateListener)

        when:
        play(stateMachine, ['beforeJob', 'beforeStep', 'beforeChunk', 'beforeRead', 'afterRead', 'beforeRead',
                            'afterRead', 'beforeProcess', 'onProcessError', 'afterChunkError', 'beforeChunk',
                            'beforeProcess', 'afterProcess', 'beforeWrite', 'afterWrite', 'onSkipInProcess',
                            'afterChunk', 'beforeChunk', 'beforeRead', 'afterRead', 'beforeRead', 'afterRead',
                            'beforeProcess', 'afterProcess', 'beforeProcess', 'afterProcess', 'beforeWrite',
                            'afterWrite', 'afterChunk', 'beforeChunk', 'beforeRead', 'afterRead', 'beforeRead',
                            'beforeProcess', 'afterProcess', 'beforeWrite', 'afterWrite', 'afterChunk', 'afterStep',
                            'afterJob']

        )

        //multiple then blocks enforces the order of expectations
        // @formatter:off
        then: 1 * jobStateListener.beforeJob(*_)
        then: 1 * jobStateListener.beforeStep(*_)
        then: 1 * jobStateListener.beforeChunk(*_)
        then: 1 * jobStateListener.beforeRead(*_)
        then: 1 * jobStateListener.afterRead(*_)
        then: 1 * jobStateListener.beforeRead(*_)
        then: 1 * jobStateListener.afterRead(*_)
        then: 1 * jobStateListener.beforeProcess(*_)
        then: 1 * jobStateListener.onProcessError(*_)
        then: 1 * jobStateListener.afterChunkError(*_)
        then: 1 * jobStateListener.beforeChunk(*_)
        then: 1 * jobStateListener.beforeChunkWriteErrorReProcess()
        then: 0 * jobStateListener.beforeProcess(*_)
        then: 0 * jobStateListener.afterProcess(*_)
        then: 0 * jobStateListener.beforeWrite(*_)
        then: 0 * jobStateListener.afterWrite(*_)
        then: 1 * jobStateListener.onSkipInProcess(*_)
        then: 1 * jobStateListener.afterChunkWriteErrorReProcess()
        then: 1 * jobStateListener.afterChunk(*_)
        then: 1 * jobStateListener.beforeChunk(*_)
        then: 1 * jobStateListener.beforeRead(*_)
        then: 1 * jobStateListener.afterRead(*_)
        then: 1 * jobStateListener.beforeRead(*_)
        then: 1 * jobStateListener.afterRead(*_)
        then: 1 * jobStateListener.beforeProcess(*_)
        then: 1 * jobStateListener.afterProcess(*_)
        then: 1 * jobStateListener.beforeProcess(*_)
        then: 1 * jobStateListener.afterProcess(*_)
        then: 1 * jobStateListener.beforeWrite(*_)
        then: 1 * jobStateListener.afterWrite(*_)
        then: 1 * jobStateListener.afterChunk(*_)
        then: 1 * jobStateListener.beforeChunk(*_)
        then: 1 * jobStateListener.beforeRead(*_)
        then: 1 * jobStateListener.afterRead(*_)
        then: 1 * jobStateListener.beforeRead(*_)
        then: 1 * jobStateListener.beforeProcess(*_)
        then: 1 * jobStateListener.afterProcess(*_)
        then: 1 * jobStateListener.beforeWrite(*_)
        then: 1 * jobStateListener.afterWrite(*_)
        then: 1 * jobStateListener.afterChunk(*_)
        then: 1 * jobStateListener.afterStep(*_)
        then: 1 * jobStateListener.afterJob(*_)
        // @formatter:on
    }

    def "it should notify listener of write error"() {
        given:
        JobStateMachine stateMachine = JobStateMachine.idle(jobStateListener)

        when:
        play(stateMachine, ['beforeJob', 'beforeStep', 'beforeChunk', 'beforeRead', 'afterRead', 'beforeRead',
                            'afterRead', 'beforeProcess', 'afterProcess', 'beforeProcess', 'afterProcess',
                            'beforeWrite', 'onWriteError', 'afterChunkError', 'beforeChunk', 'beforeProcess',
                            'afterProcess', 'onWriteError', 'afterChunkError', 'beforeChunk', 'beforeProcess',
                            'afterProcess', 'afterWrite', 'onSkipInWrite', 'afterChunk', 'beforeChunk', 'beforeRead',
                            'afterRead', 'beforeRead', 'afterRead', 'beforeProcess', 'afterProcess', 'beforeProcess',
                            'afterProcess', 'beforeWrite', 'afterWrite', 'afterChunk', 'beforeChunk', 'beforeRead',
                            'afterChunk', 'afterStep', 'afterJob']
        )

        //multiple then blocks enforces the order of expectations
        // @formatter:off
        then: 1 * jobStateListener.beforeJob(*_)
        then: 1 * jobStateListener.beforeStep(*_)
        then: 1 * jobStateListener.beforeChunk(*_)
        then: 1 * jobStateListener.beforeRead(*_)
        then: 1 * jobStateListener.afterRead(*_)
        then: 1 * jobStateListener.beforeRead(*_)
        then: 1 * jobStateListener.afterRead(*_)
        then: 1 * jobStateListener.beforeProcess(*_)
        then: 1 * jobStateListener.afterProcess(*_)
        then: 1 * jobStateListener.beforeProcess(*_)
        then: 1 * jobStateListener.afterProcess(*_)
        then: 1 * jobStateListener.beforeWrite(*_)
        then: 1 * jobStateListener.onWriteError(*_)
        then: 1 * jobStateListener.afterChunkError(*_)
        then: 1 * jobStateListener.beforeChunk(*_)
        then: 1 * jobStateListener.beforeChunkWriteErrorReProcess()
        // I intend to swallow these events.

        //remember to record last chunk params to re emit on read

        then: 0 * jobStateListener.beforeProcess(*_)
        then: 0 * jobStateListener.afterProcess(*_)
        then: 0 * jobStateListener.beforeWrite(*_)
        then: 0 * jobStateListener.onWriteError(*_)
//        then: 1 * jobStateListener.onSkipInWrite(*_)
        then: 1 * jobStateListener.afterChunkWriteErrorReProcess()
        then: 1 * jobStateListener.afterChunkError(*_)
        then: 1 * jobStateListener.beforeChunk(*_)
        then: 1 * jobStateListener.beforeChunkWriteErrorReProcess()

        then: 0 * jobStateListener.beforeProcess(*_)
        then: 0 * jobStateListener.afterProcess(*_)
        then: 0 * jobStateListener.beforeWrite(*_)
        then: 0 * jobStateListener.afterWrite(*_)

        then: 1 * jobStateListener.onSkipInWrite(*_)
        then: 1 * jobStateListener.afterChunkWriteErrorReProcess()
        then: 1 * jobStateListener.afterChunk(*_)
        then: 1 * jobStateListener.beforeChunk(*_)
        then: 1 * jobStateListener.beforeRead(*_) //<-- this marks the start of a new chunk read and the end or re process
        then: 1 * jobStateListener.afterRead(*_)
        then: 1 * jobStateListener.beforeRead(*_)
        then: 1 * jobStateListener.afterRead(*_)
        then: 1 * jobStateListener.beforeProcess(*_)
        then: 1 * jobStateListener.afterProcess(*_)
        then: 1 * jobStateListener.beforeProcess(*_)
        then: 1 * jobStateListener.afterProcess(*_)
        then: 1 * jobStateListener.beforeWrite(*_)
        then: 1 * jobStateListener.afterWrite(*_)
        then: 1 * jobStateListener.afterChunk(*_)
        then: 1 * jobStateListener.beforeChunk(*_)
        then: 1 * jobStateListener.beforeRead(*_)
        then: 1 * jobStateListener.afterChunk(*_)
        then: 1 * jobStateListener.afterStep(*_)
        then: 1 * jobStateListener.afterJob(*_)
        // @formatter:on
    }

    def "it should notify listener of write error - when error is on last chunk"() {
        given:
        JobStateMachine stateMachine = JobStateMachine.idle(jobStateListener)

        when:
        play(stateMachine, ['beforeJob', 'beforeStep', 'beforeChunk', 'beforeRead', 'afterRead', 'beforeRead',
                            'afterRead', 'beforeRead', 'beforeProcess', 'afterProcess', 'beforeProcess',
                            'afterProcess', 'beforeWrite', 'onWriteError', 'afterChunkError', 'beforeChunk',
                            'beforeProcess', 'afterProcess', 'onWriteError', 'afterChunkError', 'beforeChunk',
                            'beforeProcess', 'afterProcess', 'afterWrite', 'onSkipInWrite', 'afterChunk', 'afterStep',
                            'afterJob']

        )

        //multiple then blocks enforces the order of expectations
        // @formatter:off
        then: 1 * jobStateListener.beforeJob(*_)
        then: 1 * jobStateListener.beforeStep(*_)
        then: 1 * jobStateListener.beforeChunk(*_)
        then: 1 * jobStateListener.beforeRead(*_)
        then: 1 * jobStateListener.afterRead(*_)
        then: 1 * jobStateListener.beforeRead(*_)
        then: 1 * jobStateListener.afterRead(*_)
        then: 1 * jobStateListener.beforeRead(*_)
        then: 1 * jobStateListener.beforeProcess(*_)
        then: 1 * jobStateListener.afterProcess(*_)
        then: 1 * jobStateListener.beforeProcess(*_)
        then: 1 * jobStateListener.afterProcess(*_)
        then: 1 * jobStateListener.beforeWrite(*_)
        then: 1 * jobStateListener.onWriteError(*_)
        then: 1 * jobStateListener.afterChunkError(*_)
        then: 1 * jobStateListener.beforeChunk(*_)
        then: 1 * jobStateListener.beforeChunkWriteErrorReProcess()
        then: 0 * jobStateListener.beforeProcess(*_)
        then: 0 * jobStateListener.afterProcess(*_)
        then: 0 * jobStateListener.beforeWrite(*_)
        then: 0 * jobStateListener.onWriteError(*_)
        then: 1 * jobStateListener.afterChunkWriteErrorReProcess(*_)
        then: 1 * jobStateListener.afterChunkError(*_)
        then: 1 * jobStateListener.beforeChunk(*_)
        then: 1 * jobStateListener.beforeChunkWriteErrorReProcess()
        then: 0 * jobStateListener.beforeProcess(*_)
        then: 0 * jobStateListener.afterProcess(*_)
        then: 0 * jobStateListener.beforeWrite(*_)
        then: 0 * jobStateListener.afterWrite(*_)
        then: 1 * jobStateListener.onSkipInWrite(*_)
        then: 1 * jobStateListener.afterChunkWriteErrorReProcess()
        then: 1 * jobStateListener.afterChunk(*_)
        then: 1 * jobStateListener.afterStep(*_)
        then: 1 * jobStateListener.afterJob(*_)
        // @formatter:on
    }

    def play(JobStateMachine jobStateMachine, List<String> events) {
        def argsCount = [
                'afterProcess'   : 2,
                'onProcessError' : 2,
                'onSkipInProcess': 2,
                'onWriteError'   : 2,
                'onSkipInWrite'  : 2
        ].withDefault { 0 }

        events.each {
//            println("emitting: $it")
            if (argsCount[it] == 0)
                jobStateMachine."$it"()
            else
                jobStateMachine."$it"(*(1..argsCount[it]).collect { null })
        }
    }

    public static void main(String[] args) {
        def split = """2018-04-10 14:08:24.516 [main] INFO  - beforeJob  
2018-04-10 14:08:24.528 [main] INFO  - beforeStep  
2018-04-10 14:08:24.538 [main] INFO  - beforeChunk  
2018-04-10 14:08:24.540 [main] INFO  - beforeRead  
2018-04-10 14:08:24.541 [main] INFO  - afterRead  
2018-04-10 14:08:24.542 [main] INFO  - beforeRead  
2018-04-10 14:08:24.542 [main] INFO  - afterRead  
2018-04-10 14:08:24.548 [main] INFO  - beforeProcess  
2018-04-10 14:08:24.554 [main] INFO  - onProcessError  
2018-04-10 14:08:24.555 [main] INFO  - afterChunkError  
2018-04-10 14:08:24.556 [main] INFO  - beforeChunk  
2018-04-10 14:08:24.556 [main] INFO  - beforeProcess  
2018-04-10 14:08:24.557 [main] INFO  - afterProcess  
2018-04-10 14:08:24.558 [main] INFO  - beforeWrite  
2018-04-10 14:08:24.560 [main] INFO  - afterWrite  
2018-04-10 14:08:24.561 [main] INFO  - onSkipInProcess  
2018-04-10 14:08:24.564 [main] INFO  - afterChunk  
2018-04-10 14:08:24.565 [main] INFO  - beforeChunk  
2018-04-10 14:08:24.565 [main] INFO  - beforeRead  
2018-04-10 14:08:24.565 [main] INFO  - afterRead  
2018-04-10 14:08:24.565 [main] INFO  - beforeRead  
2018-04-10 14:08:24.565 [main] INFO  - afterRead  
2018-04-10 14:08:24.565 [main] INFO  - beforeProcess  
2018-04-10 14:08:24.565 [main] INFO  - afterProcess  
2018-04-10 14:08:24.565 [main] INFO  - beforeProcess  
2018-04-10 14:08:24.565 [main] INFO  - afterProcess  
2018-04-10 14:08:24.565 [main] INFO  - beforeWrite  
2018-04-10 14:08:24.565 [main] INFO  - afterWrite  
2018-04-10 14:08:24.567 [main] INFO  - afterChunk  
2018-04-10 14:08:24.568 [main] INFO  - beforeChunk  
2018-04-10 14:08:24.568 [main] INFO  - beforeRead  
2018-04-10 14:08:24.568 [main] INFO  - afterRead  
2018-04-10 14:08:24.568 [main] INFO  - beforeRead  
2018-04-10 14:08:24.568 [main] INFO  - beforeProcess  
2018-04-10 14:08:24.568 [main] INFO  - afterProcess  
2018-04-10 14:08:24.568 [main] INFO  - beforeWrite  
2018-04-10 14:08:24.568 [main] INFO  - afterWrite  
2018-04-10 14:08:24.570 [main] INFO  - afterChunk  
2018-04-10 14:08:24.570 [main] INFO  - afterStep  
2018-04-10 14:08:24.572 [main] INFO  - afterJob""".split("\n")
                .collect { it.replaceAll(".* - ", '') }
//                .collect { "'${it.trim()}'" }
                .collect { "then: 1 * jobStateListener.${it.trim()}(*_)" }
//        println(split)
        println(split.join("\n"))
    }
}
