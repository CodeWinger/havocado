package ResImpl;

import java.util.List;

import org.jgroups.Receiver;
import org.jgroups.View;
import org.jgroups.protocols.pbcast.NAKACK;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.Address;
import org.jgroups.ChannelException;

public class GroupMember implements Receiver {
	private JChannel channel;
	private List<MemberInfo> currentMembers;
	private MemberInfo myInfo;
	protected boolean isMaster;
	@Override
	public byte[] getState() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void receive(Message arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void setState(byte[] arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void block() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void suspect(Address arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void viewAccepted(View arg0) {
		// TODO Auto-generated method stub
		
	}
}
