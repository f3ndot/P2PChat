import java.util.Random;


public class RDTStructDriver {

	public static int sequenceNumber = new Random().nextInt(8999) + 1000;
	
	public static final String DIRECTORY_ADDR = "localhost";
	public static final int DIRECTORY_PORT = 55555;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		String s = "RDTCHECK 127.0.0.1:44444 BOKCHAT/1.0\r\n\r\nAAAAAAAAAAAAAAAAAAABBBBBBBBBBBBBBBCCCCCCCCCC";

		RDTSender sendToDirectory = new RDTSender(s, DIRECTORY_ADDR, DIRECTORY_PORT);
		
		sendToDirectory.sendMessage();
		
		
	}

}
