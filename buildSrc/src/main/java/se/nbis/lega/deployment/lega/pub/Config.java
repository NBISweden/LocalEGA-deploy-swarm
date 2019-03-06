package se.nbis.lega.deployment.lega.pub;

public enum Config {

    ;

    private String name;

    Config(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
