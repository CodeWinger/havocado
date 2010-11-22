package ResImpl;

import java.util.List;

import org.jgroups.Receiver;
import org.jgroups.View;
import org.jgroups.protocols.pbcast.NAKACK;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.Address;
import org.jgroups.ChannelException;

import ResInterface.MemberInfo;
import ResInterface.ResourceManager;

public class GroupMember implements Receiver {
	private JChannel channel;
	private List<MemberInfo> currentMembers;
	private MemberInfo myInfo;
	protected boolean isMaster;
	
	public GroupMember(boolean isMaster, String myRMIServiceName, String groupName) {
		// TODO fill this in.
	}
	
	public void promoteToMaster() {
		// TODO fill this in.
	}
	
	public ResourceManager getMaster() {
		// TODO fill this in.
		return null;
	}
	
	public byte[] getState() {
		// TODO Auto-generated method stub
		return null;
	}

	public void receive(Message arg0) {
		// TODO Auto-generated method stub
		
	}
	
	public void setState(byte[] arg0) {
		// TODO Auto-generated method stub
		
	}
	
	public void block() {
		// TODO Auto-generated method stub
		
	}
	
	public void suspect(Address arg0) {
		// TODO Auto-generated method stub
		
	}
	
	public void viewAccepted(View arg0) {
		// TODO Auto-generated method stub
		
	}
}
