package se.nbis.lega.deployment.common;

import org.apache.sshd.common.config.keys.KeyUtils;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;

public class GenerateCertificatesTask extends CommonTask {

    @TaskAction
    public void run() throws Exception {
        getProject().file(".tmp/ssl/").mkdirs();
        X509Certificate root = generateCertificate(null, "CA");
        generateCertificate(root, "vaultS3");
        generateCertificate(root, "inboxS3");
        generateCertificate(root, "cegaMQ");
        generateCertificate(root, "cegaUsers");
        generateCertificate(root, "db");
        generateCertificate(root, "finalize");
        generateCertificate(root, "inbox");
        generateCertificate(root, "ingest");
        generateCertificate(root, "keys");
        generateCertificate(root, "privateMQ");
        generateCertificate(root, "publicMQ");
        generateCertificate(root, "verify");
    }

    private X509Certificate generateCertificate(X509Certificate rootCertificate, String service)
        throws IOException, GeneralSecurityException, OperatorCreationException {
        KeyPair keyPair = KeyUtils.generateKeyPair("ssh-rsa", 2048);

        X500Name subject = new X500NameBuilder(BCStyle.INSTANCE).addRDN(BCStyle.C, "NO")
            .addRDN(BCStyle.ST, "Norway").addRDN(BCStyle.L, "Oslo").addRDN(BCStyle.O, "UiO")
            .addRDN(BCStyle.OU, "IFI").addRDN(BCStyle.CN, service)
            .addRDN(BCStyle.EmailAddress, "nels-developers@googlegroups.com").build();

        X509v3CertificateBuilder builder = rootCertificate == null ?
            new JcaX509v3CertificateBuilder(
                new X500NameBuilder(BCStyle.INSTANCE).addRDN(BCStyle.CN, service).build(),
                getSecureRandomSerial(),
                Date.from(LocalDate.of(2018, 1, 1).atStartOfDay(ZoneOffset.UTC).toInstant()),
                Date.from(LocalDate.of(2020, 1, 1).atStartOfDay(ZoneOffset.UTC).toInstant()),
                subject, keyPair.getPublic()) :
            new JcaX509v3CertificateBuilder(rootCertificate, getSecureRandomSerial(),
                Date.from(LocalDate.of(2018, 1, 1).atStartOfDay(ZoneOffset.UTC).toInstant()),
                Date.from(LocalDate.of(2020, 1, 1).atStartOfDay(ZoneOffset.UTC).toInstant()),
                subject, keyPair.getPublic());

        ContentSigner signer =
            new JcaContentSignerBuilder("SHA256withRSA").build(keyPair.getPrivate());
        X509CertificateHolder holder = builder.build(signer);

        JcaX509CertificateConverter converter = new JcaX509CertificateConverter();
        converter.setProvider(new BouncyCastleProvider());
        X509Certificate certificate = converter.getCertificate(holder);

        writeCertificate(certificate, getProject().file(".tmp/ssl/" + service + ".cert"));

        writePrivateKey(keyPair, getProject().file(".tmp/ssl/" + service + ".key"));

        return certificate;
    }

    private BigInteger getSecureRandomSerial() {
        SecureRandom random = new SecureRandom();
        byte[] id = new byte[20];
        random.nextBytes(id);
        return new BigInteger(160, random);
    }

}
