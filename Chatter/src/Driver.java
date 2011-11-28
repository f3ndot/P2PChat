import java.net.*;


public class Driver {

	/**
	 * @param args
	 * @throws UnknownHostException 
	 */
	public static void main(String[] args) throws Exception {

		DirectoryServer directoryServer = new DirectoryServer();

		
		/*DirectoryClientEntry bob = new DirectoryClientEntry("buser", "192.168.1.101", 44444, -1, "auser");
		DirectoryClientEntry alice = new DirectoryClientEntry("auser", "192.168.1.101", 44445, -1, "auser");
		DirectoryClientEntry charlie = new DirectoryClientEntry("cuser", "192.168.1.102", 44446, -1, "cuser");
		
		DirectoryClientList directory = new DirectoryClientList();
		
		directory.addClient(bob);
		directory.addClient(alice);
		directory.addClient(charlie);
		
		directory.dumpList();
		
		directory.removeClientByUsername("auser");

		directory.dumpList();
		 */
		
		/*
		int timeout = 10; // 10 seconds
		
		DatagramSocket serverSocket = new DatagramSocket(55555);
		System.out.println("Socket created, entering loop");
		
		byte[] receiveData = new byte[1024];
		byte[] sendData = new byte[1024];
		while(true) {
			
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			serverSocket.receive(receivePacket);
			
			InetAddress IPAddress = receivePacket.getAddress();
            int port = receivePacket.getPort();
			String sentence = new String(receivePacket.getData());
			
			System.out.println("RECEIVED: " + sentence);
			
			sentence = "ACKNOWLEDGED!";
			sendData = sentence.getBytes();
			
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
			serverSocket.send(sendPacket);
			
		}*/
		
		//Client test = new Client("bob", InetAddress.getLocalHost(), 55958, "dogs");
		
		//System.out.println(Arrays.toString(test.displayInfo()));
		
	}

}
