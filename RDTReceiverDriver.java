import java.util.Random;


public class RDTReceiverDriver {

	public static int sequenceNumber = new Random().nextInt(8999) + 1000;
	
	public static final String DIRECTORY_ADDR = "localhost";
	public static final int DIRECTORY_PORT = 55555;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		System.out.println("Starting up Server...");
		
		String s = "BOKCHAT/1.0 200 OK";

		RDTReceiver receiveFromClient = new RDTReceiver(DIRECTORY_PORT);
	
		while(true) {
			System.out.println(receiveFromClient.receiveRequest().getPayload());
		}
		
	}

}
