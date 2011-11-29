import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;


public class P2PServer extends Thread {
	
	private ServerSocket serverSocket;
	private ArrayList<P2PClientConnection> clients = new ArrayList<P2PClientConnection>();

	public P2PServer(int port) {
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			System.err.println("Error creating socket to open chatroom.");
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		while(true) {
			try {
				Socket socket = serverSocket.accept();
				P2PClientConnection connection = new P2PClientConnection(this, socket);
				clients.add(connection);
				connection.start();
				connection.write("Welcome!");
			} catch (IOException e) {
				System.err.println("Error creating socket with peer.");
				e.printStackTrace();
			}
		}
	}
	
	public void writeToAllClients(String readLine) {
		for (P2PClientConnection receivingSocket : clients) {
			try {
				receivingSocket.write(readLine);
			} catch (IOException e) {
				System.err.println("Failed to write back to a peer");
				e.printStackTrace();
			}
		}
	}

}
