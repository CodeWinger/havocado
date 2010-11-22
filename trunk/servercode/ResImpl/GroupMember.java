package ResImpl;

import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.jgroups.Receiver;
import org.jgroups.View;
import org.jgroups.protocols.pbcast.NAKACK;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.Address;
import org.jgroups.ChannelException;

import ResInterface.MemberInfo;
import ResInterface.ResourceManager;

public abstract class GroupMember implements Receiver {
	private JChannel channel;
	protected List<MemberInfo> currentMembers = new LinkedList<MemberInfo>();
	private MemberInfo myInfo;
	protected boolean isMaster;
	
	public GroupMember(boolean isMaster, String myRMIServiceName, String groupName) {
		// TODO fill this in.
		try {
			this.isMaster = isMaster;
			myInfo = new MemberInfo(InetAddress.getLocalHost().getHostName(), myRMIServiceName);
			//TODO currentMembers.add(myInfo);
			channel = new JChannel("jconfig_FIFO.xml");
			channel.connect(groupName);
			channel.setReceiver(this);
			channel.
			NAKACK nak = (NAKACK)channel.getProtocolStack().findProtocol(NAKACK.class);
			nak.setLogDiscardMessages(false);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (ChannelException e) {
			e.printStackTrace();
		}
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
		Vector<Address> addresses = arg0.getMembers();
		if (isMaster) {
			for (Address a : addresses) {
				//a.
			}
		}
		else {
			if (addresses.contains(currentMembers.get(0)))
				promoteToMaster();
		}
	}
}
