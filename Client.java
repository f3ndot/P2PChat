import java.io.*;
import java.net.*;
import java.util.Random;
import java.lang.String;
import java.lang.StringBuilder;

class Client {

	public static final int MTU = 68; // 96 minus IPv4 and UDP overhead
	public static final int TIMEOUT = 5000; // milliseconds
	public static final int MAX_TRIES = 3; // until quit
	
	public static final String DIRECTORY_ADDR = "localhost";
	public static final int DIRECTORY_PORT = 55555;
	public static final String PROTOCOL_VERSION = "BOKCHAT/1.0";
	public static final String CRLF = "\r\n";

	public static String username;
	public static int port;
	public static InetAddress ipaddr;
	public static String host = new String();
	public static String lastCommand = new String();
	public static String consoleState = "console";
	public static int timeoutTry = 0;
	public static int sequenceNumber;
	
	public static void main(String args[]) throws Exception {

		sequenceNumber = new Random().nextInt(8999) + 1000;
		
		System.out.println("Chat client initiated! Prompting for client info...");
		System.out.println("SequenceNumber starting at "+sequenceNumber);

		BufferedReader inFromUser =
				new BufferedReader(new InputStreamReader(System.in));

		//		System.out.print("Username: ");
		//		username = inFromUser.readLine();
		//
		//		System.out.print("Client Port: ");
		//		port = Integer.parseInt(inFromUser.readLine());
		//
		//		System.out.print("Client IP: ");
		//		ipaddr = InetAddress.getByName(inFromUser.readLine());
		//		host = ipaddr.getCanonicalHostName();

		username = "Alice";
		System.out.println("Username: "+username);

		port = 44444;
		System.out.println("Client Port: "+port);

		ipaddr = InetAddress.getByName("127.0.0.1");
		host = ipaddr.getCanonicalHostName();
		System.out.println("Client IP: "+ipaddr.getHostAddress()+"\n");

		System.out.println("--- COMMAND MENU ---");
		System.out.println(" 1. /query-for-peers");
		System.out.println(" 2. /go-online");
		System.out.println(" 3. /go-offline");
		System.out.println(" 4. /leave-room");
		System.out.println(" 5. /join-room <username>");
		System.out.println(" 6. /quit");
		System.out.println(" 7. /test-rdt\n");

		while(!(lastCommand.equals("/quit"))) {
			System.out.print("["+consoleState+"] ");
			lastCommand = inFromUser.readLine();
			commandHandler(lastCommand);
			//System.out.println("Got command "+lastCommand);
		}


		/*while(true) {
			DatagramSocket clientSocket = new DatagramSocket();
			InetAddress IPAddress = InetAddress.getByName("localhost");
			byte[] sendData = new byte[MTU];
			byte[] receiveData = new byte[MTU];

			String strLine = new String();
			StringBuilder strWhole = new StringBuilder();

			while(!(strLine = inFromUser.readLine()).trim().equals("")) {
				strWhole.append(strLine+"\n");
				//System.out.println("Line: " + strLine);
			}
			//System.out.println("Whole: " + strWhole.toString());      

			sendData = strWhole.toString().getBytes();
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 55555);
			clientSocket.send(sendPacket);
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			clientSocket.receive(receivePacket);
			String modifiedSentence = new String(receivePacket.getData());
			System.out.println("FROM SERVER:" + modifiedSentence);
			clientSocket.close();
		}*/
	}


