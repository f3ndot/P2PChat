import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.regex.Pattern;
import java.lang.String;
import java.lang.StringBuilder;

class Client {

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
	private static BufferedReader inFromUser;
	private static P2PServer p2pServer;

	public static ArrayList<String[]> rooms = new ArrayList<String[]>();

	public static void main(String args[]) throws Exception {
		System.out.println("Chat client initiated! Prompting for client info...");

		inFromUser = new BufferedReader(new InputStreamReader(System.in));


		System.out.print("Username: ");
		username = inFromUser.readLine();

		System.out.print("Client Port: ");
		port = Integer.parseInt(inFromUser.readLine());

		System.out.print("Client IP: ");
		ipaddr = InetAddress.getByName(inFromUser.readLine());
		host = ipaddr.getCanonicalHostName();

		System.out.println("--- COMMAND MENU ---");
		System.out.println(" 1. /query-for-peers");
		System.out.println(" 2. /go-online");
		System.out.println(" 3. /go-offline");
		System.out.println(" 4. /exit (only works in chatroom)");
		System.out.println(" 5. /join-room <username>");
		System.out.println(" 6. /quit");
		System.out.println(" 7. /test-rdt\n");

		while(!(lastCommand.equals("/quit"))) {
			System.out.print("["+consoleState+"] ");
			lastCommand = inFromUser.readLine();
			commandHandler(lastCommand);
		}
	}


	public static void commandHandler(String cmd) throws IOException {
		// switch(String) doesn't exist on JDK 6 (only 7)... Resorting to a series of ifelse statments
		if(cmd.contains("/query-for-peers")) {
			System.out.println("Querying directory server...");
			sendToDirectory("QUERY", null, null);
		} else if(cmd.contains("/go-online")) {
			System.out.println("Informing directory server...");
			String data[] = {"", ""};
			sendToDirectory("ONLINE", data, username);
			runChatServer();
		} else if(cmd.contains("/go-offline")) {
			System.out.println("Informing directory server...");
			sendToDirectory("OFFLINE", null, null);
			endChatServer();
		} else if(cmd.contains("/join-room")) {
			if(cmd.length() < 11) {
				System.out.println("Please specify a chat room to join");
			} else {

				for (String[] roomListing : rooms) {
					if(roomListing[0].equals(cmd.substring(11))) {
						System.out.println("Joining chat room \""+roomListing[0]+"\"...");
						System.out.println("Informing directory server...");
						String data[] = {"", cmd.substring(11)};
						sendToDirectory("JOINED", data, cmd.substring(11));
						consoleState = cmd.substring(11);
						joinChatServer(roomListing[1], Integer.parseInt(roomListing[2]));

					}
				}
				System.out.println("No room by that name!");

			}
		} else if(cmd.contains("/quit")) {
			System.out.println("Quitting!");
		} else if(cmd.contains("/test-rdt")) {
			System.out.println("Sending a payload > 58 bytes to directory server...");
			sendToDirectory("RDTCHECK", null, "AAAAAAAAAAAAAAAAAAABBBBBBBBBBCCCCCCCCCCCCCCCCCCCCCCCCCCCC");
		} else {
			System.out.println("You need to join a room before chatting!");
		}
	}

	public static void sendToDirectory(String method, String[] headers, String data) throws IOException {
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
		System.out.println("DEBUG: Sending Request: "+s);

		RDTSender sendToDirectory = new RDTSender(s, DIRECTORY_ADDR, DIRECTORY_PORT);

		sendToDirectory.sendRequest();

		byte[] receiveData = new byte[1024];
		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
		sendToDirectory.socket.receive(receivePacket);
		sendToDirectory.closeSocket();
		String sentence = new String(receivePacket.getData()).trim();
		System.out.println("DEBUG: Received Response: " + sentence);
		showAvailableChatrooms(sentence);


	}

	public static void showAvailableChatrooms(String s) {
		System.out.println("--- AVAILABLE CHATROOMS ---");
		for(String line : s.split(CRLF)) {
			if(line.matches("Client: .*")) {
				String[] chatPair = line.split(": ");
				String[] roomDetails = chatPair[1].split(" ");
				String roomName = roomDetails[0].substring(9);
				String hostAddr = roomDetails[1].substring(6);
				String portNum = roomDetails[2].substring(5);

				String[] listing = { roomName, hostAddr, portNum };
				rooms.add(listing);

				System.out.println(listing[0] + "  " + listing[1] + " " + listing[2]);
			}
		}
	}

	public static void runChatServer() throws IOException {
		p2pServer = new P2PServer(port);
		p2pServer.start();
	}

	private static void endChatServer() throws IOException {
		p2pServer.stop();
		p2pServer.endAllConnections();
		p2pServer.end();
	}

	public static void joinChatServer(String someHost, int somePort) throws IOException {
		Socket socket = new Socket(someHost, somePort);

		ReceivedMessagePrinter receiverPrinter = new ReceivedMessagePrinter(socket);
		receiverPrinter.start();

		PrintWriter socketWriter = new PrintWriter(socket.getOutputStream(),true);
		while (true) {
			String line = inFromUser.readLine();
			if (line.equals("/exit")) {
				sendToDirectory("PARTED", null, consoleState);
				consoleState = "console";
				break;
			}
			socketWriter.println(username +": "+ line);
		}
		socketWriter.println("/END-SESSION");
		receiverPrinter.stop();
		socket.close();
	}

	static class ReceivedMessagePrinter extends Thread {

		private BufferedReader socketReader;
		private Socket socket;

		public ReceivedMessagePrinter(Socket socket) throws IOException {
			this.socket = socket;
			this.socketReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		}

		@Override
		public void run() {
			while (true) {
				try {
					System.out.println(socketReader.readLine());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}