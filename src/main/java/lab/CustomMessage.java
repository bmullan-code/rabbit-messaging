package lab;

import java.io.Serializable;

public final class CustomMessage implements Serializable {

	private long id;
    private String text;
    private int priority;
    private boolean secret;


    // Default constructor is needed to deserialize JSON
    public CustomMessage() {
    }

    public CustomMessage(long id, String text, int priority, boolean secret) {
    		this.id = id;
        this.text = text;
        this.priority = priority;
        this.secret = secret;
    }

    public String getText() {
        return text;
    }

    public int getPriority() {
        return priority;
    }

    public boolean isSecret() {
        return secret;
    }

    @Override
    public String toString() {
        return "CustomMessage{" +
                "text='" + text + '\'' +
                ", priority=" + priority +
                ", secret=" + secret +
                '}';
    }

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
}
