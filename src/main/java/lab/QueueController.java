package lab;

import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// QueueController is a rest api that enqueues items onto a rabbitmq and then receives 
// updates from the consumer

@ConditionalOnProperty("producer")
@RestController
public class QueueController {
	
	// get the host name the request is being made on, we will pass it in the queue item so we know 
	// where to call back to with the results
	@Value("${vcap.application.application_uris:localhost}")
	private String[] application_uris;

	// the rabbit template is our interface with the queue
	private final RabbitTemplate rabbitTemplate;
	
	// we will store in memory a hashmap of requests and their status. Note this only works for when we have a 
	// single instances of the api, better to use a shared redis or cloud-cache or store in a database 
	private HashMap<String,String> map = new HashMap<>();
	
	private final AtomicLong counter = new AtomicLong();
	private static final Logger log = LoggerFactory.getLogger(QueueController.class);
    
	// file utils is used to persist files to s3 or filesystem (see application.properties for configuration)
	@Autowired
	private FileUtils fileUtil;

	@Autowired
    public QueueController(final RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
        System.out.println("===============================");
        String vcapServices = System.getenv("VCAP_SERVICES");
        System.out.println(vcapServices);
        System.out.println("===============================");
    }

	// default route. 
	@RequestMapping("/")
	public String greet(@RequestHeader(value="host")String host) {	
		return "Hello from:'" + this.application_uris[0] +"'";
	}
	
	// returns the hashmap of request ids and their status
	@RequestMapping("/request/map")
	public HashMap map() {
		return this.map;
	}
	
	// creates a queue message and puts it on the rabbitmq queue
	@RequestMapping("/request/enqueue")
	public String enqueue() throws InterruptedException {

		final QueueMessage message = new QueueMessage(
        		counter.incrementAndGet(),
        		UUID.randomUUID().toString() 
		);
		
		message.setSourceHost(this.application_uris[0]);
        log.info("Sending message..." + message.getGuid());
        map.put(message.getGuid().toString(), "Queued");
        
        // send to the rabbitmq queue using an exchange
        rabbitTemplate.convertAndSend(
        		MessagingApplication.EXCHANGE_NAME, 
        		MessagingApplication.ROUTING_KEY, 
        		message
		);
        
        return message.getGuid();
	}
	
	// returns the status of an individual request based on its guid
	@RequestMapping("/request/{guid}/status")
	public String status(@PathVariable("guid") String guid) {
		return map.get(guid);
	}

	// updates a request status based on its guid and status
	@PutMapping("/request/{guid}/status/{status}")
	public void updateStatus(@PathVariable("guid") String guid,
			                 @PathVariable("status") String status)  {
		map.put(guid, status);
	}

	// not implemented
	@RequestMapping("/request/{guid}/download")
	public String download() throws InterruptedException {
		return "File";
	}
	
	// receives the request content from the consumer (in our case its simply a copy of the message)
	// and persists it using the fileutils wrapper class
	@PostMapping("/request/{guid}/content")
	ResponseEntity<String> postContent( @RequestBody QueueMessage message) throws IOException {
	
		this.fileUtil.putObject(message.getGuid(), message.toString());
		
		return new ResponseEntity<>("Hello World!", HttpStatus.OK);
	}

}
