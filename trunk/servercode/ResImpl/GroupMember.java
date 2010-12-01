package ResImpl;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Set;
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
	
	private String configFile;
	private String groupName;
	public GroupMember(boolean isMaster, String myRMIServiceName, String pGroupName, String pConfigFile) {
		this.isMaster = isMaster;
		try { 
			myInfo = new MemberInfo(myRMIServiceName, InetAddress.getLocalHost());
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		}
		this.currentMembers.add(myInfo);
		this.configFile = pConfigFile;
		this.groupName = pGroupName;
	}
	
	/** Called by the child class once it is done initializing its constructor. */
	protected void joinGroup() {
		try {
			channel = new JChannel(configFile);
			channel.connect(groupName);
			channel.setReceiver(this);
			NAKACK nak = (NAKACK)channel.getProtocolStack().findProtocol(NAKACK.class);
			nak.setLogDiscardMessages(false);
		} catch (ChannelException e) {
			e.printStackTrace();
		}

		try {
			//System.out.println("Pre slave send");
			//channel.send(null, null, myInfo);
			
			Message m = new Message();
			m.setObject(myInfo);
			channel.send(m);
			//channel.send(null, null, myInfo);
			channel.startFlush(true);
			
			//send(myInfo);
			//System.out.println("Post slave send");
			
		} catch (ChannelNotConnectedException e) {
			e.printStackTrace();
		} catch (ChannelClosedException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Promotes this group member to a master.
	 * Specifically, sets the 'master' info to this member's info, sets the
	 * boolean 'isMaster' to true, and updates all other members' member list.
	 */
	public void promoteToMaster() {
		// DEBUG
		System.out.println("Promoted myself to master");
		master = myInfo;
		isMaster = true;
		//channel.send(null, null, currentMembers);
		//System.out.println("Sending current members: " + currentMembers);
		send(currentMembers);
		//System.out.println("Sent current members.");
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
			//Message m = new Message();
			//m.setObject(obj);
			//channel.send(m);
			//channel.startFlush(true);
			this.channel.send(null, null, obj);
			//channel.startFlush(true);
			//channel.startFlush(false); //test
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
		//System.out.println("MESSAGE RECEIVED.");
		// If we're master, if we get new member information, add the new member to
		// the list and broadcast the list.
		if (isMaster) {
			if (msg.getObject() instanceof MemberInfo) {
				MemberInfo newMember = (MemberInfo)msg.getObject();
				System.out.println("New member: " + newMember);
				boolean edited = false;
				for (MemberInfo mi : currentMembers) {
					if (newMember.equals(mi)) {
						mi.setViewID(msg.getSrc());
						edited = true;
					}
				}
				
				if (!edited) {
					currentMembers.add((MemberInfo) msg.getObject());
					currentMembers.getLast().setViewID(msg.getSrc());
					System.out.println("Current members: "+currentMembers);
				}
				//try {
					//channel.send(null, null, currentMembers);
					send(currentMembers);
					//channel.startFlush(false); //test
					/*
				} catch (ChannelNotConnectedException e) {
					e.printStackTrace();
				} catch (ChannelClosedException e) {
					e.printStackTrace();
				}
				*/
				
			}
		}
		// If we're slave, and we get a list of MemberInfo, update our list.
		else {
			if (msg.getObject() instanceof LinkedList<?>) {
				
				/*int position = 0;
				for (MemberInfo newMember : (LinkedList<MemberInfo>)msg.getObject()) {
					boolean found = false;
					for (MemberInfo mi : currentMembers) {
						if (mi.equals(newMember))
							found = true;
					}
					if (!found)
						currentMembers.add(position, newMember);
					position++;
				}*/
				currentMembers = (LinkedList<MemberInfo>)msg.getObject();
				master = currentMembers.getFirst();
				System.out.println("Current members: "+currentMembers);
			}
		}
		specialReceive(msg.getObject());
		
	}
	
	protected abstract void specialReceive(Object arg0);
	
	public void setState(byte[] arg0) {
		throw new UnsupportedOperationException("Not supported");
	}
	
	public void block() {
		throw new UnsupportedOperationException("Not supported");
	}
	
	public void suspect(Address arg0) {
		//throw new UnsupportedOperationException("Not supported");
	}
	
	/**
	 * @param arg0 The new view.
	 */
	public void viewAccepted(View arg0) {		
		Vector<Address> addresses = arg0.getMembers();
		Set<MemberInfo>toRemove = new HashSet<MemberInfo>();
		boolean found;
		
		// For everyone in currentmembers, if they aren't in the view, mark them to be removed.
		for (MemberInfo mi : currentMembers) {
			found = false;
			for (Address a : addresses) {
				if (a.equals(mi.viewID)) {
					found = true;
					break;
				}
			}
			if (!found){
				toRemove.add(mi);
				System.out.println("Member no longer present: "+mi);
			}
		}
		// Remove all marked members.
		for (MemberInfo mi : toRemove)
			currentMembers.remove(mi);

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
				if (myInfo.equals(currentMembers.getFirst())){
					
					// promoteToMaster();
					Runnable masterPromotion = new Runnable(){
						public void run() {
							try{
								// The idea here is to start the execution
								// of master promotion after we have exitted
								// viewAccepted()
								Thread.sleep(1000);
								promoteToMaster();
							} catch(InterruptedException e) {
								e.printStackTrace();
							}
						}
					};
					Thread masterPromotionThread = new Thread(masterPromotion); 
					masterPromotionThread.start();
					
				} else {
					// Even if we're not next in line, we know who the new master is.
					master = currentMembers.getFirst();
				}
			}
		}
	}
}
