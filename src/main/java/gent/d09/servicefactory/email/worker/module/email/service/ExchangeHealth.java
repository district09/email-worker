package gent.d09.servicefactory.email.worker.module.email.service;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Liveness;
import javax.enterprise.context.ApplicationScoped;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Liveness
@ApplicationScoped
public class ExchangeHealth implements HealthCheck {
    private final EmailService emailService;

    public ExchangeHealth(EmailService emailService) {
        this.emailService = emailService;
    }

    @Override
    public HealthCheckResponse call() {
        HealthCheckResponseBuilder builder = HealthCheckResponse
            .named("Exchange")
            .withData("path", "exchange");
        try {
            if(CompletableFuture.supplyAsync(emailService::isUp).get(10, TimeUnit.SECONDS)){
                builder.up();
            } else {
                builder
                    .down()
                    .withData("details", "Health check failed. The service has " + this.emailService.getFailedRequestsCounter() + " failed requests to exchange");
            }
        } catch(Exception e){
            builder
                .down()
                .withData("details", "Health check timed out");
        }

        return builder.build();
    }
}
