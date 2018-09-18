package lab;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

//import io.micrometer.core.annotation.Timed;
//import io.micrometer.core.instrument.Gauge;
//import io.micrometer.core.instrument.MeterRegistry;

@RestController
public class QueueController {

	private final RabbitTemplate rabbitTemplate;
	private final AtomicLong counter = new AtomicLong();
    private static final Logger log = LoggerFactory.getLogger(MessageSender.class);
    private List<QueueMessage> quota = new ArrayList<>(1000);
    private static final List<String> keys = new LinkedList<String>();
    
//    Gauge gauge = null;
	
	@Autowired
    public QueueController(final RabbitTemplate rabbitTemplate /*,MeterRegistry registry*/) {
        this.rabbitTemplate = rabbitTemplate;
        System.out.println("===============================");
        String vcapServices = System.getenv("VCAP_SERVICES");
        System.out.println(vcapServices);
        System.out.println("===============================");

//        gauge = Gauge
//            .builder("quota.usage", quota, List::size)
//            .register(registry);

    }

//    @Autowired
//    MeterRegistry registry;
	
	@RequestMapping("/")
	public String greet() {
		return "Hello!";
	}
	
//	@Timed(value = "enqueue_restapi.invoke", histogram = true, percentiles = { 0.5,0.95, 0.99 }, extraTags = { "version",
//		"v1" })
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
        quota.add(message);
        	// simulate processing delay.
        TimeUnit.MILLISECONDS.sleep(
        		ThreadLocalRandom.current().nextInt(100, 3000));
        
        return String.valueOf(message.getId());
	}
	
	@RequestMapping("/key/{key}")
	public void key(@PathVariable("key") String key) throws InterruptedException {
		this.keys.add(key);
	}

	@RequestMapping("/keys")
	public int keys() throws InterruptedException {
		return this.keys.size();
	}


}
