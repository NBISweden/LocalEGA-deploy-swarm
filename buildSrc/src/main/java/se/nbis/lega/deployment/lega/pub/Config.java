package se.nbis.lega.deployment.lega.pub;

public enum Config {

    NGINX_CONF("nginx.conf");

    private String name;

    Config(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
