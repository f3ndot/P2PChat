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
	
	public void sendRequest() {
		openSocket();
		for (RDTPacket packet : message.getPackets()) {
			try {
				DatagramPacket request = new DatagramPacket(packet.toString().getBytes(), packet.toString().getBytes().length, InetAddress.getByName(destHostname), destPort);				
				socket.send(request);
				RDTPacket response = receiveResponse();
				if(response.ackFlag.equals("1")) {
					System.out.println("DEBUG: Received ACK for packet "+response.ackedSeqNum);
				}
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}		
	}
	
	// TODO borrow from RDTReceiver to complete this
	public RDTPacket receiveResponse() {
		byte[] data = new byte[RDTPacket.MTU];
		DatagramPacket response = new DatagramPacket(data, data.length);
		try {
			socket.receive(response);
		} catch (IOException e) {
			e.printStackTrace();
		}
		RawDatagramExtractor pktInfo = new RawDatagramExtractor(response);
		return new RDTPacket(pktInfo.extractAckFlag(), pktInfo.extractConFlag(), pktInfo.extractSequenceNumber(), pktInfo.extractAckedSeqNum(), pktInfo.extractPayload());
	}
		
	private void openSocket() {
		try {
			socket = new DatagramSocket();
			socket.setSoTimeout(TIMEOUT);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	private void closeSocket() {
		socket.close();
	}
	
	
}
