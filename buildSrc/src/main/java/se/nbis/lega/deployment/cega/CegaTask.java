package se.nbis.lega.deployment.cega;

import se.nbis.lega.deployment.Groups;
import se.nbis.lega.deployment.LocalEGATask;
import se.nbis.lega.deployment.cluster.Machine;

/**
 * Cega task
 */
public abstract class CegaTask extends LocalEGATask {

    public CegaTask() {
        super();
        this.setGroup(Groups.CEGA.getName());
        this.machineName = Machine.CEGA.getName();
    }

}
