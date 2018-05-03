package lab;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import java.util.concurrent.TimeUnit;

@ConditionalOnProperty("consumer")
@Service
public class CustomMessageListener {

    private static final Logger log = LoggerFactory.getLogger(CustomMessageListener.class);

    @RabbitListener(queues = MessagingApplication.QUEUE_GENERIC_NAME)
    public void receiveMessage(final Message message) {
        log.info("Received message as generic: {}", message.toString());
        try {
        	TimeUnit.SECONDS.sleep(1);
        } catch(java.lang.InterruptedException ie) {
        	log.info("Interrupted");
        }
    }

    @RabbitListener(queues = MessagingApplication.QUEUE_SPECIFIC_NAME)
    public void receiveMessage(final CustomMessage customMessage) {
        log.info("Received message as specific class: {}", customMessage.toString());
        try {
        	TimeUnit.SECONDS.sleep(1);
        } catch(java.lang.InterruptedException ie) {
        	log.info("Interrupted");
        }
    }
}
