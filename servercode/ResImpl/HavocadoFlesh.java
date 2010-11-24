// -------------------------------
// adapated from Kevin T. Manley
// CSE 593
//
package ResImpl;

import ResInterface.*;

import java.util.*;
import java.rmi.*;
import java.net.*;
import java.io.*;

import Commands.RMICommands.*;

import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import org.jgroups.Message;

import LockManager.DeadlockException;
import LockManager.LockManager;


//public class HavocadoFlesh extends java.rmi.server.UnicastRemoteObject
public class HavocadoFlesh extends GroupMember implements ResourceManager {
	private static final String MASTER_FLAG = "master";
	private static final String SLAVE_FLAG = "slave";
	public static final String FORCE_SHUTDOWN = "force";

    /** The middleware's lock manager. All lock management is done on the middleware side */
    final LockManager lm;
    
    /** The middleware's transaction manager. Handles keeping track of transactions. */
    final Overseer overseer;

    private final LinkedList<MemberInfo> carGroup = new LinkedList<MemberInfo>();
    private final LinkedList<MemberInfo> flightGroup = new LinkedList<MemberInfo>();
    private final LinkedList<MemberInfo> roomGroup = new LinkedList<MemberInfo>();
    
    /** 
     * Main program - starts the middleware. The middleware looks for the resource managers'
     * RMI servers and registers itself to the RMI registry on the machine. 
     * @param args
     */ 
    public static void main(String args[]) {
        String carMachine, flightMachine, roomMachine;
        String carServiceName, flightServiceName, roomServiceName;
        String myRole, myGroupName, myServiceName;
        
        carMachine = flightMachine = roomMachine = "localhost";

        HavocadoFlesh obj = null;
		if (args.length == 9) {
		    // A master is born...
		    myRole = args[0];
		    myServiceName = args[1];
		    myGroupName = args[2];
		    carMachine = args[3];
		    carServiceName = args[4];
		    flightMachine = args[5];
		    flightServiceName = args[6];
		    roomMachine = args[7];
		    roomServiceName = args[8];
		    boolean isMaster = myRole == MASTER_FLAG;
		    if(isMaster) {
			    // create the master:
			    try {
					obj = new HavocadoFlesh(true, myServiceName, myGroupName, 
							carMachine, carServiceName, flightMachine, flightServiceName, roomMachine, roomServiceName);
				} catch (Exception e) {
					System.out.println("Server Exception.");
					e.printStackTrace();
					System.exit(1);
				}
			} else {
				System.out.println("Wrong argument - first argument should be 'master' in this case.");
				System.exit(1);
			}
		} else if(args.length == 3) {
			// A poor slave is born...
			myRole = args[0];
			myServiceName = args[1];
			myGroupName = args[2];
			boolean isSlave = myRole == SLAVE_FLAG;
			if(isSlave) {
				// create the slave:
				try {
					obj = new HavocadoFlesh(false, myServiceName, myGroupName );
				} catch (Exception e) {
					System.out.println("Server Exception.");
					e.printStackTrace();
					System.exit(1);
				}
			} else {
				System.out.println("Wrong argument - first argument should be 'slave' in this case.");
				System.exit(1);
			}
		} else {
		    System.err.println ("Wrong usage");
		    System.out.println("Usage: ");
		    System.out.println("master: 'java ResImpl.HavocadoFlesh master <myRMIServiceName> <myGroupName> " +
		    		"<carMachineName> <carRMIServiceName> <flightMachineName> <flightRMIServiceName> <roomMachineName> <roomRMIServiceName>'");
		    System.out.println("slave: 'java ResImpl.HavocadoFlesh slave <myRMIServiceName> <myGroupName>'");
		    System.exit(1);
		}
    }
    
    
    private HavocadoFlesh(boolean isMaster, String myRMIServiceName, String groupName, 
    		String carMachine, String carRMIServiceName, 
    		String flightMachine, String flightRMIServiceName,
    		String roomMachine, String roomRMIServiceName) throws RemoteException, NotBoundException {
    	// Create the group member.
    	super(isMaster, myRMIServiceName, groupName);
    	
    	// Initialize the lock manager.
    	this.lm = new LockManager(this);
    	
    	// Initialize the overseer
    	this.overseer = new Overseer(this);
    	
    	// initialize the RMI service.
	    ResourceManager rm = (ResourceManager) UnicastRemoteObject.exportObject(this, 0);

	    // Bind the remote object's stub in the registry
	    Registry registry = LocateRegistry.getRegistry();
	    registry.rebind(myRMIServiceName, rm);
    	
	    if(this.isMaster){ 
	    	if(carMachine != null && carRMIServiceName != null
	    			&& flightMachine != null && flightRMIServiceName != null
	    			&& roomMachine != null && roomRMIServiceName != null) { 	    
	    		// cars.
	    	    ResourceManager rmCars;
	    	    registry = LocateRegistry.getRegistry(carMachine);
	    	    rmCars = (ResourceManager) registry.lookup(carRMIServiceName);
	    	    //this.carGroup.addAll(rmCars.getGroupMembers());
	    	    updateRMGroup(carGroup, rmCars.getGroupMembers());
	
	    	    // flights.
	    	    ResourceManager rmFlights;
	    	    registry = LocateRegistry.getRegistry(flightMachine);
	    	    rmFlights = (ResourceManager) registry.lookup(flightRMIServiceName);
	    	    //this.flightGroup.addAll(rmFlights.getGroupMembers());
	    	    updateRMGroup(flightGroup, rmFlights.getGroupMembers());
	    	    
	    	    // rooms.
	    	    ResourceManager rmRooms;
	    	    registry = LocateRegistry.getRegistry(roomMachine);
	    	    rmRooms = (ResourceManager) registry.lookup(roomRMIServiceName);
	    	    //his.roomGroup.addAll(rmRooms.getGroupMembers());
	    	    updateRMGroup(roomGroup, rmRooms.getGroupMembers());
	    	}
    	
	    	// Start the overseer thread if I am a master.
    		this.overseer.start();
    	}
    }
    
