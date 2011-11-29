
public class RDTSender {
	
	public String message;
	public String destHostname;
	public int destPort;
	
	public RDTSender(String message, String destHostname, int destPort) {
		this.message = message;
		this.destHostname = destHostname;
		this.destPort = destPort;
	}
	
}
