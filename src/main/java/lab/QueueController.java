package lab;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.micrometer.core.annotation.Timed;

@RestController
public class QueueController {

	private final RabbitTemplate rabbitTemplate;
	private final AtomicLong counter = new AtomicLong();
    private static final Logger log = LoggerFactory.getLogger(MessageSender.class);
	
	@Autowired
    public QueueController(final RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
        System.out.println("===============================");
        String vcapServices = System.getenv("VCAP_SERVICES");
        System.out.println(vcapServices);
        System.out.println("===============================");
    }
	
	@RequestMapping("/")
	public String greet() {
		return "Hello!";
	}
	
	@Timed(value = "enqueue_restapi.invoke", histogram = true, percentiles = { 0.5,0.95, 0.99 }, extraTags = { "version",
		"v1" })
	@RequestMapping("/enqueue")
	public String enqueue() throws InterruptedException {

		final QueueMessage message = new QueueMessage(
        		counter.incrementAndGet(),
        		"Hello there!", 
        		new Random().nextInt(50), 
        		false
    		);
        log.info("Sending message..." + message.getId());
        rabbitTemplate.convertAndSend(MessagingApplication.EXCHANGE_NAME, MessagingApplication.ROUTING_KEY, message);

        	// simulate processing delay.
        TimeUnit.MILLISECONDS.sleep(
        		ThreadLocalRandom.current().nextInt(100, 500));
        
        return String.valueOf(message.getId());
	}


}
