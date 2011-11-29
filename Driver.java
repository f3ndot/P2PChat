import java.net.*;


public class Driver {

	/**
	 * @param args
	 * @throws UnknownHostException 
	 */
	public static void main(String[] args) throws Exception {

        GUIConsole serverGUI = new GUIConsole("Server Console");				
        GUIConsole aliceGUI = new GUIConsole("Alice Console");
        GUIConsole bobGUI = new GUIConsole("Bob Console");
        GUIConsole charlieGUI = new GUIConsole("Charlie Console");
        
        serverGUI.launchFrame();
        aliceGUI.launchFrame();

        Client alice = new Client(aliceGUI);
		DirectoryServer directoryServer = new DirectoryServer(serverGUI);

	}

}
