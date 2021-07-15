package gent.d09.servicefactory.email.worker.module.common.service;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Liveness;

import javax.enterprise.context.ApplicationScoped;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Liveness
@ApplicationScoped
public class activeMqHealth implements HealthCheck {
    private final ConnectionFactory connectionFactory;

    public activeMqHealth(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public HealthCheckResponse call() {
        HealthCheckResponseBuilder builder = HealthCheckResponse
                .named("ActiveMQ")
                .withData("path", "activemq");

        try(Connection connection = this.connectionFactory.createConnection()) {
            builder.up();
        } catch(Exception e) {
            builder.down()
                .withData("details", "Could not connect to queue");
        }

        return builder.build();
    }
}
