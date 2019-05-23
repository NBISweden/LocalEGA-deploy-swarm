package se.nbis.lega.deployment.lega.pub;

public enum Config {

    CA_CERT("CA.cert"),
    INBOX_CERT("inbox.cert"),
    INBOX_KEY("inbox.key"),
    INBOX_JKS("inbox.jks"),
    MQ_CERT("mq.cert"),
    MQ_KEY("mq.key");

    private String name;

    Config(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
