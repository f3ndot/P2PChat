import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Random;


public class RDTReceiver {

	public int sequenceNumber = new Random().nextInt(8999) + 1000;
	
	public RDTPacket incomingPacket;
	public RDTSegment receivedRequest;
	public DatagramSocket socket;
	public int listenPort;
	
	public RDTReceiver(int listenPort) {
		this.listenPort = listenPort;
		openSocket();
	}

	public RDTSegment receiveRequest() {
		
		
		
		RDTSegment request = new RDTSegment("", 0);
		return request;
	}
	
	private RDTPacket receivePacket() {
		byte[] data = new byte[RDTPacket.MTU];
		DatagramPacket request = new DatagramPacket(data, data.length);
		try {
			socket.receive(request);
		} catch (IOException e) {
			e.printStackTrace();
		}
		RawDatagramExtractor pktInfo = new RawDatagramExtractor(request);
		return new RDTPacket(pktInfo.extractAckFlag(), pktInfo.extractConFlag(), pktInfo.extractSequenceNumber(), pktInfo.extractAckedSeqNum(), pktInfo.extractPayload());
	}
	
	private void openSocket() {
		try {
			socket = new DatagramSocket(listenPort);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void closeSocket() { // Should only be called when shutting down Directory Server?
		socket.close();
	}
	
}
