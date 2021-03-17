package lab;
import java.io.FileWriter;
import java.io.IOException;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

@Service
public class FileUtils {
	
    private static final Logger log = LoggerFactory.getLogger(FileUtils.class);

    AWSCredentials credentials = null;
    AmazonS3 s3client = null;

    @Value("${filetype:default}")
	private String fileType;

	@Value("${accesskey:default}")
	private String s3AccessKey;

	@Value("${accesssecret:default}")
	private String s3AccessSecret;

    @Value("${bucketname:default}")
	private String s3Bucket;

    @Value("${datapath:default}")
	private String dataPath;

    @PostConstruct
	public void init() {

		System.out.println("key:'"+this.s3AccessKey+ "'");
		System.out.println("key:'"+this.s3AccessSecret + "'");

		this.credentials = new BasicAWSCredentials(
			this.s3AccessKey,
			this.s3AccessSecret
		);
    
    	this.s3client = AmazonS3ClientBuilder
    		  .standard()
    		  .withCredentials(new AWSStaticCredentialsProvider(credentials))
    		  .withRegion(Regions.US_EAST_2)
    		  .build();
	}

    public void putObject(String guid, String content) throws IOException {
        if (this.fileType=="s3") {
            s3client.putObject(
                this.s3Bucket, 
                guid+".txt",
                content.toString());
        } else {
        	FileWriter writer = new FileWriter(this.dataPath+"/"+guid+".txt");
        	writer.write(content);
        	writer.close();
        }
    }



}