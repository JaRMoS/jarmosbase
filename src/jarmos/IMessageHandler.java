package jarmos;

/**
 * An interface for classes capable of sending messages.
 * 
 * Used so far for jarmos.io.AModelManager loading messages on the JaRMoSA android app.
 * 
 * @author Daniel Wirtz @date 2013-08-07
 * 
 */
public interface IMessageHandler {

	public void sendMessage(String msg);

}
