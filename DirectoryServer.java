import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Random;

public class DirectoryServer {

	public static final int MTU = 68; // 96 minus IPv4 and UDP overhead
	public static final int TIMEOUT = 5000; // milliseconds
	public static final int MAX_TRIES = 3; // until quit
	
	public static final String DIRECTORY_ADDR = "localhost";
	public static final int DIRECTORY_PORT = 55555;
	public static final String PROTOCOL_VERSION = "BOKCHAT/1.0";
	public static final String CRLF = "\r\n";
	public static int timeoutTry = 0;
	public static int sequenceNumber;
	
	static DirectoryClientList directory = null;
	static DatagramSocket serverSocket = null;

	// really dirty, I know
	static int clientPort = -1;
	static InetAddress clientHost = null;
	
	public DirectoryServer() throws IOException {
		sequenceNumber = new Random().nextInt(8999) + 1000;
		directory = new DirectoryClientList();
		serverSocket = new DatagramSocket(DIRECTORY_PORT);
		System.out.println("Directory server sequence number starting at "+sequenceNumber);
		System.out.println("Directory server socket created, entering loop...");

		while(true) {
			String s = receiveFromClient(serverSocket);
			rdtReceive(s, serverSocket);
		}
	}

	public static void handleIncomingData(String s, int clientSeqNum) throws UnknownHostException {
				
		// add it to an ArrayList for easy accessing, searching, etc.
		ArrayList<String> request = new ArrayList<String>();
		for(String line : s.split(CRLF)) {
			request.add(line);
		}

		String[] requestLineMembers = request.get(0).split(" ");

		// add it to an ArrayList for easy accessing, searching, etc.
		ArrayList<String[]> headers = new ArrayList<String[]>();
		String requestData = new String();
		for (String string : request) {
			if(string.matches(".*: .*")) {
				String[] headerPair = string.split(": ");
				headers.add(headerPair);
			} else if(!string.contains(PROTOCOL_VERSION) || !string.contains("ACK")) { // TODO BAD CODE! Should check if i > 0...
				requestData = requestData.concat(string);
			}
		}

		String method = requestLineMembers[0];
		String[] hostAndPort = requestLineMembers[1].split(":"); 
		String version = requestLineMembers[2].trim();
		
		InetAddress host = clientHost;
		int port = clientPort;

		// TODO Add ACK sequence check here?
		if(!version.equals(PROTOCOL_VERSION)) {
			sendToClient(500, null, host, port, clientSeqNum); // Version mismatch / Not Implemented
		} else if(method.equals("QUERY")) {
			System.out.println("Dumping directory list for "+hostAndPort[0]+"...");
			directory.dumpList();
			sendToClient(201, directory, host, port, clientSeqNum); // OK Peerlist
		} else if(method.equals("ONLINE")) {
			System.out.println("Putting "+hostAndPort[0]+" online...");
			boolean result = directory.addClient(new DirectoryClientEntry(requestData.trim(), hostAndPort[0], Integer.parseInt(hostAndPort[1]), -1, null));
			if(!result) {
				System.err.println("USER ALREADY ONLINE / A USER HAS THE SAME CREDENTIALS");
				sendToClient(401, null, host, port, clientSeqNum);
			} else {
				sendToClient(200, null, host, port, clientSeqNum); // OK
			}
		} else if(method.equals("OFFLINE")) {
			System.out.println("Putting "+hostAndPort[0]+" offline...");
			boolean result = directory.removeClientByHost(hostAndPort[0]);
			if(!result) {
				System.err.println("USER ALREADY OFFLINE");
				sendToClient(402, null, host, port, clientSeqNum);
			} else {
				sendToClient(200, null, host, port, clientSeqNum); // OK
			}
		} else if(method.equals("JOINED")) {
			System.err.println("TODO :-)");
			sendToClient(500, null, host, port, clientSeqNum); 
		} else if(method.equals("PARTED")) {
			System.err.println("TODO :-)");
			sendToClient(500, null, host, port, clientSeqNum); 
		} else {
			System.err.println("UNKNOWN REQUEST");
			sendToClient(400, null, host, port, clientSeqNum); 
		}

		System.out.println("---\n");

	}

	public static void sendToClient(int statusCode, DirectoryClientList directory, InetAddress clientAddress, int clientPort, int clientSeqNum) {
		try {

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
			
			byte[] sendData = new byte[MTU];

			StringBuilder sb = new StringBuilder();
			String s = new String();
			if(directory == null) {
				s = PROTOCOL_VERSION + " " + statusCode + " " + statusPhrase + CRLF;
			} else {
				sb.append(PROTOCOL_VERSION + " " + statusCode + " " + statusPhrase + CRLF);
				
				for ( DirectoryClientEntry client : directory.clientList) {
					sb.append("Client: username="+client.username+" host="+client.hostIP.toString()+" port="+client.protocolPort+" rating="+client.rating+" room="+client.usernameChatroom+ CRLF);
				}
				
				s = sb.toString();
			}

			rdtDispatch(s, serverSocket, clientSeqNum);
			// close socket?
		} catch (IOException e) {
			e.printStackTrace();	
		}
	}

	public static String receiveFromClient(DatagramSocket serverSocket) throws SocketTimeoutException, IOException {
		byte[] receiveData = new byte[MTU];
		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
		serverSocket.receive(receivePacket);
		String request = new String(receivePacket.getData()).trim();
		System.out.println("Raw Request: " + request);
		System.out.println("len: " + request.length());
		//serverSocket.close();
		//TODO send ACK here (or RDT) <<< OR DO IT IN HANDLER's sendToClient() >>
		
		clientPort = receivePacket.getPort();
		clientHost = receivePacket.getAddress();
				
		return request;
	}

	public static void rdtReceive(String s, DatagramSocket socket) throws UnknownHostException {
		int clientSeqNum = extractSequenceNumber(s);
		
		if(isACK(s)) {
			System.out.println("ITS AN ACK.. ACKING SeqNum: "+getAckedSequenceNumber(s));
			s = s.substring(7);
		} else {
			s = s.substring(4);
			handleIncomingData(s, clientSeqNum);
		}
	}
	
	public static void rdtDispatch(String s, DatagramSocket socket, int clientSeqNum) throws IOException {
		
		byte[] sendData = new byte[MTU];
		
		String outString = Integer.toString(sequenceNumber) + "ACK" + Integer.toString(clientSeqNum) + s;

		System.out.println("Sending Raw Response: "+outString);
		
		sendData = outString.getBytes();//Append SeqNum		
		DatagramPacket packet = new DatagramPacket(sendData, sendData.length, clientHost, clientPort);
		socket.send(packet);
		sequenceNumber++;

	}

	public static int extractSequenceNumber(String data) {
		return Integer.parseInt(data.substring(0,4));
	}
	
	public static boolean isACK(String data) {
		return data.substring(4,7).equals("ACK");
	}
	
	public static int getAckedSequenceNumber(String data) {
		return Integer.parseInt(data.substring(7,11));		
	}

	
}
