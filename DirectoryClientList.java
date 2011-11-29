import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class DirectoryClientList {

	public List<DirectoryClientEntry> clientList = new ArrayList<DirectoryClientEntry>();
	
	public boolean addClient(DirectoryClientEntry client) {
		boolean duplicateEntry = false;
		for (DirectoryClientEntry clientInDir : clientList) {
			if(clientInDir.username.equals(client.username))
				duplicateEntry = true;
			
			if(clientInDir.hostname.equals(client.hostname) && clientInDir.protocolPort == client.protocolPort)
				duplicateEntry = true;
		}
		
		if(!duplicateEntry) {
			clientList.add(client);
			return true;
		} else {
			return false;
		}
	}

	public DirectoryClientEntry getClientByHost(String hostaddr) {
		for (DirectoryClientEntry client : clientList) {
			if(client.hostname.equals(hostaddr)) {
				return client;
			}
		}
		return null;
	}
	
	public boolean removeClient(DirectoryClientEntry client) {
		boolean result = clientList.remove(client);
		return result;
	}

	public boolean removeClientByUsername(String username) {
		boolean result = false;
		Iterator<DirectoryClientEntry> it = clientList.iterator();
		while(it.hasNext()) {
			DirectoryClientEntry client = (DirectoryClientEntry) it.next();
			if(username.equals(client.username)) {
				result = clientList.remove(client);
			}
		}
		return result;
	}

	public boolean removeClientByHost(String hostaddr) {
		boolean result = false;

		for (int i = 0; i < clientList.size(); i++) {
			if(clientList.get(i).hostname.equals(hostaddr)) {
				clientList.remove(i);
				result = true;
			}
		}

		return result;
	}

	
	
	public void dumpList() {
		Iterator<DirectoryClientEntry> it = clientList.iterator();
		while(it.hasNext()) {
			DirectoryClientEntry client = (DirectoryClientEntry) it.next();
			System.out.println(client.toString());
		}
		System.out.println("---");
	}
	
}
