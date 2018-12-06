package se.nbis.lega.deployment.lega;

public enum Volume {

    LEGA_INBOX("lega-public_inbox"),
    LEGA_S3("lega-private_s3"),
    LEGA_DB("lega-private_db"),
    LEGA_PUBLIC_PORTAINER("lega-public_portainer"),
    LEGA_PRIVATE_PORTAINER("lega-private_portainer");

    private String name;

    Volume(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
