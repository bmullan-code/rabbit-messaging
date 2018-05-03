package lab;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.micrometer.core.annotation.Timed;

@RestController
public class HelloController {

	private final RabbitTemplate rabbitTemplate;
	private final AtomicLong counter = new AtomicLong();
    private static final Logger log = LoggerFactory.getLogger(CustomMessageSender.class);
	
	@Autowired
    public HelloController(final RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }
	
	@RequestMapping("/")
	public String greet() {
		return "Hello!";
	}
	
	@Timed(value = "enqueue_restapi.invoke", histogram = true, percentiles = { 0.95, 0.99 }, extraTags = { "version",
	"v1" })
	@RequestMapping("/enqueue")
	public String enqueue() {
//		private val counter = Metrics.counter("handler.calls", "uri", "/messages");
        final CustomMessage message = new CustomMessage(
        		counter.incrementAndGet(),
        		"Hello there!", 
        		new Random().nextInt(50), 
        		false
    		);
        log.info("Sending message..." + message.getId());
        rabbitTemplate.convertAndSend(MessagingApplication.EXCHANGE_NAME, MessagingApplication.ROUTING_KEY, message);
        
        return String.valueOf(message.getId());
	}


}
