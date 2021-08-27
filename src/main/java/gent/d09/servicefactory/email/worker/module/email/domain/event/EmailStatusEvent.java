package gent.d09.servicefactory.email.worker.module.email.domain.event;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@RegisterForReflection
public class EmailStatusEvent extends Event {
    private String status;
    private String statusMessage;
    private String applicationId;

    @Builder
    public EmailStatusEvent(Long id, String correlationId, String status, String statusMessage, String applicationId) {
        super(id, correlationId);
        this.status = status;
        this.statusMessage = statusMessage;
        this.applicationId = applicationId;
    }
}