package gent.d09.servicefactory.email.worker.module.email.domain.event;

import gent.d09.servicefactory.email.worker.module.email.domain.Attachment;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@RegisterForReflection
public class EmailCreationEvent extends Event {
    private String applicationId;
    private String from;
    private List<String> to;
    private List<String> cc;
    private List<String> bcc;
    private String replyTo;
    private String subject;
    private String text;
    private String html;
    private List<Attachment> attachments;
    public List<Attachment> inlineImages;
    private Integer retries;

    @Builder
    public EmailCreationEvent(Long id, String correlationId, String applicationId, String from, List<String> to, List<String> cc, List<String> bcc, String replyTo, String subject, String text, String html, List<Attachment> attachments, List<Attachment> inlineImages, Integer retries) {
        super(id, correlationId);
        this.applicationId = applicationId;
        this.from = from;
        this.to = to;
        this.cc = cc;
        this.bcc = bcc;
        this.replyTo = replyTo;
        this.subject = subject;
        this.text = text;
        this.html = html;
        this.attachments = attachments;
        this.inlineImages = inlineImages;
        this.retries = retries;
        if(this.retries == null) {
            this.retries = 0;
        }
    }
}
