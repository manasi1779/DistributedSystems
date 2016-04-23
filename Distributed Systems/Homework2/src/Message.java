import java.io.Serializable;

public class Message implements Serializable{
	String message;
	int state;
	
	public Message(String message){
		this.message = message;
	}
}