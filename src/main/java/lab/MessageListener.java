package lab;

import java.net.URI;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


@ConditionalOnProperty("consumer")
@Service
public class MessageListener {

    private static final Logger log = LoggerFactory.getLogger(MessageListener.class);
    
    // we use the client resttemplate to send updates back to the producer rest api.
    RestTemplate restTemplate = new RestTemplate();
    // templates for the requests
    private String statusUrl = "https://%s/request/%s/status/%s";
    private String requestUrl = "https://%s/request/%s/content";
    
    private Random random = new Random();
    
    // update the status of a request
    private void updateStatus(QueueMessage message, String status) {
		try {
			URI uri = new URI(String.format(this.statusUrl,
				message.getSourceHost(),message.getGuid(),status));
			
			log.info(uri.toString());
			
			restTemplate.put( 
				uri,null
			);
		} catch(Exception use) {
			System.out.println("Exception:"+use.toString());
		}
    }
    
    // send the content back to the producer, in a "real" application you would write it to a 
    // shared persistent source here  
    private void sendContent(QueueMessage message) {
    	try {
			URI uri = new URI(String.format(this.requestUrl,
				message.getSourceHost(),message.getGuid().toString()));
			System.out.println(uri.toString());

			restTemplate.postForObject(uri, message, String.class);

    	} catch(Exception use) {
			System.out.println("Exception:"+use.toString());
			use.printStackTrace();
		}
    	
    }

    // this is the method that is called when a new message is received from the queue
    @RabbitListener(queues = MessagingApplication.QUEUE_SPECIFIC_NAME)
    public void receiveMessage(final QueueMessage customMessage)  {
    	
        log.info("Received message as specific class: {}", customMessage.toString());
        
        try {
			updateStatus(customMessage,"Processing");
			// insert a random delay to simulate processing time
			int delay = random.nextInt(60)*1000;
			log.info("Pausing for {} milliseconds",delay);
			Thread.sleep(delay);
			this.sendContent(customMessage);
			updateStatus(customMessage,"Complete");
        } catch (Exception e) {
			e.printStackTrace();
			updateStatus(customMessage,"Error");
		}
    }
}
