import java.util.ArrayList;
import java.util.List;


public class RDTSegment {
	
	public String payload = new String();
	public int sequenceNumberBase;
	public List<RDTPacket> packets = new ArrayList<RDTPacket>();
	
	public RDTSegment(String payload, int sequenceNumberBase) {
		this.payload = payload; 
		while(payload.length() > RDTPacket.PAYLOAD_LEN) {
			String payloadChunk = payload.substring(0,RDTPacket.PAYLOAD_LEN);
			RDTPacket packet = new RDTPacket(payloadChunk, sequenceNumberBase++, "1");
			packets.add(packet);
			payload = payload.substring(RDTPacket.PAYLOAD_LEN);
		}
		RDTPacket packet = new RDTPacket(payload, sequenceNumberBase++, "0");
		packets.add(packet);
		this.sequenceNumberBase = sequenceNumberBase; 
	}
	
	public String toString() {
		return payload;
	}
	
	public List<RDTPacket> getPackets() {
		return packets;
	}
	
	public void dumpRaw() {
		for (RDTPacket packet : packets) {
			System.out.println(packet.toString());
		}
	}
	
}
