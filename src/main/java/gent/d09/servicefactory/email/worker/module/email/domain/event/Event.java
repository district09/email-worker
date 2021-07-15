package gent.d09.servicefactory.email.worker.module.email.domain.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public abstract class Event {
    private Long id;
    private String correlationId;
}
