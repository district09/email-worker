package gent.d09.servicefactory.email.worker.container;

import io.quarkus.arc.config.ConfigProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

@ConfigProperties(prefix = "exchange")
@Data
@NoArgsConstructor
public class ExchangeConfig {
    private String mailbox;
    private String username;
    private String password;
    private String url;
    private boolean mock;
    private int maxRetries;
    private int retryDelay;
}