    // helper constructor for slaves.
    private HavocadoFlesh(boolean isMaster, String myRMIServiceName, String groupName) throws RemoteException, NotBoundException {
    	this(isMaster, myRMIServiceName, groupName, null, null, null, null, null, null);
    }
    
	@Override
	public void specialPromoteToMaster() {
		// If we have just been promoted to a master, start our overseer thread.
		this.overseer.refreshAllTransactionsTTL();
		this.overseer.start();
		
		// Send out a list of the updated RM group members
		refreshRMGroups();
		broadcastRMGroups();
	}

	@Override
	protected void specialReceive(Object arg0) {
		if(arg0 == null) {
			// do nothing.
			
		} else {
			if(arg0 instanceof MemberInfo && isMaster) {
				// If the object is a MemberInfo, and I am a master, it means a new slave has joined the group.
				// I therefore need to send to everyone the list of RM's to communicate to.
				broadcastRMGroups();
				
			} else if(arg0 instanceof ReplicationCommand && !isMaster) {
				// if the object is not null, and i am a slave, check if it is a replication command
				// and execute that replication command.
				ReplicationCommand c = (ReplicationCommand) arg0;
				c.execute(this);
				
			} else if (arg0 instanceof RMInfoMessage && !isMaster) {
				// if the object is not null, and i am a slave, check if it is a resource manager
				// update, and refresh my groups.
				updateRMGroups((RMInfoMessage) arg0);
			}
		}
	}
	
	/**
	 * Refresh our groups by contacting at least one available RM per group, and retrieving
	 * the new group members.
	 */
	private void refreshRMGroups() {
		// Cars.
		ResourceManager carRM = AbstractRMICommand.getAvailableRM(carGroup);
		if(carRM != null) {
			try {
				this.updateRMGroup(carGroup, carRM.getGroupMembers());
			} catch (RemoteException e) {
				System.out.println("Invalid carRM to refresh the car group.");
			}
		} else {
			System.out.println("no available carRM.");
		}
		
		// Flights.
		ResourceManager flightRM = AbstractRMICommand.getAvailableRM(flightGroup);
		if(flightRM != null) {
			try {
				this.updateRMGroup(flightGroup, flightRM.getGroupMembers());
			} catch (RemoteException e) {
				System.out.println("Invald flightRM to refresh the flight group.");
			}
		} else {
			System.out.println("no available flightRM.");
		}
		
		// Rooms.
		ResourceManager roomRM = AbstractRMICommand.getAvailableRM(roomGroup);
		if(roomRM != null) {
			try {
				this.updateRMGroup(roomGroup, roomRM.getGroupMembers());
			} catch (RemoteException e) {
				System.out.println("Invald roomRM to refresh the room group.");
			}
		} else {
			System.out.println("no available roomRM.");
		}
	}
	
	/**
	 * Broadcast my RM groups to the group.
	 */
	private void broadcastRMGroups() {
		RMInfoMessage rmsg = new RMInfoMessage();
		rmsg.setCarGroup(carGroup);
		rmsg.setFlightGroup(flightGroup);
		rmsg.setRoomGroup(roomGroup);
		// Broadcast the message.
		send(rmsg);
	}
	
	/**
	 * Update my RM groups from a message.
	 * @param rmsg
	 */
	private void updateRMGroups(RMInfoMessage rmsg) {
		updateRMGroup(carGroup, rmsg.getCarGroup());
		updateRMGroup(flightGroup, rmsg.getFlightGroup());
		updateRMGroup(roomGroup, rmsg.getRoomGroup());
	}
	
	/**
	 * Helper function: update the "old" RM group with the "next" group provided.
	 * @param old
	 * @param next
	 */
	private void updateRMGroup(LinkedList<MemberInfo> old, LinkedList<MemberInfo> next) {
		if(next == null || next.isEmpty()) { 
			System.out.println("empty or null next MemberInfo sent in updated.");
			return; 
		}
		
		// Add to our old list the memberInfo objects that were not previously there.
		for(MemberInfo m : next) {
			if(!old.contains(m)) {
				old.add(m);
			}
		}
		
		// Remove from our old list the memberInfo objects that are absent in next.
		LinkedList<MemberInfo> toRemove = new LinkedList<MemberInfo>();
		for(MemberInfo m : old) {
			if(!next.contains(m)) {
				toRemove.add(m);
			}
		}
		for(MemberInfo m : toRemove) {
			old.remove(m);
		}
	}
    
    /**
     * Called when a lock is set. Sends the appropriate replication
     * command to the group.
     * @param tId
     * @param lockType
     * @param resource
     */
    public void lockSet(int tId, int lockType, String resource) {
    	if(this.isMaster) {
    		ReplicationCommand r = new ReplicateSetLock(tId, lockType, resource);
    		this.send(r);
    	}
    }
    
    /**
     * Called when a transaction is created. Sends the appropriate replication
     * command to the group.
     * @param tId
     */
    void transactionCreated(int tId) {
    	if(this.isMaster) {
    		ReplicationCommand r = new ReplicateCreateTransaction(tId);
    		this.send(r);
    	}
    }
    
    /**
     * Called when a command is added to a transaction. Sends the appropriate
     * replication command to the group.
     * @param tId
     * @param pCommand
     */
    void commandAddedToTransaction(int tId, AbstractRMICommand pCommand) {
    	if(this.isMaster) {
    		ReplicationCommand r = new ReplicateAddCommand(tId, pCommand);
    		this.send(r);
    	}
    }
    
