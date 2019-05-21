package se.nbis.lega.deployment.common;

import se.nbis.lega.deployment.Groups;
import se.nbis.lega.deployment.LocalEGATask;

/**
 * Common task
 */
public abstract class CommonTask extends LocalEGATask {

    public CommonTask() {
        super();
        this.setGroup(Groups.COMMON.getName());
    }

}
