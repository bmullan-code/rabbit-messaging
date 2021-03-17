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

@ConditionalOnProperty("producer")
@RestController
public class QueueController {
	
	@Value("${vcap.application.application_uris:localhost}")
    private String[] application_uris;

	private final RabbitTemplate rabbitTemplate;
	private final AtomicLong counter = new AtomicLong();
    private static final Logger log = LoggerFactory.getLogger(QueueController.class);
    private HashMap<String,String> map = new HashMap<>();
    
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

	@RequestMapping("/")
	public String greet(@RequestHeader(value="host")String host) {
		System.out.println(this.application_uris);
		return "Hello:'"+host+"'"+"    '"+ this.application_uris[0];
	}
	
	@RequestMapping("/request/map")
	public HashMap map() {
		return this.map;
	}
	
	@RequestMapping("/request/enqueue")
	public String enqueue() throws InterruptedException {

		final QueueMessage message = new QueueMessage(
        		counter.incrementAndGet(),
        		UUID.randomUUID().toString() 
		);
		message.setSourceHost(this.application_uris[0]);
        log.info("Sending message..." + message.getGuid());
        map.put(message.getGuid().toString(), "Queued");
        rabbitTemplate.convertAndSend(
        		MessagingApplication.EXCHANGE_NAME, 
        		MessagingApplication.ROUTING_KEY, 
        		message
		);
        return message.getGuid();
	}
	
	@RequestMapping("/request/{guid}/status")
	public String status(@PathVariable("guid") String guid) {
		return map.get(guid);
	}

	@PutMapping("/request/{guid}/status/{status}")
	public void updateStatus(@PathVariable("guid") String guid,
			                 @PathVariable("status") String status)  {
		map.put(guid, status);
	}

	@RequestMapping("/request/{guid}/download")
	public String download() throws InterruptedException {
		return "File";
	}
	
	@PostMapping("/request/{guid}/content")
	ResponseEntity<String> postContent( @RequestBody QueueMessage message) throws IOException {
	
		this.fileUtil.putObject(message.getGuid(), message.toString());
		
		return new ResponseEntity<>("Hello World!", HttpStatus.OK);
	}

}
