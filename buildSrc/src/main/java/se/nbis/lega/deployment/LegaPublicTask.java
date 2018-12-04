/**
 * 
 */
package se.nbis.lega.deployment;

/**
 *Lega public task
 */
public abstract class LegaPublicTask extends LocalEGATask {

    /**
     * 
     */
    public LegaPublicTask() {
        super();
        this.setGroup(Groups.LEGA_PUBLIC.name());
    }

}