    /**
     * Called when a transaction is aborted. Sends the appropriate replication
     * command to the group.
     * @param tId
     */
    void transactionAborted(int tId) {
    	if(this.isMaster) {
    		ReplicationCommand r = new ReplicateAbortTransaction(tId);
    		this.send(r);
    	}
    }
    
    /**
     * Called when a transaction is committed. Sends the appropriate replication
     * command to the group.
     * @param tId
     */
    void transactionCommitted(int tId) {
    	if(this.isMaster) {
    		ReplicationCommand r = new ReplicateCommitTransaction(tId);
    		this.send(r);
    	}
    }

    // Create a new flight, or add seats to existing flight
    //  NOTE: if flightPrice <= 0 and the flight already exists, it maintains its current price
    public ReturnTuple<Boolean> addFlight(int id, int flightNum, int flightSeats, int flightPrice, Timestamp timestamp)
	throws RemoteException, TransactionAbortedException, InvalidTransactionException
    {
    	if(!isMaster) {
    		System.out.println("Slave retransmitting addFlight to master");
    		return this.getMaster().addFlight(id, flightNum, flightSeats, flightPrice, timestamp);
    	} else {
			timestamp.stamp();
			AddFlightRMICommand af = new AddFlightRMICommand(flightGroup, id, flightNum, flightSeats, flightPrice);
			af.setTimestampObject(timestamp);
			ReturnTuple<Boolean> result = null;
			try {
				overseer.validTransaction(id);
				lm.Lock(id, Flight.getKey(flightNum), af.getRequiredLock());
				af.execute();
				result = af.success;
				if(result.result)
					overseer.addCommandToTransaction(id, af);
			}
			catch (DeadlockException d) {
				timestamp.stamp();
				overseer.abort(id);
				timestamp.stamp();
				throw new TransactionAbortedException(timestamp);
			}
			catch (TransactionAbortedException tae) {
				tae.t = timestamp;
				throw tae;
			}
			catch (InvalidTransactionException ite) {
				ite.t = timestamp;
				throw ite;
			}
			result.timestamp.stamp();
			return result;
    	}
    }


	
    public ReturnTuple<Boolean> deleteFlight(int id, int flightNum, Timestamp timestamp)
	throws RemoteException, TransactionAbortedException, InvalidTransactionException
    {
    	if(!isMaster) {
    		System.out.println("Slave retransmitting deleteFlight to master");
    		return this.getMaster().deleteFlight(id, flightNum, timestamp);
    	} else {
	    	timestamp.stamp();
			DeleteFlightRMICommand df = new DeleteFlightRMICommand(flightGroup, id, flightNum);
			df.setTimestampObject(timestamp);
			ReturnTuple<Boolean> result = null;
			try {
				overseer.validTransaction(id);
				lm.Lock(id, Flight.getKey(flightNum), df.getRequiredLock());
				df.execute();
				result = df.success;
				if (result.result)
					overseer.addCommandToTransaction(id, df);
			}
			catch (DeadlockException d) {
				timestamp.stamp();
				overseer.abort(id);
				timestamp.stamp();
				throw new TransactionAbortedException(timestamp);
			}
			catch (TransactionAbortedException tae) {
				tae.t = timestamp;
				throw tae;
			}
			catch (InvalidTransactionException ite) {
				ite.t = timestamp;
				throw ite;
			}
			result.timestamp.stamp();
			return result;
    	}
    }



    // Create a new room location or add rooms to an existing location
    //  NOTE: if price <= 0 and the room location already exists, it maintains its current price
    public ReturnTuple<Boolean> addRooms(int id, String location, int count, int price, Timestamp timestamp)
	throws RemoteException, TransactionAbortedException, InvalidTransactionException
    {
    	if(!isMaster) {
    		System.out.println("Slave retransmitting addRooms to master");
    		return this.getMaster().addRooms(id, location, count, price, timestamp);
    	} else {
	    	timestamp.stamp();
			AddRoomsRMICommand ar = new AddRoomsRMICommand(roomGroup, id, location, count, price);
			ar.setTimestampObject(timestamp);
			ReturnTuple<Boolean> result = null;
			try {
				overseer.validTransaction(id);
				lm.Lock(id, Hotel.getKey(location), ar.getRequiredLock());
				ar.execute();
				result = ar.success;
				if (result.result)
					overseer.addCommandToTransaction(id, ar);
			}
			catch (DeadlockException d) {
				timestamp.stamp();
				overseer.abort(id);
				timestamp.stamp();
				throw new TransactionAbortedException(timestamp);
			}
			catch (TransactionAbortedException tae) {
				tae.t = timestamp;
				throw tae;
			}
			catch (InvalidTransactionException ite) {
				ite.t = timestamp;
				throw ite;
			}
			result.timestamp.stamp();
			return result;
    	}
    }

    // Delete rooms from a location
    public ReturnTuple<Boolean> deleteRooms(int id, String location, Timestamp timestamp)
	throws RemoteException, TransactionAbortedException, InvalidTransactionException
    {
    	if(!isMaster) {
    		System.out.println("Slave retransmitting deleteRooms to master");
    		return this.getMaster().deleteRooms(id, location, timestamp);
    	} else {
	    	timestamp.stamp();
			DeleteRoomsRMICommand dr = new DeleteRoomsRMICommand(roomGroup, id, location);
			dr.setTimestampObject(timestamp);
			ReturnTuple<Boolean> result = null;
			try {
				overseer.validTransaction(id);
				lm.Lock(id, Hotel.getKey(location), dr.getRequiredLock());
				dr.execute();
				result = dr.success;
				if (result.result)
					overseer.addCommandToTransaction(id, dr);
			}
			catch (DeadlockException d) {
				timestamp.stamp();
				overseer.abort(id);
				timestamp.stamp();
				throw new TransactionAbortedException(timestamp);
			}
			catch (TransactionAbortedException tae) {
				tae.t = timestamp;
				throw tae;
			}
			catch (InvalidTransactionException ite) {
				ite.t = timestamp;
				throw ite;
			}
			result.timestamp.stamp();
			return result;
    	}
    }

