package se.nbis.lega.deployment.lega.priv;

public enum Volume {

    INBOX_S3("lega-private_inbox_s3"),
    VAULT_S3("lega-private_vault_s3"),
    DB("lega-private_db"),
    PORTAINER("lega-private_portainer");

    private String name;

    Volume(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
