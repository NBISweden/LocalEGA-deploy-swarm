package se.nbis.lega.deployment.lega.priv;

import se.nbis.lega.deployment.Groups;
import se.nbis.lega.deployment.LocalEGATask;
import se.nbis.lega.deployment.cluster.Machine;

/**
 * Lega Private task
 */
public abstract class LegaPrivateTask extends LocalEGATask {

    public LegaPrivateTask() {
        super();
        this.setGroup(Groups.LEGA_PRIVATE.getName());
        this.setMachineName(Machine.LEGA_PRIVATE.getName());
    }

}