    // Create a new car location or add cars to an existing location
    //  NOTE: if price <= 0 and the location already exists, it maintains its current price
    public ReturnTuple<Boolean> addCars(int id, String location, int count, int price, Timestamp timestamp)
	throws RemoteException, TransactionAbortedException, InvalidTransactionException
    {
    	if(!isMaster) {
    		System.out.println("Slave retransmitting addCars to master");
    		return this.getMaster().addCars(id, location, count, price, timestamp);
    	} else {
	    	timestamp.stamp();
			AddCarsRMICommand ac = new AddCarsRMICommand(carGroup, id, location, count, price);
			ac.setTimestampObject(timestamp);
			ReturnTuple<Boolean> result = null;
			try {
				overseer.validTransaction(id);
				lm.Lock(id, Car.getKey(location), ac.getRequiredLock());
				ac.execute();
				result = ac.success;
				if (result.result)
					overseer.addCommandToTransaction(id, ac);
			}
			catch (DeadlockException d) {
				timestamp.stamp();
				overseer.abort(id);
				timestamp.stamp();
				throw new TransactionAbortedException(timestamp);
			}
			catch (TransactionAbortedException tae) {
				tae.t = timestamp;
				throw tae;
			}
			catch (InvalidTransactionException ite) {
				ite.t = timestamp;
				throw ite;
			}
			result.timestamp.stamp();
			return result;
    	}
    }


    // Delete cars from a location
    public ReturnTuple<Boolean> deleteCars(int id, String location, Timestamp timestamp)
	throws RemoteException, TransactionAbortedException, InvalidTransactionException
    {
    	if(!isMaster) {
    		System.out.println("Slave retransmitting deleteCars to master");
    		return this.getMaster().deleteCars(id, location, timestamp);
    	} else {
	    	timestamp.stamp();
			DeleteCarsRMICommand dc = new DeleteCarsRMICommand(carGroup, id, location);
			dc.setTimestampObject(timestamp);
			ReturnTuple<Boolean> result = null;
			try {
				overseer.validTransaction(id);
				lm.Lock(id, Car.getKey(location), dc.getRequiredLock());
				dc.execute();
				result = dc.success;
				if (result.result)
					overseer.addCommandToTransaction(id, dc);
			}
			catch (DeadlockException d) {
				timestamp.stamp();
				overseer.abort(id);
				timestamp.stamp();
				throw new TransactionAbortedException(timestamp);
			}
			catch (TransactionAbortedException tae) {
				tae.t = timestamp;
				throw tae;
			}
			catch (InvalidTransactionException ite) {
				ite.t = timestamp;
				throw ite;
			}
			result.timestamp.stamp();
			return result;
    	}
    }



    // Returns the number of empty seats on this flight
    public ReturnTuple<Integer> queryFlight(int id, int flightNum, Timestamp timestamp)
	throws RemoteException, TransactionAbortedException, InvalidTransactionException
    {
    	if(!isMaster) {
    		System.out.println("Slave retransmitting queryFlight to master");
    		return this.getMaster().queryFlight(id, flightNum, timestamp);
    	} else {
	    	timestamp.stamp();
			QueryFlightRMICommand qf = new QueryFlightRMICommand(flightGroup, id, flightNum);
			qf.setTimestampObject(timestamp);
			ReturnTuple<Integer> result = null;
			try {
				overseer.validTransaction(id);
				lm.Lock(id, Flight.getKey(flightNum), qf.getRequiredLock());
				qf.execute();
				result = qf.numSeats;
			}
			catch (DeadlockException d) {
				timestamp.stamp();
				overseer.abort(id);
				timestamp.stamp();
				throw new TransactionAbortedException(timestamp);
			}
			catch (TransactionAbortedException tae) {
				tae.t = timestamp;
				throw tae;
			}
			catch (InvalidTransactionException ite) {
				ite.t = timestamp;
				throw ite;
			}
			result.timestamp.stamp();
			return result;
    	}
    }

    // Returns the number of reservations for this flight. 
    //	public int queryFlightReservations(int id, int flightNum)
    //		throws RemoteException
    //	{
    //		Trace.info("RM::queryFlightReservations(" + id + ", #" + flightNum + ") called" );
    //		RMInteger numReservations = (RMInteger) readData( id, Flight.getNumReservationsKey(flightNum) );
    //		if( numReservations == null ) {
    //			numReservations = new RMInteger(0);
    //		} // if
    //		Trace.info("RM::queryFlightReservations(" + id + ", #" + flightNum + ") returns " + numReservations );
    //		return numReservations.getValue();
    //	}


    // Returns price of this flight
    public ReturnTuple<Integer> queryFlightPrice(int id, int flightNum, Timestamp timestamp )
	throws RemoteException, TransactionAbortedException, InvalidTransactionException
    {
    	if(!isMaster) {
    		System.out.println("Slave retransmitting queryFlightPrice to master");
    		return this.getMaster().queryFlightPrice(id, flightNum, timestamp);
    	} else {
	    	timestamp.stamp();
			QueryFlightPriceRMICommand qfp = new QueryFlightPriceRMICommand(flightGroup, id, flightNum);
			qfp.setTimestampObject(timestamp);
			ReturnTuple<Integer> result = null;
			try {
				overseer.validTransaction(id);
				lm.Lock(id, Flight.getKey(flightNum), qfp.getRequiredLock());
				qfp.execute();
				result = qfp.price;
			}
			catch (DeadlockException d) {
				timestamp.stamp();
				overseer.abort(id);
				timestamp.stamp();
				throw new TransactionAbortedException(timestamp);
			}
			catch (TransactionAbortedException tae) {
				tae.t = timestamp;
				throw tae;
			}
			catch (InvalidTransactionException ite) {
				ite.t = timestamp;
				throw ite;
			}
			result.timestamp.stamp();
			return result;
    	}
    }


