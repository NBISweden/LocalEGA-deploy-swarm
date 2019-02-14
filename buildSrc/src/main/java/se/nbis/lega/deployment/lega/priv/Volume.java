package se.nbis.lega.deployment.lega.priv;

public enum Volume {

    INBOX_S3("lega-private-staging_inbox_s3"),
    VAULT_S3("lega-private-staging_vault_s3"),
    DB("lega-private-staging_db"),
    PORTAINER("lega-private-staging_portainer");

    private String name;

    Volume(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
