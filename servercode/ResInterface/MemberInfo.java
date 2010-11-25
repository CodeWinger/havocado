package ResInterface;

import java.io.Serializable;
import java.net.InetAddress;

import org.jgroups.Address;

public class MemberInfo implements Serializable{
	private static final long serialVersionUID = 1L;
	public String rmiName;
	public InetAddress address;
	public Address viewID;
	public MemberInfo(String pRmiName, InetAddress pAddress) {
		rmiName = pRmiName;
		address = pAddress;
	}
	
	public String toString() {
		return rmiName + " " + address + " " + viewID;
	}
	
	public void setViewID(Address a) {
		viewID = a;
	}
	
	public boolean equals(MemberInfo other) {
		return other.rmiName.equals(this.rmiName)
			&& other.address.equals(this.address)
			&& other.viewID.equals(this.viewID);
	}
}