    // Returns the number of rooms available at a location
    public ReturnTuple<Integer> queryRooms(int id, String location, Timestamp timestamp)
	throws RemoteException, TransactionAbortedException, InvalidTransactionException
    {
    	if(!isMaster) {
    		System.out.println("Slave retransmitting queryRooms to master");
    		return this.getMaster().queryRooms(id, location, timestamp);
    	} else {
	    	timestamp.stamp();
			QueryRoomsRMICommand qr = new QueryRoomsRMICommand(roomGroup, id, location);
			qr.setTimestampObject(timestamp);
			ReturnTuple<Integer> result = null;
			try {
				overseer.validTransaction(id);
				lm.Lock(id, Hotel.getKey(location), qr.getRequiredLock());
				qr.execute();
				result = qr.numRooms;
			}
			catch (DeadlockException d) {
				timestamp.stamp();
				overseer.abort(id);
				timestamp.stamp();
				throw new TransactionAbortedException(timestamp);
			}
			catch (TransactionAbortedException tae) {
				tae.t = timestamp;
				throw tae;
			}
			catch (InvalidTransactionException ite) {
				ite.t = timestamp;
				throw ite;
			}
			result.timestamp.stamp();
			return result;
    	}
    }


	
	
    // Returns room price at this location
    public ReturnTuple<Integer> queryRoomsPrice(int id, String location, Timestamp timestamp)
	throws RemoteException, TransactionAbortedException, InvalidTransactionException
    {
    	if(!isMaster) {
    		System.out.println("Slave retransmitting queryRoomsPrice to master");
    		return this.getMaster().queryRoomsPrice(id, location, timestamp);
    	} else {
	    	timestamp.stamp();
			QueryRoomsPriceRMICommand qrp = new QueryRoomsPriceRMICommand(roomGroup, id, location);
			qrp.setTimestampObject(timestamp);
			ReturnTuple<Integer> result = null;
			try {
				overseer.validTransaction(id);
				lm.Lock(id, Hotel.getKey(location), qrp.getRequiredLock());
				qrp.execute();
				result = qrp.price;
			}
			catch (DeadlockException d) {
				timestamp.stamp();
				overseer.abort(id);
				timestamp.stamp();
				throw new TransactionAbortedException(timestamp);
			}
			catch (TransactionAbortedException tae) {
				tae.t = timestamp;
				throw tae;
			}
			catch (InvalidTransactionException ite) {
				ite.t = timestamp;
				throw ite;
			}
			result.timestamp.stamp();
			return result;
    	}
    }


    // Returns the number of cars available at a location
    public ReturnTuple<Integer> queryCars(int id, String location, Timestamp timestamp)
	throws RemoteException, TransactionAbortedException, InvalidTransactionException
    {
    	if(!isMaster) {
    		System.out.println("Slave retransmitting queryCars to master");
    		return this.getMaster().queryCars(id, location, timestamp);
    	} else {
	    	timestamp.stamp();
			QueryCarsRMICommand qc = new QueryCarsRMICommand(carGroup, id, location);
			qc.setTimestampObject(timestamp);
			ReturnTuple<Integer> result = null;
			try {
				overseer.validTransaction(id);
				lm.Lock(id, Car.getKey(location), qc.getRequiredLock());
				qc.execute();
				result = qc.numCars;
			}
			catch (DeadlockException d) {
				timestamp.stamp();
				overseer.abort(id);
				timestamp.stamp();
				throw new TransactionAbortedException(timestamp);
			}
			catch (TransactionAbortedException tae) {
				tae.t = timestamp;
				throw tae;
			}
			catch (InvalidTransactionException ite) {
				ite.t = timestamp;
				throw ite;
			}
			result.timestamp.stamp();
			return result;
    	}
    }


    // Returns price of cars at this location
    public ReturnTuple<Integer> queryCarsPrice(int id, String location, Timestamp timestamp)
	throws RemoteException, TransactionAbortedException, InvalidTransactionException
    {
    	if(!isMaster) {
    		System.out.println("Slave retransmitting queryCarsPrice to master");
    		return this.getMaster().queryCarsPrice(id, location, timestamp);
    	} else {
			QueryCarsPriceRMICommand qcp = new QueryCarsPriceRMICommand(roomGroup, id, location);
			qcp.setTimestampObject(timestamp);
			ReturnTuple<Integer> result = null;
			try {
				overseer.validTransaction(id);
				lm.Lock(id, Car.getKey(location), qcp.getRequiredLock());
				qcp.execute();
				result = qcp.price;
			}
			catch (DeadlockException d) {
				timestamp.stamp();
				overseer.abort(id);
				timestamp.stamp();
				throw new TransactionAbortedException(timestamp);
			}
			catch (TransactionAbortedException tae) {
				tae.t = timestamp;
				throw tae;
			}
			catch (InvalidTransactionException ite) {
				ite.t = timestamp;
				throw ite;
			}
			result.timestamp.stamp();
			return result;
    	}
    }

