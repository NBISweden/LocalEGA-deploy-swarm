package se.nbis.lega.deployment.test;

import se.nbis.lega.deployment.Groups;
import se.nbis.lega.deployment.LocalEGATask;

/**
 * Cega task
 */
public abstract class TestTask extends LocalEGATask {

    public TestTask() {
        super();
        this.setGroup(Groups.TEST.getName());
    }

}
