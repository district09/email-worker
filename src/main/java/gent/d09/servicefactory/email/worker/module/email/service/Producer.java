package gent.d09.servicefactory.email.worker.module.email.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import gent.d09.servicefactory.email.worker.container.QueueConfig;
import gent.d09.servicefactory.email.worker.module.email.domain.event.EmailCreationEvent;
import gent.d09.servicefactory.email.worker.module.email.domain.event.Event;
import gent.d09.servicefactory.email.worker.module.email.domain.event.EmailStatusEvent;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.jms.*;

@ApplicationScoped
public class Producer {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final JMSContext context;
    private final JMSProducer producer;
    private final QueueConfig queueConfig;

    public Producer(ConnectionFactory connectionFactory, QueueConfig queueConfig) {
        this.context = connectionFactory.createContext(Session.AUTO_ACKNOWLEDGE);
        this.producer = context.createProducer();
        this.queueConfig = queueConfig;
    }

    public void send(EmailStatusEvent event) {
        log.info("Sending status event for email entity with id: " + event.getId());
        send(event, queueConfig.getPrefix() + "-" + queueConfig.getStatus(), 0);
    }

    public void retry(EmailCreationEvent event, long delay) {
        send(event, queueConfig.getPrefix() + "-" + queueConfig.getRetry(), delay);
    }

    @SneakyThrows
    private void send(Event event, String topic, long delay) {
        Message message = context.createTextMessage(new ObjectMapper().writeValueAsString(event));
        producer.setDeliveryDelay(delay).send(context.createTopic(topic), message);
    }
}
