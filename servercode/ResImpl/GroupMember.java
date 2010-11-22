package ResImpl;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
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
	protected JChannel channel;
	protected LinkedList<MemberInfo> currentMembers = new LinkedList<MemberInfo>();
	private MemberInfo myInfo = null;
	protected MemberInfo master = null;
	protected boolean isMaster;
	
	public GroupMember(boolean isMaster, String myRMIServiceName, String groupName) {
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
		if (!isMaster) {
			try {
				channel.send(null, null, myInfo);
			} catch (ChannelNotConnectedException e) {
				e.printStackTrace();
			} catch (ChannelClosedException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Promotes this group member to a master.
	 * Specifically, sets the 'master' info to this member's info, sets the
	 * boolean 'isMaster' to true, and updates all other members' member list.
	 */
	public void promoteToMaster() {
		master = myInfo;
		isMaster = true;
		try {
			channel.send(null, null, currentMembers);
		} catch (ChannelNotConnectedException e) {
			e.printStackTrace();
		} catch (ChannelClosedException e) {
			e.printStackTrace();
		}
	}
	
	public ResourceManager getMaster() {
		try {
			return (ResourceManager)LocateRegistry.getRegistry().lookup(master.rmiName);
		} catch (AccessException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public ResourceManager memberInfoToResourceManager(MemberInfo mi) {
		try {
			return (ResourceManager)LocateRegistry.getRegistry().lookup(mi.rmiName);
		} catch (AccessException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public byte[] getState() {
		throw new UnsupportedOperationException("Not supported");
	}

	@SuppressWarnings("unchecked")
	/**
	 * @param msg The message being received. 
	 */
	public void receive(Message msg) {
		// If we're master, if we get new member information, add the new member to
		// the list and broadcast the list.
		if (isMaster) {
			if (msg.getObject() instanceof MemberInfo) {
				currentMembers.add((MemberInfo)msg.getObject());
				try {
					channel.send(null, null, currentMembers);
				} catch (ChannelNotConnectedException e) {
					e.printStackTrace();
				} catch (ChannelClosedException e) {
					e.printStackTrace();
				}
			}
		}
		// If we're slave, and we get a list of MemberInfo, update our list.
		else {
			if (msg.getObject() instanceof LinkedList<?>) {
				currentMembers = (LinkedList<MemberInfo>)msg.getObject();
			}
		}
		specialReceive(msg);
	}
	
	protected abstract void specialReceive(Message arg0);
	
	public void setState(byte[] arg0) {
		throw new UnsupportedOperationException("Not supported");
	}
	
	public void block() {
		throw new UnsupportedOperationException("Not supported");
	}
	
	public void suspect(Address arg0) {
		throw new UnsupportedOperationException("Not supported");
	}
	
	/**
	 * @param arg0 The new view.
	 */
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
