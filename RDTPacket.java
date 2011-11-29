
public class RDTPacket {

	/**
	 * ._____________________.
	 * |A|C|_S__|_SA_|__PAY__|
	 * |                     |
	 * |       PAYLOAD       |
	 * |_____________________|
	 * 
	 * A:  ACK Flag
	 * C:  Continuation Flag
	 * S:  Sequence Number
	 * SA: Sequence Number being ACK'd
	 * PAY: Payload Data
	 * 
	 */
	
	public static final int MTU = 68; // 96 bytes minus UDP and IPv4 overhead
	public static final int SEQNUM_LEN = 4; // 4 chars
	public static final int OVERHEAD = 2 + (SEQNUM_LEN * 2); // 10 bytes overhead for ACK flag, Continue flag, Seq numbers
	public static final int PAYLOAD_LEN = MTU - OVERHEAD; // 58 bytes for payload
	
	public String payload = new String();
	public String sequenceNumber = new String();
	public String ackedSeqNum = new String(); // the sequence number the ackFlag is dealing with
	public String ackFlag = new String();
	public String conFlag = new String();

	public RDTPacket(String payload, int sequenceNumber, String conFlag) {
		this.payload = payload;
		this.sequenceNumber = Integer.toString(sequenceNumber);
		this.conFlag = conFlag;
		this.ackFlag = "0";
		this.ackedSeqNum = "0000";
	}

	public RDTPacket(String ackedSeqNum, int sequenceNumber) {
		this.payload = "";
		this.sequenceNumber = Integer.toString(sequenceNumber);
		this.ackedSeqNum = ackedSeqNum;
		this.conFlag = "0";
		this.ackFlag = "1";
	}

	public RDTPacket(String ackFlag, String conFlag, String sequenceNumber, String ackedSeqNum, String payload) {
		this.payload = payload;
		this.sequenceNumber = sequenceNumber;
		this.ackedSeqNum = ackedSeqNum;
		this.conFlag = conFlag;
		this.ackFlag = ackFlag;
	}
	
	public String toString() {
		String s = ackFlag + conFlag + sequenceNumber + ackedSeqNum + payload;
		return s;
	}
	
}
