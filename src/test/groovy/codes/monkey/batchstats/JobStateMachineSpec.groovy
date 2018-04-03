package codes.monkey.batchstats

import spock.lang.Specification

/**
 * @author Johan Zietsman (jzietsman@thoughtworks.com.au).
 */
class JobStateMachineSpec extends Specification {

    JobStateListener jobStateListener = Mock()

    def "it should notify of null read - items divisible by chunk size"() {
        given:
        JobStateMachine stateMachine = JobStateMachine.idle(jobStateListener)

        when:
        play(stateMachine, ['beforeJob', 'beforeStep', 'beforeChunk', 'beforeRead', 'afterChunk'])


        //multiple then blocks enforces the order of expectations
        then:
        1 * jobStateListener.beforeJob(_)
        then:
        1 * jobStateListener.beforeStep(_)
        then:
        1 * jobStateListener.beforeChunk(_)
        then:
        1 * jobStateListener.beforeRead()
        then:
        1 * jobStateListener.onAfterLastRead()
        then:
        1 * jobStateListener.afterChunk(_)

    }

    def "it should notify of null read - items not divisible by chunk size"() {
        given:
        JobStateMachine stateMachine = JobStateMachine.idle(jobStateListener)

        when:
        play(stateMachine, ['beforeJob', 'beforeStep', 'beforeChunk', 'beforeRead', 'afterRead', 'beforeRead',
                            'beforeProcess'])


        //multiple then blocks enforces the order of expectations
        then:
        1 * jobStateListener.beforeJob(_)
        then:
        1 * jobStateListener.beforeStep(_)
        then:
        1 * jobStateListener.beforeChunk(_)
        then:
        1 * jobStateListener.beforeRead()
        then:
        1 * jobStateListener.afterRead(_)
        then:
        1 * jobStateListener.beforeRead()
        then:
        1 * jobStateListener.onAfterLastRead()
        then:
        1 * jobStateListener.beforeProcess(_)

    }

    def "it should notify of read error"() {
        given:
        JobStateMachine stateMachine = JobStateMachine.idle(jobStateListener)

        when:
        play(stateMachine, ['beforeJob', 'beforeStep', 'beforeChunk', 'beforeRead', 'onReadError', 'beforeRead'])


        //multiple then blocks enforces the order of expectations
        then:
        1 * jobStateListener.beforeJob(_)
        then:
        1 * jobStateListener.beforeStep(_)
        then:
        1 * jobStateListener.beforeChunk(_)
        then:
        1 * jobStateListener.beforeRead()
        then:
        1 * jobStateListener.onReadError(_)
        then:
        1 * jobStateListener.beforeRead()
    }

    def play(JobStateMachine jobStateMachine, List<String> events) {
        events.each {
            jobStateMachine."$it"()
        }
    }
}
