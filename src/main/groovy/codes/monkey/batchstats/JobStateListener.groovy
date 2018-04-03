package codes.monkey.batchstats
/**
 * @author Johan Zietsman (jzietsman@thoughtworks.com.au).
 */
interface JobStateListener extends JobStateMachine.JobState {

    void onAfterLastRead()
}