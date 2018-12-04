/**
 * 
 */
package se.nbis.lega.deployment;

/**
 * Lega Private task
 */
public abstract class LegaPrivateTask extends LocalEGATask {

    /**
     * 
     */
    public LegaPrivateTask() {
        super();
        this.setGroup(Groups.LEGA_PRIVATE.name());
    }

}
