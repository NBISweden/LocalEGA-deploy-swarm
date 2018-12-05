package se.nbis.lega.deployment;

/**
 * Cega task
 */
public abstract class CegaTask extends LocalEGATask {

    public CegaTask() {
        super();
        this.setGroup(Groups.CEGA.name());
    }

}
