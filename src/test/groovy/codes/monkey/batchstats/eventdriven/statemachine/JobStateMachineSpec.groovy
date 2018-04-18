package codes.monkey.batchstats.eventdriven.statemachine

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
        then: 1 * jobStateListener.beforeProcess(*_)
        then: 1 * jobStateListener.afterProcess(*_)
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

        then: 1 * jobStateListener.beforeProcess(*_)
        then: 1 * jobStateListener.afterProcess(*_)
        // I intend to swallow these events.

        then: 0 * jobStateListener.beforeWrite(*_)
        then: 0 * jobStateListener.onWriteError(*_)
        then: 1 * jobStateListener.afterChunkWriteErrorReProcess()
        then: 1 * jobStateListener.afterChunkError(*_)
        then: 1 * jobStateListener.beforeChunk(*_)
        then: 1 * jobStateListener.beforeChunkWriteErrorReProcess()

        then: 1 * jobStateListener.beforeProcess(*_)
        then: 1 * jobStateListener.afterProcess(*_)
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
        then: 1 * jobStateListener.beforeProcess(*_)
        then: 1 * jobStateListener.afterProcess(*_)
        then: 0 * jobStateListener.beforeWrite(*_)
        then: 0 * jobStateListener.onWriteError(*_)
        then: 1 * jobStateListener.afterChunkWriteErrorReProcess(*_)
        then: 1 * jobStateListener.afterChunkError(*_)
        then: 1 * jobStateListener.beforeChunk(*_)
        then: 1 * jobStateListener.beforeChunkWriteErrorReProcess()
        then: 1 * jobStateListener.beforeProcess(*_)
        then: 1 * jobStateListener.afterProcess(*_)
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
            if (argsCount[it] == 0)
                jobStateMachine."$it"()
            else
                jobStateMachine."$it"(*(1..argsCount[it]).collect { null })
        }
    }

}
