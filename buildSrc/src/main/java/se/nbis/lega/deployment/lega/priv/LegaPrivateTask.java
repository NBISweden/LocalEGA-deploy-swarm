package se.nbis.lega.deployment.lega.priv;

import se.nbis.lega.deployment.Groups;
import se.nbis.lega.deployment.LocalEGATask;

/**
 * Lega Private task
 */
public abstract class LegaPrivateTask extends LocalEGATask {

    public LegaPrivateTask() {
        super();
        this.setGroup(Groups.LEGA_PRIVATE.getName());
    }

}
