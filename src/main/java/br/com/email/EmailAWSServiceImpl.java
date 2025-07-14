package br.com.email;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.RawMessage;
import com.amazonaws.services.simpleemail.model.SendRawEmailRequest;

import br.com.email.dto.EmailRequest;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class EmailAWSServiceImpl implements EmailService {

        @ConfigProperty(name = "app.mailer.from")
        private String mailerFromAddress;

        @ConfigProperty(name = "app.mailer.configuration-set-name")
        private String mailerConfigurationSetName;

        @ConfigProperty(name = "app.aws.access-key-id")
        private String accessKey;

        @ConfigProperty(name = "app.aws.secret-access-key")
        private String secretKey;

        @Override
        public void send(EmailRequest emailDTO) {
                try {
                        final MimeMessage email = this.getMensagemEmailAmazon(emailDTO);
                        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                        email.writeTo(outputStream);
                        final RawMessage emailComAnexo = new RawMessage(ByteBuffer.wrap(outputStream.toByteArray()));
                        final SendRawEmailRequest requisicao = new SendRawEmailRequest(emailComAnexo)
                                        .withConfigurationSetName(this.mailerConfigurationSetName);
                        final AWSStaticCredentialsProvider credenciais = new AWSStaticCredentialsProvider(
                                        new BasicAWSCredentials(this.accessKey, this.secretKey));
                        final AmazonSimpleEmailService ses = AmazonSimpleEmailServiceClientBuilder.standard()
                                        .withCredentials(credenciais).withRegion(Regions.US_EAST_1).build();
                        ses.sendRawEmail(requisicao);
                } catch (final Exception e) {
                        throw new IllegalArgumentException(e);
                }
        }

        private MimeMessage getMensagemEmailAmazon(EmailRequest emailDTO) {
                try {
                        final Session sessao = Session.getDefaultInstance(new Properties());
                        final MimeMessage email = new MimeMessage(sessao);
                        email.setSubject(emailDTO.getSubject(), "UTF-8");

                        email.setFrom(new InternetAddress(this.mailerFromAddress));

                        email.setRecipients(RecipientType.TO, this.getRecipients(emailDTO.getTo()));
                        final MimeBodyPart wrap = new MimeBodyPart();

                        final MimeMultipart cover = new MimeMultipart("alternative");
                        final MimeBodyPart html = new MimeBodyPart();
                        cover.addBodyPart(html);

                        wrap.setContent(cover);

                        final MimeMultipart content = new MimeMultipart("related");
                        email.setContent(content);
                        content.addBodyPart(wrap);

                        html.setContent(emailDTO.getBody(), "text/html; charset=utf-8");

                        return email;
                } catch (final MessagingException e) {
                        throw new IllegalArgumentException(e);
                }
        }

        private Address[] getRecipients(final String emailRecipients) {
                try {
                        final String[] emails = emailRecipients.split(";");

                        final Address[] enderecos = new Address[emails.length];
                        for (int i = 0; i < emails.length; i++) {
                                enderecos[i] = new InternetAddress(emails[i]);
                        }

                        return enderecos;
                } catch (final AddressException e) {
                        throw new IllegalArgumentException(e);
                }
        }

}
