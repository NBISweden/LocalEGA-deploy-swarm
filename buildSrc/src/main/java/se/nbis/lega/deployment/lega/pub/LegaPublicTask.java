package se.nbis.lega.deployment.lega.pub;

import se.nbis.lega.deployment.Groups;
import se.nbis.lega.deployment.LocalEGATask;

/**
 * Lega public task
 */
public abstract class LegaPublicTask extends LocalEGATask {

    public LegaPublicTask() {
        super();
        this.setGroup(Groups.LEGA_PUBLIC.getName());
    }

}
