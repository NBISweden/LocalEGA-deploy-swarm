package se.nbis.lega.deployment.cega;

import se.nbis.lega.deployment.Groups;
import se.nbis.lega.deployment.LocalEGATask;

/**
 * Cega task
 */
public abstract class CegaTask extends LocalEGATask {

    public CegaTask() {
        super();
        this.setGroup(Groups.CEGA.getName());
    }

}
