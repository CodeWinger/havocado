package ResImpl;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.Collections;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Vector;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.jgroups.Receiver;
import org.jgroups.View;
import org.jgroups.protocols.pbcast.NAKACK;
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
	protected MemberInfo myInfo = null;
	protected MemberInfo master = null;
	protected boolean isMaster;
	
	public GroupMember(boolean isMaster, String myRMIServiceName, String groupName) {
		try {
			this.isMaster = isMaster;
			myInfo = new MemberInfo(myRMIServiceName, InetAddress.getLocalHost());
			currentMembers.add(myInfo);
			channel = new JChannel("jconfig.xml");
			channel.connect(groupName);
			channel.setReceiver(this);
			NAKACK nak = (NAKACK)channel.getProtocolStack().findProtocol(NAKACK.class);
			nak.setLogDiscardMessages(false);
		} catch (ChannelException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

		try {
			System.out.println("Pre slave send");
			//channel.send(null, null, myInfo);
			Message m = new Message();
			m.setObject(myInfo);
			channel.send(m);
			channel.startFlush(true);
			System.out.println("Post slave send");
		} catch (ChannelNotConnectedException e) {
			e.printStackTrace();
		} catch (ChannelClosedException e) {
			e.printStackTrace();
		}
		System.out.println("GroupMember created. isMaster: " + isMaster);
	}
	
	/**
	 * Promotes this group member to a master.
	 * Specifically, sets the 'master' info to this member's info, sets the
	 * boolean 'isMaster' to true, and updates all other members' member list.
	 */
	public void promoteToMaster() {
		// DEBUG
		System.out.println("Promoting myself to master");
		
		master = myInfo;
		isMaster = true;
		try {
			channel.send(null, null, currentMembers);
		} catch (ChannelNotConnectedException e) {
			e.printStackTrace();
		} catch (ChannelClosedException e) {
			e.printStackTrace();
		}
		specialPromoteToMaster();
	}
	
	public abstract void specialPromoteToMaster();
	
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
	
	public static ResourceManager memberInfoToResourceManager(MemberInfo mi) {
		try {
			return (ResourceManager)LocateRegistry.getRegistry(mi.address.getHostName()).lookup(mi.rmiName);
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

	/**
	 * Send an object to all the members in our group.
	 * @param obj
	 */
	public void send(Serializable obj) {
		try {
			this.channel.send(null, null, obj);
		} catch (ChannelNotConnectedException e) {
			System.out.println("Channel is not connected.\n" + e.toString());
		} catch (ChannelClosedException e) {
			System.out.println("Channel closed.\n" + e.toString());
		}
	}
	
	@SuppressWarnings("unchecked")
	/**
	 * @param msg The message being received. 
	 */
	public void receive(Message msg) {
		System.out.println("MESSAGE RECEIVED.");
		// If we're master, if we get new member information, add the new member to
		// the list and broadcast the list.
		if (isMaster) {
			if (msg.getObject() instanceof MemberInfo) {
				currentMembers.add((MemberInfo)msg.getObject());
				currentMembers.getLast().setViewID(msg.getSrc());
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
				master = currentMembers.getFirst();
			}
		}
		specialReceive(msg.getObject());
		System.out.println("Current members: "+currentMembers);
	}
	
	protected abstract void specialReceive(Object arg0);
	
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
		// DEBUG: print the view.
		System.out.println(arg0.toString());
		System.out.println("Current members:");
		System.out.println(currentMembers);
		
		Vector<Address> addresses = arg0.getMembers();
		PriorityQueue<Integer> toRemove = new PriorityQueue<Integer>(1, Collections.reverseOrder());
		boolean found;
		int position = 0;
		
		// For everyone in currentmembers, if they aren't in the view, mark them to be removed.
		for (MemberInfo mi : currentMembers) {
			found = false;
			for (Address a : addresses) {
				if (a.equals(mi.viewID)) {
					mi.viewID = a;
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
				if (a.equals(master.viewID)) {
					found = true;
					break;
				}
			}
			if (!found) {
				if (myInfo.equals(currentMembers.getFirst()))
					promoteToMaster();
				// Even if we're not next in line, we know who the new master is.
				else
					master = currentMembers.getFirst();
			}
		}
		
		System.out.println("New members:"+currentMembers);
	}
}
