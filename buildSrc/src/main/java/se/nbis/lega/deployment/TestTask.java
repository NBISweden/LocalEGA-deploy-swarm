/**
 * 
 */
package se.nbis.lega.deployment;

/**
 *Cega task
 */
public abstract class TestTask extends LocalEGATask {

    /**
     * 
     */
    public TestTask() {
        super();
        this.setGroup(Groups.CEGA.name());
    }

}
