package gent.d09.servicefactory.email.worker.container;

import io.quarkus.arc.config.ConfigProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

@ConfigProperties(prefix = "queue")
@Data
@NoArgsConstructor
public class QueueConfig {
    private String prefix;
    private String creation;
    private String status;
    private String retry;
}
