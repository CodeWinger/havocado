package ResImpl;

import java.util.Collections;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Vector;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.jgroups.Receiver;
import org.jgroups.View;
import org.jgroups.protocols.pbcast.NAKACK;
import org.jgroups.stack.IpAddress;
import org.jgroups.ChannelClosedException;
import org.jgroups.ChannelNotConnectedException;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.Address;
import org.jgroups.ChannelException;

import ResInterface.MemberInfo;
import ResInterface.ResourceManager;

public abstract class GroupMember implements Receiver {
	private JChannel channel;
	protected LinkedList<MemberInfo> currentMembers = new LinkedList<MemberInfo>();
	private MemberInfo myInfo = null;
	protected MemberInfo master = null;
	protected boolean isMaster;
	
	public GroupMember(boolean isMaster, String myRMIServiceName, String groupName) {
		// TODO fill this in.
		try {
			this.isMaster = isMaster;
			myInfo = new MemberInfo(myRMIServiceName, InetAddress.getLocalHost());
			currentMembers.add(myInfo);
			channel = new JChannel("jconfig_FIFO.xml");
			channel.connect(groupName);
			channel.setReceiver(this);
			NAKACK nak = (NAKACK)channel.getProtocolStack().findProtocol(NAKACK.class);
			nak.setLogDiscardMessages(false);
		} catch (ChannelException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		//TODO Master specific stuff.
		//TODO Slave specific stuff.
	}
	
	public void promoteToMaster() {
		// TODO fill this in.
		master = myInfo;
		isMaster = true;
		try {
			channel.send(null, null, currentMembers);
		} catch (ChannelNotConnectedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ChannelClosedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
	
	protected abstract void specialReceive(Message arg0);
	
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
		Vector<Address> addresses = arg0.getMembers();
		PriorityQueue<Integer> toRemove = new PriorityQueue<Integer>(1, Collections.reverseOrder());
		boolean found;
		int position = 0;
		
		// For everyone in currentmembers, if they aren't in the view, mark them to be removed.
		for (MemberInfo mi : currentMembers) {
			found = false;
			for (Address a : addresses) {
				IpAddress ipa = (IpAddress)a;
				if (ipa.getIpAddress().equals(mi.address)) {
					found = true;
					break;
				}
			}
			if (!found)
				toRemove.add(position);	
			position++;
		}
		// Remove all marked members.
		while (!toRemove.isEmpty())
			currentMembers.remove(toRemove.poll());

		// If the master dies and we're next in line, become a master.
		if (!isMaster) {
			found = false;
			for (Address a : addresses) {
				IpAddress ipa = (IpAddress)a;
				if (ipa.getIpAddress().equals(master.address)) {
					found = true;
					break;
				}
			}
			if (!found)
				promoteToMaster();
		}
	}
}
