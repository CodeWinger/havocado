package ResInterface;

import java.io.Serializable;
import java.net.InetAddress;

public class MemberInfo implements Serializable{
	public String machineName;
	public String rmiName;
	public InetAddress address;
	public MemberInfo(String pMachineName, String pRmiName, InetAddress pAddress) {
		machineName = pMachineName;
		rmiName = pRmiName;
		address = pAddress;
	}
}
