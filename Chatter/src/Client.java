import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;

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

	public static void main(String args[]) throws Exception {

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
		host = ipaddr.getCanonicalHostName();
		System.out.println("Client IP: "+ipaddr.getHostAddress()+"\n");

		System.out.println("--- COMMAND MENU ---");
		System.out.println(" 1. /query-for-peers");
		System.out.println(" 2. /go-online");
		System.out.println(" 3. /go-offline");
		System.out.println(" 4. /leave-room");
		System.out.println(" 5. /join-room <username>");
		System.out.println(" 6. /quit\n");

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
		} else {
			System.out.println("You need to join a room before chatting!");
		}
	}

	public static void sendToDirectory(String method, String[] headers, String data) {
		try {
			DatagramSocket clientSocket = new DatagramSocket();
			InetAddress IPAddress = InetAddress.getByName(DIRECTORY_ADDR);

			byte[] sendData = new byte[1024];
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

			sendData = s.getBytes();
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, DIRECTORY_PORT);
			clientSocket.send(sendPacket);
			receiveFromDirectory(clientSocket);
		} catch (IOException e) {
			e.printStackTrace();	
		}

	}


	public static void receiveFromDirectory(DatagramSocket clientSocket) {
		try {
			byte[] receiveData = new byte[1024];
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			clientSocket.receive(receivePacket);
			String response = new String(receivePacket.getData()).trim();
			System.out.println("Raw Response: " + response);
			clientSocket.close();
		} catch (IOException e) {
			e.printStackTrace();	
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