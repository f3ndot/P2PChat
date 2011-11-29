import java.net.DatagramPacket;


public class RawDatagramExtractor {

	String packetData;
	
	public RawDatagramExtractor(DatagramPacket p ) {
		packetData = new String(p.getData()).trim();
	}
	
	public String extractAckFlag() {
		return packetData.substring(0, 1);
	}

	public String extractConFlag() {
		return packetData.substring(1, 2);
	}

	public String extractSequenceNumber() {
		return packetData.substring(2, 6);
	}

	public String extractAckedSeqNum() {
		return packetData.substring(6, 10);
	}

	public String extractPayload() {
		
		return packetData.substring(10);
	}
	
}
