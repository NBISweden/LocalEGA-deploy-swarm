package se.nbis.lega.deployment.cluster;

public enum Machine {

    CEGA("cega"), LEGA_PUBLIC("lega-public"), LEGA_PRIVATE("lega-private");

    private String name;

    Machine(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
