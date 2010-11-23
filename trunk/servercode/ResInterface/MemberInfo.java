package ResInterface;

import java.io.Serializable;
import java.net.InetAddress;

public class MemberInfo implements Serializable{
	public String rmiName;
	public InetAddress address;
	public MemberInfo(String pRmiName, InetAddress pAddress) {
		rmiName = pRmiName;
		address = pAddress;
	}
	
	public boolean equals(MemberInfo other) {
		return other.rmiName.equals(this.rmiName)
			&& other.address.equals(this.address);
	}
}
