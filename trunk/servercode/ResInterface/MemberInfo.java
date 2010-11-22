package ResInterface;

import java.io.Serializable;

public class MemberInfo implements Serializable{
	public String machineName;
	public String rmiName;
	public MemberInfo(String pMachineName, String pRmiName) {
		machineName = pMachineName;
		rmiName = pRmiName;
	}
}
