package gent.d09.servicefactory.email.worker.module.email.service;

import gent.d09.servicefactory.email.worker.container.ExchangeConfig;
import gent.d09.servicefactory.email.worker.module.email.domain.Attachment;
import gent.d09.servicefactory.email.worker.module.email.domain.event.EmailCreationEvent;
import gent.d09.servicefactory.email.worker.module.email.domain.event.EmailStatusEvent;
import lombok.SneakyThrows;
import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.enumeration.misc.ConnectingIdType;
import microsoft.exchange.webservices.data.core.enumeration.misc.ExchangeVersion;
import microsoft.exchange.webservices.data.core.enumeration.property.BodyType;
import microsoft.exchange.webservices.data.core.exception.service.local.ServiceLocalException;
import microsoft.exchange.webservices.data.core.service.item.EmailMessage;
import microsoft.exchange.webservices.data.credential.ExchangeCredentials;
import microsoft.exchange.webservices.data.credential.WebCredentials;
import microsoft.exchange.webservices.data.misc.ImpersonatedUserId;
import microsoft.exchange.webservices.data.property.complex.EmailAddress;
import microsoft.exchange.webservices.data.property.complex.EmailAddressCollection;
import microsoft.exchange.webservices.data.property.complex.FileAttachment;
import microsoft.exchange.webservices.data.property.complex.MessageBody;
import microsoft.exchange.webservices.data.property.complex.time.TimeZoneDefinition;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.xml.bind.DatatypeConverter;
import java.net.URI;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ApplicationScoped
public class EmailService {
    private static final Pattern emailPattern = Pattern.compile("(.*)[<](.+@.+\\..+)[>]");
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final ExchangeConfig exchangeConfig;
    private final Producer producer;
    private int failedRequestsCounter;

    @SneakyThrows
    public EmailService(ExchangeConfig exchangeConfig, Producer producer) {
        this.exchangeConfig = exchangeConfig;
        this.producer = producer;
        this.failedRequestsCounter = 0;
    }

    public void sendEmail(EmailCreationEvent event) {
        try {
            producer.send(EmailStatusEvent.builder()
                .correlationId(event.getCorrelationId())
                .id(event.getId())
                .status("InProgress")
                .statusMessage("The worker service is processing this request")
                .applicationId(event.getApplicationId())
                .build());

            log.info("Connecting to exchange");
            ExchangeService exchangeService = CompletableFuture.supplyAsync(() -> createExchangeService(exchangeConfig)).get(120, TimeUnit.SECONDS);
            exchangeService.setImpersonatedUserId(new ImpersonatedUserId(ConnectingIdType.SmtpAddress, event.getFrom()));
            EmailMessage email = new EmailMessage(exchangeService);

            // Set subject
            email.setSubject(event.getSubject());

            // Set text
            if(event.getText() != null){
                MessageBody messageBody = new MessageBody();
                messageBody.setBodyType(BodyType.Text);
                messageBody.setText(event.getText());
                email.setBody(messageBody);
            }

            // Set HTML
            if(event.getHtml() != null){
                MessageBody messageBody = new MessageBody();
                messageBody.setBodyType(BodyType.HTML);
                messageBody.setText(event.getHtml());
                email.setBody(messageBody);
            }

            email.setFrom(convertToAddress(event.getFrom()));
            email.setSender(convertToAddress(event.getFrom()));
            //email.setSender(new EmailAddress(getRawEmailAddress(event.getFrom())));

            // Add to addresses
            EmailAddressCollection to = email.getToRecipients();
            event.getTo().forEach((s) -> {
                to.add(convertToAddress(s));
            });


            // Add cc addresses
            EmailAddressCollection cc = email.getCcRecipients();
            event.getCc().forEach((s) -> {
                cc.add(convertToAddress(s));
            });

            // Add bcc addresses
            EmailAddressCollection bcc = email.getBccRecipients();
            event.getBcc().forEach((s) -> {
                bcc.add(convertToAddress(s));
            });

            // Set replyTo
            if(event.getReplyTo() != null){
                email.getReplyTo().add(new EmailAddress(event.getReplyTo()));
            }

            // Add attachments
            if(event.getAttachments() != null) {
                event.getAttachments().forEach(attachment -> addAttachment(email, attachment, false));
            }

            // Add inline images
            if(event.getInlineImages() != null) {
                event.getInlineImages().forEach(attachment -> addAttachment(email, attachment, true));
            }

            // Prevent actual sending if service mocking is enabled
            if (!exchangeConfig.isMock()) {
                log.info("Sending email with exchange");
                CompletableFuture.runAsync(() -> sendEmail(email)).get(600, TimeUnit.SECONDS);
            } else {
                log.info("Worker is running in mock mode. Exchange calls are not executed");
            }

            producer.send(EmailStatusEvent.builder()
                .correlationId(event.getCorrelationId())
                .id(event.getId())
                .status("Sent")
                .statusMessage("The Email has been sent with Exchange")
                .applicationId(event.getApplicationId())
                .build());

            log.info("Email with entity id " + event.getId() + " has been sent with Exchange");
        } catch (Exception e) {
            failedRequestsCounter++;
            if(event.getRetries() < exchangeConfig.getMaxRetries()) {
                event.setRetries(event.getRetries() + 1);
                long delay = ((Double) (Math.pow(event.getRetries(), 2) * exchangeConfig.getRetryDelay())).longValue();
                producer.retry(event, delay);
                log.error("Failed to send Email with entity id " + event.getId() + " to Exchange. Event will be retried for the " + event.getRetries() + "th time. Reason: " + e.getMessage());
            } else {
                log.error("Failed to send Email with entity id " + event.getId() + " for the maximum number of times. Sending 'Failed' status event. Reason: " + e.getMessage());
                producer.send(EmailStatusEvent.builder()
                    .correlationId(event.getCorrelationId())
                    .id(event.getId())
                    .status("Failed")
                    .statusMessage("An error occurred when trying to send the Email with Exchange: " + e.getMessage())
                    .applicationId(event.getApplicationId())
                    .build());
            }
        }
    }

