package se.nbis.lega.deployment.lega.pub;

import se.nbis.lega.deployment.Groups;
import se.nbis.lega.deployment.LocalEGATask;
import se.nbis.lega.deployment.cluster.Machine;

/**
 * Lega public task
 */
public abstract class LegaPublicTask extends LocalEGATask {

    public LegaPublicTask() {
        super();
        this.setGroup(Groups.LEGA_PUBLIC.getName());
        this.machineName = Machine.LEGA_PUBLIC.getName();
    }

}
