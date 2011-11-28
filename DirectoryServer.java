import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class DirectoryServer {

	public static final String DIRECTORY_ADDR = "localhost";
	public static final int DIRECTORY_PORT = 55555;
	public static final String PROTOCOL_VERSION = "BOKCHAT/1.0";
	public static final String CRLF = "\r\n";

	static DirectoryClientList directory = null;
	static DatagramSocket serverSocket = null;

	public DirectoryServer() throws IOException {
		directory = new DirectoryClientList();
		serverSocket = new DatagramSocket(DIRECTORY_PORT);
		System.out.println("Directory server socket created, entering loop...");

		while(true) {
			byte[] receiveData = new byte[1024];
			
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			serverSocket.receive(receivePacket);
			handleIncomingData(receivePacket.getData(), receivePacket.getAddress(), receivePacket.getPort());
		}
	}

	public void handleIncomingData(byte[] data, InetAddress clientAddress, int clientPort) {
		String strData = new String(data);

		// add it to an ArrayList for easy accessing, searching, etc.
		ArrayList<String> request = new ArrayList<String>();
		for(String line : strData.split(CRLF)) {
			request.add(line);
			System.out.println("Raw Request: "+line);
		}

		String[] requestLineMembers = request.get(0).split(" ");

		// add it to an ArrayList for easy accessing, searching, etc.
		ArrayList<String[]> headers = new ArrayList<String[]>();
		String requestData = new String();
		for (String string : request) {
			if(string.matches(".*: .*")) {
				String[] headerPair = string.split(": ");
				headers.add(headerPair);
			} else if(!string.contains(PROTOCOL_VERSION)) {
				requestData = requestData.concat(string);
			}
		}

		String method = requestLineMembers[0];
		String[] hostAndPort = requestLineMembers[1].split(":"); 
		String version = requestLineMembers[2].trim();

		if(!version.equals(PROTOCOL_VERSION)) {
			sendToClient(500, null, clientAddress, clientPort); // Version mismatch / Not Implemented
		
		//		System.out.println("---");
		//		
		//		System.out.println("Method:\t\t" + method);
		//		System.out.println("From:\t\t" + hostAndPort);
		//		System.out.println("Version:\t" + version);
		//		
		//		for (String[] strings : headers) {
		//			System.out.println("Header:\t\t"+strings[0]+"="+strings[1]);
		//		}
		//		
		//		if(!requestData.isEmpty()) {
		//			System.out.println("Data:\t\t"+requestData);
		//		}
		//		
		//		System.out.println("---\n");

		} else if(method.equals("QUERY")) {
            try {
				System.out.println("Dumping directory list for "+hostAndPort[0]+"...");
				Thread.sleep(10000); // simulating network lag
				directory.dumpList();
				sendToClient(201, directory, clientAddress, clientPort); // OK Peerlist
            } catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if(method.equals("ONLINE")) {
			System.out.println("Putting "+hostAndPort[0]+" online...");
			boolean result = directory.addClient(new DirectoryClientEntry(requestData, hostAndPort[0], Integer.parseInt(hostAndPort[1]), -1, null));
			if(!result) {
				System.err.println("USER ALREADY ONLINE / A USER HAS THE SAME CREDENTIALS");
				sendToClient(401, null, clientAddress, clientPort);
			} else {
				sendToClient(200, null, clientAddress, clientPort); // OK
			}
		} else if(method.equals("OFFLINE")) {
			System.out.println("Putting "+hostAndPort[0]+" offline...");
			boolean result = directory.removeClientByHost(hostAndPort[0]);
			if(!result) {
				System.err.println("USER ALREADY OFFLINE");
				sendToClient(402, null, clientAddress, clientPort);
			} else {
				sendToClient(200, null, clientAddress, clientPort); // OK
			}
		} else if(method.equals("JOINED")) {
			System.err.println("TODO :-)");
			sendToClient(500, null, clientAddress, clientPort); 
		} else if(method.equals("PARTED")) {
			System.err.println("TODO :-)");
			sendToClient(500, null, clientAddress, clientPort); 
		} else {
			System.err.println("UNKNOWN REQUEST");
			sendToClient(400, null, clientAddress, clientPort); 
		}

		System.out.println("---\n");

	}

	public static void sendToClient(int statusCode, DirectoryClientList directory, InetAddress clientAddress, int clientPort) {
		try {
			InetAddress IPAddress = InetAddress.getByName(DIRECTORY_ADDR);

			String statusPhrase = new String();
			switch(statusCode) {
			case 200:
				statusPhrase = "OK";
				break;
			case 201:
				statusPhrase = "OK Peerlist";
				break;
			case 400:
				statusPhrase = "Unknown Request";
				break;
			case 401:
				statusPhrase = "User Exists";
				break;
			case 402:
				statusPhrase = "User Already Offline";
				break;
			case 500:
				statusPhrase = "Not Implemented";
				break;
			}
			
			byte[] sendData = new byte[1024];

			String s = new String();
			if(directory == null) {
				s = PROTOCOL_VERSION + " " + statusCode + " " + statusPhrase;
			} else {
				s = PROTOCOL_VERSION + " " + statusCode + " " + statusPhrase;
			}

			sendData = s.getBytes();
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, clientAddress, clientPort);
			serverSocket.send(sendPacket);
		} catch (IOException e) {
			e.printStackTrace();	
		}
	}

}
