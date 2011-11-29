import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Random;


public class RDTSender {

	public static final int TIMEOUT = 5000; // 5 seconds timeout
	
	public int sequenceNumber = new Random().nextInt(8999) + 1000;
	
	public RDTSegment message;
	public String destHostname;
	public int destPort;
	
	DatagramSocket socket;
	
	public RDTSender(String message, String destHostname, int destPort) {
		this.message = new RDTSegment(message, sequenceNumber);
		sequenceNumber = this.message.sequenceNumberBase;
		this.destHostname = destHostname;
		this.destPort = destPort;
	}
	
	public void sendMessage() {			
		for (RDTPacket packet : message.getPackets()) {
			try {
				DatagramPacket p = new DatagramPacket(packet.toString().getBytes(), packet.toString().getBytes().length, InetAddress.getByName(destHostname), destPort);
				socket.send(p);
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}		
	}
	
	public void openSocket() {
		try {
			socket = new DatagramSocket();
			socket.setSoTimeout(TIMEOUT);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void closeSocket() {
		socket.close();
	}
	
}
