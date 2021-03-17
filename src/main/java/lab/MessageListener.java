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
    
    RestTemplate restTemplate = new RestTemplate();
    
    private String statusUrl = "https://%s/request/%s/status/%s";
    private String requestUrl = "https://%s/request/%s/content";
    private Random random = new Random();
    
    private void updateStatus(QueueMessage message, String status) {
		try {
			URI uri = new URI(String.format(this.statusUrl,
				message.getSourceHost(),message.getGuid(),status));

			System.out.println(uri.toString());
			
			restTemplate.put( 
				uri,null
			);
		} catch(Exception use) {
			System.out.println("Exception:"+use.toString());
		}
    }
    
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

    @RabbitListener(queues = MessagingApplication.QUEUE_SPECIFIC_NAME)
    public void receiveMessage(final QueueMessage customMessage)  {
        log.info("Received message as specific class: {}", customMessage.toString());
        
        try {
			updateStatus(customMessage,"Processing");
			// insert a random delay to simulate processing time
			int delay = random.nextInt(10)*1000;
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