    // return a bill
    public ReturnTuple<String> queryCustomerInfo(int id, int customerID, Timestamp timestamp)
	throws RemoteException, TransactionAbortedException, InvalidTransactionException
    {
    	if(!isMaster) {
    		System.out.println("Slave retransmitting queryCustomerInfo to master");
    		return this.getMaster().queryCustomerInfo(id, customerID, timestamp);
    	} else {
	    	timestamp.stamp();
			QueryCustomerInfoRMICommand qci = new QueryCustomerInfoRMICommand(carGroup, flightGroup, roomGroup, id, customerID);
			qci.setTimestampObject(timestamp);
			ReturnTuple<String> result = null;
			try {
				Vector<Integer> flightNos;
				Vector<String> locations;
				overseer.validTransaction(id);
				lm.Lock(id, Customer.getKey(customerID), qci.getRequiredLock());
				qci.execute();
				flightNos = qci.getCustomerFlightReservations();
				for (int flightNo : flightNos) {
					lm.Lock(id, Flight.getKey(flightNo), qci.getRequiredLock());
				}
				locations = qci.getCustomerRoomReservations();
				for (String location : locations) {
					lm.Lock(id, Hotel.getKey(location), qci.getRequiredLock());
				}
				locations = qci.getCustomerCarReservations();
				for (String location : locations) {
					lm.Lock(id, Car.getKey(location), qci.getRequiredLock());
				}
				qci.execute();
				result = qci.customerInfo;
			}
			catch (DeadlockException d) {
				timestamp.stamp();
				overseer.abort(id);
				timestamp.stamp();
				throw new TransactionAbortedException(timestamp);
			}
			catch (TransactionAbortedException tae) {
				tae.t = timestamp;
				throw tae;
			}
			catch (InvalidTransactionException ite) {
				ite.t = timestamp;
				throw ite;
			}
			result.timestamp.stamp();
			return result;
    	}
    }

    // customer functions
    // new customer just returns a unique customer identifier
	
    public ReturnTuple<Integer> newCustomer(int id, Timestamp timestamp)
	throws RemoteException, TransactionAbortedException, InvalidTransactionException
    {
    	if(!isMaster) {
    		System.out.println("Slave retransmitting newCustomer to master");
    		return this.getMaster().newCustomer(id, timestamp);
    	} else {
	    	timestamp.stamp();
			int rid = Integer.parseInt( String.valueOf(id) +
						    String.valueOf(Calendar.getInstance().get(Calendar.MILLISECOND)) +
						    String.valueOf( Math.round( Math.random() * 100 + 1 )));
			NewCustomerWithIdRMICommand nc = new NewCustomerWithIdRMICommand(carGroup, flightGroup, roomGroup, id, rid);
			nc.setTimestampObject(timestamp);
			ReturnTuple<Integer> result = null;
			try {
				overseer.validTransaction(id);
				lm.Lock(id, Customer.getKey(rid), nc.getRequiredLock());
				nc.execute();
				result = new ReturnTuple<Integer>(rid, nc.success.timestamp);
				overseer.addCommandToTransaction(id, nc);
			}
			catch (DeadlockException d) {
				timestamp.stamp();
				overseer.abort(id);
				timestamp.stamp();
				throw new TransactionAbortedException(timestamp);
			}
			catch (TransactionAbortedException tae) {
				tae.t = timestamp;
				throw tae;
			}
			catch (InvalidTransactionException ite) {
				ite.t = timestamp;
				throw ite;
			}
			result.timestamp.stamp();
			return result;
    	}
    }

    // I opted to pass in customerID instead. This makes testing easier
    public ReturnTuple<Boolean> newCustomer(int id, int customerID, Timestamp timestamp )
	throws RemoteException, TransactionAbortedException, InvalidTransactionException
    {
    	if(!isMaster) {
    		System.out.println("Slave retransmitting newCustomer to master");
    		return this.getMaster().newCustomer(id, customerID, timestamp);
    	} else {
	    	timestamp.stamp();
			NewCustomerWithIdRMICommand ncwi = new NewCustomerWithIdRMICommand(carGroup, flightGroup, roomGroup, id, customerID);
			ncwi.setTimestampObject(timestamp);
			ReturnTuple<Boolean> result = null;
			try {
				overseer.validTransaction(id);
				lm.Lock(id, Customer.getKey(customerID), ncwi.getRequiredLock());
				ncwi.execute();
				result = ncwi.success;
				if (result.result)
					overseer.addCommandToTransaction(id, ncwi);
			}
			catch (DeadlockException d) {
				timestamp.stamp();
				overseer.abort(id);
				timestamp.stamp();
				throw new TransactionAbortedException(timestamp);
			}
			catch (TransactionAbortedException tae) {
				tae.t = timestamp;
				throw tae;
			}
			catch (InvalidTransactionException ite) {
				ite.t = timestamp;
				throw ite;
			}
			result.timestamp.stamp();
			return result;
    	}
    }


    // Deletes customer from the database. 
    public ReturnTuple<Boolean> deleteCustomer(int id, int customerID, Timestamp timestamp)
	throws RemoteException, TransactionAbortedException, InvalidTransactionException
    {
    	if(!isMaster) {
    		System.out.println("Slave retransmitting deleteCustomer to master");
    		return this.getMaster().deleteCustomer(id, customerID, timestamp);
    	} else {
	    	timestamp.stamp();
			DeleteCustomerRMICommand dc = new DeleteCustomerRMICommand(carGroup, flightGroup, roomGroup, id, customerID);
			dc.setTimestampObject(timestamp);
			ReturnTuple<Boolean> result = null;
			try {
				overseer.validTransaction(id);
				lm.Lock(id, Customer.getKey(customerID), dc.getRequiredLock());
				dc.execute();
				result = dc.success;
				if (result.result)
					overseer.addCommandToTransaction(id, dc);
			}
			catch (DeadlockException d) {
				timestamp.stamp();
				overseer.abort(id);
				timestamp.stamp();
				throw new TransactionAbortedException(timestamp);
			}
			catch (TransactionAbortedException tae) {
				tae.t = timestamp;
				throw tae;
			}
			catch (InvalidTransactionException ite) {
				ite.t = timestamp;
				throw ite;
			}
			result.timestamp.stamp();
			return result;
    	}
    }




