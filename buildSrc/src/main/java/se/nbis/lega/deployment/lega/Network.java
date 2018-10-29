package se.nbis.lega.deployment.lega;

public enum Network {

    CEGA("cega_cega");

    private String name;

    Network(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