	public static void commandHandler(String cmd) {
		// switch(String) doesn't exist on JDK 6 (only 7)... Resorting to a series of ifelse statments
		if(cmd.contains("/query-for-peers")) {
			System.out.println("Querying directory server...");
			sendToDirectory("QUERY", null, null);
		} else if(cmd.contains("/go-online")) {
			System.out.println("Informing directory server...");
			String data[] = {"5", "bob"}; //TODO this should be nulls if needed and directory needs to handle it
			sendToDirectory("ONLINE", data, username);
		} else if(cmd.contains("/go-offline")) {
			System.out.println("Informing directory server...");
			sendToDirectory("OFFLINE", null, null);
		} else if(cmd.contains("/leave-room")) {;
		if(consoleState.equals("console")) {
			System.out.println("You're not in a room!");
		} else {
			System.out.println("Leaving chat room \""+cmd.substring(12)+"\"...");
			//TODO initiate P2P connection (if true set console state and room state)
			System.out.println("Informing directory server...");
			sendToDirectory("PARTED", null, cmd.substring(12));
		}
		} else if(cmd.contains("/join-room")) {
			if(cmd.length() < 11) {
				System.out.println("Please specify a chat room to join");
			} else {
				System.out.println("Joining chat room \""+cmd.substring(11)+"\"...");
				//TODO initiate P2P connection (if true set console state and room state)
				System.out.println("Informing directory server...");
				sendToDirectory("JOINED", null, cmd.substring(11));
			}
		} else if(cmd.contains("/quit")) {
			System.out.println("Quitting!");
		} else if(cmd.contains("/test-rdt")) {
			System.out.println("Sending a payload > 68 bytes to directory server...");
			sendToDirectory("RDTCHECK", null, "AAAAAAAAAAAAAAAAAAABBBBBBBBBBCCCCCCC");
		} else {
			System.out.println("You need to join a room before chatting!");
		}
	}


	public static void sendToDirectory(String method, String[] headers, String data) {
		try {
			

			String s = new String();

			if(data == null) {
				data = "";
			} else {
				data = CRLF + CRLF + data;
			}

			if(headers == null) {
				s = method + " " + host + ":" + port + " " + PROTOCOL_VERSION +

						data;
			} else {

				String rating = "";
				String chatroom = "";

				if(headers[0] != null)
					rating = "rating: " + headers[0];
				if(headers[1] != null)
					chatroom = "chatroom: " + headers[1];

				s = method + " " + host + ":" + port + " " + PROTOCOL_VERSION + CRLF +
						rating + CRLF +
						chatroom +

						data;
			}

			// annoyingly verbose
			//System.out.println("Sending Request: "+s);
			
			DatagramSocket clientSocket = new DatagramSocket();

			clientSocket.setSoTimeout(TIMEOUT);
			
			System.out.println("SBF: "+clientSocket.getSendBufferSize());
			System.out.println("SoT: "+clientSocket.getSoTimeout());
			
			rdtDispatch(s, clientSocket);
			clientSocket.close();
		} catch (IOException e) {
			e.printStackTrace();	
		}

	}

	public static String receiveFromDirectory(DatagramSocket clientSocket) throws SocketTimeoutException, IOException {
		byte[] receiveData = new byte[MTU];
		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
		clientSocket.receive(receivePacket);
		String response = new String(receivePacket.getData()).trim();
		System.out.println("Raw Response: " + response);
		//clientSocket.close();
		return response;
	}

	public static void rdtDispatch(String s, DatagramSocket socket) throws UnknownHostException {
		
		byte[] sendData = new byte[MTU];
		
		String outString = Integer.toString(sequenceNumber) + s;
		
		sendData = outString.getBytes();//Append SeqNum		
		DatagramPacket packet = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(DIRECTORY_ADDR), DIRECTORY_PORT);

		// NOTE TODO 4 chars = seqnum

		
		int directorySeqNum = -1;
		String incomingData = new String();
		if(timeoutTry < MAX_TRIES) {
			try {
				socket.send(packet);
				if(!(incomingData = receiveFromDirectory(socket)).isEmpty()) {
					directorySeqNum = extractSequenceNumber(incomingData);
					if(incomingData.contains(directorySeqNum+"ACK")) {
						System.out.println("ITS AN ACK");
					} else {
						System.out.println("NOT AN ACK");
						String ack = directorySeqNum+"ACK";
						rdtDispatch(ack, socket);
					}
				}
			} catch(SocketTimeoutException e) {
				System.out.println("Timeout waiting for directory! Trying again ("+timeoutTry+")");
				timeoutTry++;
				rdtDispatch(s, socket);
			} catch (IOException e) {
				e.printStackTrace();
			}		
			timeoutTry = 0;
			sequenceNumber++;
		} else {
			System.err.println("Maximum number of tries reached. Reverting back to console...");
		}
	}

	public static int extractSequenceNumber(String data) {
		System.out.println(data.substring(0,4));
		return Integer.parseInt(data.substring(0,4));
	}
	
}