    // Frees flight reservation record. Flight reservation records help us make sure we
    //  don't delete a flight if one or more customers are holding reservations
    //	public boolean freeFlightReservation(int id, int flightNum)
    //		throws RemoteException
    //	{
    //		Trace.info("RM::freeFlightReservations(" + id + ", " + flightNum + ") called" );
    //		RMInteger numReservations = (RMInteger) readData( id, Flight.getNumReservationsKey(flightNum) );
    //		if( numReservations != null ) {
    //			numReservations = new RMInteger( Math.max( 0, numReservations.getValue()-1) );
    //		} // if
    //		writeData(id, Flight.getNumReservationsKey(flightNum), numReservations );
    //		Trace.info("RM::freeFlightReservations(" + id + ", " + flightNum + ") succeeded, this flight now has "
    //				+ numReservations + " reservations" );
    //		return true;
    //	}
    //	

	
    // Adds car reservation to this customer. 
    public ReturnTuple<Boolean> reserveCar(int id, int customerID, String location, Timestamp timestamp)
	throws RemoteException, TransactionAbortedException, InvalidTransactionException
    {
    	if(!isMaster) {
    		System.out.println("Slave retransmitting reserveCar to master");
    		return this.getMaster().reserveCar(id, customerID, location, timestamp);
    	} else {
	    	timestamp.stamp();
			ReserveCarRMICommand rc = new ReserveCarRMICommand(carGroup, id, customerID, location);
			rc.setTimestampObject(timestamp);
			ReturnTuple<Boolean> result = null;
			try {
				overseer.validTransaction(id);
				lm.Lock(id, Customer.getKey(customerID), rc.getRequiredLock());
				lm.Lock(id, Car.getKey(location), rc.getRequiredLock());
				rc.execute();
				result = rc.success;
				if (result.result)
					overseer.addCommandToTransaction(id, rc);
			}
			catch (DeadlockException d) {
				timestamp.stamp();
				overseer.abort(id);
				timestamp.stamp();
				throw new TransactionAbortedException(timestamp);
			}
			catch (TransactionAbortedException tae) {
				tae.t = timestamp;
				throw tae;
			}
			catch (InvalidTransactionException ite) {
				ite.t = timestamp;
				throw ite;
			}
			result.timestamp.stamp();
			return result;
    	}
    }


    // Adds room reservation to this customer. 
    public ReturnTuple<Boolean> reserveRoom(int id, int customerID, String location, Timestamp timestamp)
	throws RemoteException, TransactionAbortedException, InvalidTransactionException
    {
    	if(!isMaster) {
    		System.out.println("Slave retransmitting reserveRoom to master");
    		return this.getMaster().reserveRoom(id, customerID, location, timestamp);
    	} else {
	    	timestamp.stamp();
			ReserveRoomRMICommand rr = new ReserveRoomRMICommand(roomGroup, id, customerID, location);
			rr.setTimestampObject(timestamp);
			ReturnTuple<Boolean> result = null;
			try {
				overseer.validTransaction(id);
				lm.Lock(id, Customer.getKey(customerID), rr.getRequiredLock());
				lm.Lock(id, Hotel.getKey(location), rr.getRequiredLock());
				rr.execute();
				result = rr.success;
				if (result.result)
					overseer.addCommandToTransaction(id, rr);
			}
			catch (DeadlockException d) {
				timestamp.stamp();
				overseer.abort(id);
				timestamp.stamp();
				throw new TransactionAbortedException(timestamp);
			}
			catch (TransactionAbortedException tae) {
				tae.t = timestamp;
				throw tae;
			}
			catch (InvalidTransactionException ite) {
				ite.t = timestamp;
				throw ite;
			}
			result.timestamp.stamp();
			return result;
    	}
    }
    
    
    // Adds flight reservation to this customer.  
    public ReturnTuple<Boolean> reserveFlight(int id, int customerID, int flightNum, Timestamp timestamp)
	throws RemoteException, TransactionAbortedException, InvalidTransactionException
    {
    	if(!isMaster) {
    		System.out.println("Slave retransmitting reserveFlight to master");
    		return this.getMaster().reserveFlight(id, customerID, flightNum, timestamp);
    	} else {
	    	timestamp.stamp();
			ReserveFlightRMICommand rf = new ReserveFlightRMICommand(flightGroup, id, customerID, flightNum);
			rf.setTimestampObject(timestamp);
			ReturnTuple<Boolean> result = null;
			try {
				overseer.validTransaction(id);
				lm.Lock(id, Customer.getKey(customerID), rf.getRequiredLock());
				lm.Lock(id, Flight.getKey(flightNum), rf.getRequiredLock());
				rf.execute();
				result = rf.success;
				if (result.result)
					overseer.addCommandToTransaction(id, rf);
			}
			catch (DeadlockException d) {
				timestamp.stamp();
				overseer.abort(id);
				timestamp.stamp();
				throw new TransactionAbortedException(timestamp);
			}
			catch (TransactionAbortedException tae) {
				tae.t = timestamp;
				throw tae;
			}
			catch (InvalidTransactionException ite) {
				ite.t = timestamp;
				throw ite;
			}
			result.timestamp.stamp();
			return result;
    	}
    }
	
