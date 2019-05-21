package se.nbis.lega.deployment;

public enum Groups {

    CLUSTER("cluster"), COMMON("common"), LEGA_PUBLIC("lega-public"), LEGA_PRIVATE("lega-private"), CEGA("cega"), TEST("test");

    private String name;

    Groups(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
