package se.nbis.lega.deployment.cluster;

public enum Machine {

    CEGA("cega"), LEGA("lega");

    private String name;

    Machine(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