    @SneakyThrows
    private void sendEmail(EmailMessage email) {
        email.send();
    }

    @SneakyThrows
    private ExchangeService createExchangeService(ExchangeConfig exchangeConfig) {
        try {
            ExchangeService exchangeService = new ExchangeService(ExchangeVersion.Exchange2010_SP2);
            exchangeService.setUrl(new URI(exchangeConfig.getUrl()));
            ExchangeCredentials credentials = new WebCredentials(exchangeConfig.getUsername(), exchangeConfig.getPassword());
            exchangeService.setCredentials(credentials);
            return exchangeService;
        } catch (Exception e) {
            log.error("Failed to connect to Exchange: " + e.getMessage() + " | Stacktrace: " + ExceptionUtils.getStackTrace(e));
            throw e;
        }
    }

    @SneakyThrows
    private void addAttachment(EmailMessage emailMessage, Attachment attachment, boolean inline){
        try {
            FileAttachment fileAttachment = emailMessage.getAttachments().addFileAttachment(attachment.getName(), DatatypeConverter.parseBase64Binary(attachment.getContent()));
            fileAttachment.setIsInline(inline);
            fileAttachment.setContentId(attachment.getId().toString());
            fileAttachment.setContentType(attachment.getContentType());
        } catch (ServiceLocalException e) {
            log.error("Failed to add attachment with id " + attachment.getId() + ": " + e.getMessage());
            throw e;
        }
    }

    public boolean isUp() {
        if(failedRequestsCounter > 0) {
            return false;
        }
        try {
            ExchangeService exchangeService = CompletableFuture.supplyAsync(() -> createExchangeService(exchangeConfig)).get(120, TimeUnit.SECONDS);
            Collection<TimeZoneDefinition> timeZoneDefinitions = exchangeService.getServerTimeZones();
            return (timeZoneDefinitions != null && !timeZoneDefinitions.isEmpty());
        } catch (Exception e){
            log.error("Ping to Exchange server failed: " + e.getMessage());
        }
        return false;
    }

    public int getFailedRequestsCounter() {
        return failedRequestsCounter;
    }

    private EmailAddress convertToAddress(String email) {
        Matcher matcher = emailPattern.matcher(email);
        if(matcher.find()) {
            return new EmailAddress(matcher.group(1), matcher.group(2));
        } else {
            return new EmailAddress(email);
        }
    }
}