import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


class P2PClientConnection extends Thread {
		
		private final P2PServer parentServer;
		private final Socket socket;
		private PrintWriter socketWriter;
		private BufferedReader socketReader;

		public P2PClientConnection(P2PServer parentServer, Socket socket) {
			this.parentServer = parentServer;
			this.socket = socket;
			try {
				socketWriter = new PrintWriter(this.socket.getOutputStream(), true);
				socketReader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		public void write(String message) throws IOException {
			socketWriter.println(message);
		}

		@Override
		public void run() {
			try {
				System.out.println("Somebody has joined your chatroom.");
				parentServer.writeToAllClients("Chatroom: Somebody has joined us.");
				while (true) {
					String readLine = socketReader.readLine();
					if (readLine.equals("/END-SESSION")) {
						parentServer.endConnectionWith(this);
					}
					parentServer.writeToAllClients(readLine);
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void end() throws IOException {
			socket.close();
		}
	}