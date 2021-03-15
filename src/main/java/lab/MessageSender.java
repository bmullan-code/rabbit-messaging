package lab;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import io.micrometer.core.instrument.Metrics;


@ConditionalOnProperty("producer")
@Service
public class MessageSender {
	
	private final AtomicLong counter = new AtomicLong();
    private static final Logger log = LoggerFactory.getLogger(MessageSender.class);
    private final RabbitTemplate rabbitTemplate;
    
    @Autowired
    private AmqpAdmin amqpAdmin;

    @Autowired
    public MessageSender(final RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Scheduled(fixedDelay = 7500L)
    public void sendMessage() {
        final QueueMessage message = new QueueMessage(
        		counter.incrementAndGet(),
        		UUID.randomUUID().toString() 
    		);
        log.info("Sending message..." + message.getId());
        rabbitTemplate.convertAndSend(
        		MessagingApplication.EXCHANGE_NAME, 
        		MessagingApplication.ROUTING_KEY, 
        		message);
    }
}
