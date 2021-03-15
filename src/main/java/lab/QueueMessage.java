package lab;

import java.io.Serializable;
import java.util.HashMap;

public final class QueueMessage implements Serializable {

	private long id;
    private String guid;

    // Default constructor is needed to deserialize JSON
    public QueueMessage() {
    }

    public QueueMessage(long id, String guid) {
		this.id = id;
		this.guid = guid;
    }
    public void setGuid(String guid) {
    	this.guid = guid;
    }
    public String getGuid() {
        return this.guid;
    }
	public void setId(long id) {
		this.id = id;
	}

    public long getId() {
    	return this.id;
    }

    @Override
    public String toString() {
        return "CustomMessage{" +
        			"id='" + id + "' " +
                "guid='" + guid + '\'' +
                '}';
    }

}
