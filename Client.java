import java.io.*;
import java.net.*;
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

	public Client(GUIConsole gui) throws Exception {

		System.setOut(gui.log);
		System.setErr(gui.log);
		System.setIn(gui.consoleIn);
		
		System.out.println("Chat client initiated! Prompting for client info...");

		inFromUser = new BufferedReader(new InputStreamReader(System.in));


//		 System.out.print("Username: ");
//		 username = inFromUser.readLine();
//		
//		 System.out.print("Client Port: ");
//		 port = Integer.parseInt(inFromUser.readLine());
//		
//		 System.out.print("Client IP: ");
//		 ipaddr = InetAddress.getByName(inFromUser.readLine());
//		 host = ipaddr.getCanonicalHostName();

		username = "Alice";
		System.out.println("Username: "+username);

		port = 44444;
		System.out.println("Client Port: "+port);

		ipaddr = InetAddress.getByName("127.0.0.1");
		host = ipaddr.getHostAddress();
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
		}
	}


	public static void commandHandler(String cmd) throws IOException {
		// switch(String) doesn't exist on JDK 6 (only 7)... Resorting to a series of ifelse statments
		if(cmd.contains("/query-for-peers")) {
			System.out.println("Querying directory server...");
			sendToDirectory("QUERY", null, null);
		} else if(cmd.contains("/go-online")) {
			System.out.println("Informing directory server...");
			String data[] = {"5", "bob"}; //TODO this should be nulls if needed and directory needs to handle it
			sendToDirectory("ONLINE", data, username);
			runChatServer();
		} else if(cmd.contains("/go-offline")) {
			System.out.println("Informing directory server...");
			sendToDirectory("OFFLINE", null, null);
			endChatServer();
		} else if(cmd.contains("/leave-room")) {
			if(consoleState.equals("console")) {
				System.out.println("You're not in a room!");
			} else {
				System.out.println("Leaving chat room \""+cmd.substring(12)+"\"...");
				System.out.println("Informing directory server...");
				sendToDirectory("PARTED", null, cmd.substring(12));
			}
		} else if(cmd.contains("/join-room")) {
			if(cmd.length() < 11) {
				System.out.println("Please specify a chat room to join");
			} else {
				System.out.println("Joining chat room \""+cmd.substring(11)+"\"...");
				//FIXME get these two values from Directory
				joinChatServer("localhost", port);
				System.out.println("Informing directory server...");
				sendToDirectory("JOINED", null, cmd.substring(11));
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

	public static void sendToDirectory(String method, String[] headers, String data) {
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