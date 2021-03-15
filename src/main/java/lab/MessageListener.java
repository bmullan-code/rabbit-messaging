package lab;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;


@ConditionalOnProperty("consumer")
@Service
public class MessageListener {

    private static final Logger log = LoggerFactory.getLogger(MessageListener.class);
    RestTemplate restTemplate = new RestTemplate();
    
    AWSCredentials credentials = new BasicAWSCredentials(
    		  "", 
    		  ""
	);
    
    AmazonS3 s3client = AmazonS3ClientBuilder
    		  .standard()
    		  .withCredentials(new AWSStaticCredentialsProvider(credentials))
    		  .withRegion(Regions.US_EAST_2)
    		  .build();
    
    private String statusUrl = "http://localhost:8000/request/%s/status/%s";
    
    private void updateStatus(String guid, String status) throws URISyntaxException {
        URI uri = new URI(String.format(this.statusUrl,
    			guid,status));
        
        restTemplate.put( 
        	uri,null
        );
    }
    
    @RabbitListener(queues = MessagingApplication.QUEUE_SPECIFIC_NAME)
    public void receiveMessage(final QueueMessage customMessage) throws URISyntaxException {
        log.info("Received message as specific class: {}", customMessage.toString());
        
        
        updateStatus(customMessage.getGuid(),"Processing");
        s3client.putObject(
		  "a-sample-bucket-9b289a5080e2", 
		  customMessage.getGuid()+".txt",
		  customMessage.toString()
//		  new File("/Users/user/Document/hello.txt")
        );
        
    	try {
			TimeUnit.MINUTES.sleep(1);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			updateStatus(customMessage.getGuid(),"Complete");	
		}
    }
}