    /* reserve an itinerary */
    public ReturnTuple<Boolean> itinerary(int id,int customer,Vector flightNumbers,String location,boolean Car,boolean Room, Timestamp timestamp)
	throws RemoteException, TransactionAbortedException, InvalidTransactionException {
    	if(!isMaster) {
    		System.out.println("Slave retransmitting itinerary to master");
    		return this.getMaster().itinerary(id, customer, flightNumbers, location, Car, Room, timestamp);
    	} else {
	    	timestamp.stamp();
			ItineraryRMICommand i = new ItineraryRMICommand(carGroup, flightGroup, roomGroup, id, customer, flightNumbers, location, Car, Room);
			i.setTimestampObject(timestamp);
			ReturnTuple<Boolean> result = null;
			try {
				overseer.validTransaction(id);
				lm.Lock(id, Customer.getKey(customer), i.getRequiredLock());
				lm.Lock(id, ResImpl.Car.getKey(location), i.getRequiredLock());
				lm.Lock(id, Hotel.getKey(location), i.getRequiredLock());
				for (Object flightNo : flightNumbers) {
					lm.Lock(id, Flight.getKey(((Integer)flightNo).intValue()), i.getRequiredLock());
				}
				i.execute();
				result = i.success;
				if (result.result)
					overseer.addCommandToTransaction(id, i);
			}
			catch (DeadlockException d) {
				timestamp.stamp();
				overseer.abort(id);
				timestamp.stamp();
				throw new TransactionAbortedException(timestamp);
			}
			catch (TransactionAbortedException tae) {
				tae.t = timestamp;
				throw tae;
			}
			catch (InvalidTransactionException ite) {
				ite.t = timestamp;
				throw ite;
			}
			result.timestamp.stamp();
			return result;
    	}
    }


	public ReturnTuple<Object> abort(int id, Timestamp timestamp) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
		if(!isMaster) {
			System.out.println("Slave retransmitting abort to master");
			return this.getMaster().abort(id, timestamp);
		} else {
			timestamp.stamp();
			overseer.abort(id);
			timestamp.stamp();
			return new ReturnTuple<Object>(null, timestamp);
		}
	}


	public ReturnTuple<Boolean> commit(int id, Timestamp timestamp) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
		if(!isMaster) {
			System.out.println("Slave retransmitting commit to master");
			return this.getMaster().commit(id, timestamp);
		} else {
			timestamp.stamp();
			boolean result = overseer.commit(id);
			timestamp.stamp();
			return new ReturnTuple<Boolean>(result, timestamp);
		}
	}
	
	private void shutdownGroup(LinkedList<MemberInfo> members) {
		for(MemberInfo m: members) {
			shutdownMember(m);
		}
	}
	
	/**
	 * Shutdown a specific member.
	 * @param m
	 */
	private void shutdownMember(MemberInfo m) {
		ResourceManager rm = GroupMember.memberInfoToResourceManager(m);
		if(rm != null) {
			try{
				rm.shutdown(FORCE_SHUTDOWN);
			} catch(Exception e) {
				// do nothing.
			}
		}
	}
	
	public boolean shutdown(String server) throws RemoteException {
		if (server.equalsIgnoreCase(FORCE_SHUTDOWN)) {
			System.exit(0);
			return true;
			
		} else if (server.equalsIgnoreCase("middleware")) {
			// Shutdown the cars group.
			shutdownGroup(this.carGroup);
			
			// Shutdown the rooms group.
			shutdownGroup(this.roomGroup);
			
			// Shutdown the flights group.
			shutdownGroup(this.flightGroup);
			
			// Shutdown all the middleware servers except yourself.
			for(MemberInfo m: this.currentMembers) {
				if(!m.equals(this.myInfo)){
					shutdownMember(m);
				}
			}
			
			// Shut yourself down.
			System.exit(0);
			return true;
			
		} else if (server.equalsIgnoreCase("cars")) {
			shutdownGroup(this.carGroup);
			return true;
			
		} else if (server.equalsIgnoreCase("rooms")) {
			shutdownGroup(this.roomGroup);
			return true;
			
		} else if (server.equalsIgnoreCase("flights")) {
			shutdownGroup(this.flightGroup);
			return true;
			
		} else {
			return false;
		}
	}


	public ReturnTuple<Integer> start(Timestamp timestamp) throws RemoteException {
		if(!isMaster) {
			System.out.println("Slave retransmitting start to master");
			return this.getMaster().start(timestamp);
		} else {
			timestamp.stamp();
			int tId;
			tId = overseer.createTransaction(lm);
			timestamp.stamp();
			return new ReturnTuple<Integer>(tId, timestamp);
		}
	}


	public ReturnTuple<Object> unreserveCar(int id, int customer, String location, Timestamp timestamp) throws RemoteException {
		// DO NOTHING.
		return null;
	}


	public ReturnTuple<Object> unreserveFlight(int id, int customer, int flightNumber, Timestamp timestamp) throws RemoteException {
		// DO NOTHING.
		return null;
	}


	public ReturnTuple<Object> unreserveRoom(int id, int customer, String locationd, Timestamp timestamp) throws RemoteException {
		// DO NOTHING.
		return null;
	}


	public ReturnTuple<Object> setCars(int id, String location, int count, int price, Timestamp timestamp) throws RemoteException {
		// DO NOTHING.
		return null;
	}


	public ReturnTuple<Object> setFlight(int id, int flightNum, int count, int price, Timestamp timestamp) throws RemoteException {
		// DO NOTHING.
		return null;
	}


	public ReturnTuple<Object> setRooms(int id, String location, int count, int price, Timestamp timestamp) throws RemoteException {
		// DO NOTHING.
		return null;
	}
    
    public ReturnTuple<Vector<String>> customerCarReservations(int id, int customer, Timestamp timestamp) throws RemoteException {
    	// DO NOTHING.
    	return null;
    }
    
    public ReturnTuple<Vector<Integer>> customerFlightReservations(int id, int customer, Timestamp timestamp) throws RemoteException {
    	// DO NOTHING.
    	return null;
    }
    
    public ReturnTuple<Vector<String>> customerRoomReservations(int id, int customer, Timestamp timestamp) throws RemoteException {
    	// DO NOTHING.
    	return null;
    }


	public void crash() throws RemoteException {
		System.out.println("CRASHING.");
		System.exit(-1);
	}

	public LinkedList<MemberInfo> getGroupMembers() throws RemoteException {
		return this.currentMembers;
	}

	@Override
	public void poke() throws RemoteException {
		// do nothing.
	}
}
