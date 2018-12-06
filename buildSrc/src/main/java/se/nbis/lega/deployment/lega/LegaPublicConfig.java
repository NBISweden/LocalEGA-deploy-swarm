package se.nbis.lega.deployment.lega;

public enum LegaPublicConfig {

    DEFS_JSON("lega-public.defs.json"),
    RABBITMQ_CONFIG("lega-public.rabbitmq.config"),
    ENTRYPOINT_SH("lega-public.entrypoint.sh");

    private String name;

    LegaPublicConfig(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
