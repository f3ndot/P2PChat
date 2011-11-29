import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
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
		
		boolean done = false;
		while(!done) {
			RDTPacket packet = receivePacket();
			request.packets.add(packet);
			sendAck(packet.sequenceNumber, packet.senderHost, packet.senderPort);
			if(packet.conFlag.equals("0")) {
				done = true;
			}
		}
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
		RDTPacket packet = new RDTPacket(pktInfo.extractAckFlag(), pktInfo.extractConFlag(), pktInfo.extractSequenceNumber(), pktInfo.extractAckedSeqNum(), pktInfo.extractPayload());
		packet.senderHost = request.getAddress();
		packet.senderPort = request.getPort();
		return packet;
	}
	
	private void sendAck(String ackedSeqNum, InetAddress destHost, int destPort) {
		RDTPacket ackPacket = new RDTPacket(ackedSeqNum, sequenceNumber);
		sequenceNumber++;
		DatagramPacket p = new DatagramPacket(ackPacket.toString().getBytes(), ackPacket.toString().getBytes().length, destHost, destPort);
		try {
			socket.send(p);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
