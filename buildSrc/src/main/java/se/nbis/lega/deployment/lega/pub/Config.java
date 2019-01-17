package se.nbis.lega.deployment.lega.pub;

public enum Config {

    DEFS_JSON("lega-public.defs.json"),
    RABBITMQ_CONFIG("lega-public.rabbitmq.config"),
    ENTRYPOINT_SH("lega-public.entrypoint.sh");

    private String name;

    Config(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
