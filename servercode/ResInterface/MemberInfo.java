package ResInterface;

import java.io.Serializable;

import org.jgroups.stack.IpAddress;

public class MemberInfo implements Serializable{
	public String machineName;
	public String rmiName;
	public IpAddress address;
	public MemberInfo(String pMachineName, String pRmiName, IpAddress pAddress) {
		machineName = pMachineName;
		rmiName = pRmiName;
		address = pAddress;
	}
}
