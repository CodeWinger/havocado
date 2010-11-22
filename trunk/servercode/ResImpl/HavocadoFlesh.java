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

import LockManager.DeadlockException;
import LockManager.LockManager;


//public class HavocadoFlesh extends java.rmi.server.UnicastRemoteObject
public class HavocadoFlesh 
	extends GroupMember 
    implements ResourceManager {
	
	/** Static resource manager references */
    static ResourceManager rmCars, rmFlights, rmRooms;

    /** The middleware's lock manager. All lock management is done on the middleware side */
    private LockManager lm = new LockManager();
    
    /** The middleware's transaction manager. Handles keeping track of transactions. */
    private Overseer overseer = new Overseer();

    /** 
     * Main program - starts the middleware. The middleware looks for the resource managers'
     * RMI servers and registers itself to the RMI registry on the machine. 
     * @param args
     */ 
    public static void main(String args[]) {
        // Figure out where server is running
		String carSeed, flightSeed, roomSeed;
		carSeed = flightSeed = roomSeed = "localhost";

		if (args.length == 3) {
		    //port = Integer.parseInt(args[0]);
		    carSeed = args[0];
		    flightSeed = args[1];
		    roomSeed = args[2];
		} else {
		    System.err.println ("Wrong usage");
		    System.out.println("Usage: ");
		    System.out.println("'java ResImpl.HavocadoFlesh <car server host> <flight server host> <room server host>' - Uses the specified host names or IPs to find the resource managers.");
		    System.exit(1);
		}
		
		// Set up RMI.	 
		HavocadoFlesh obj = null;
		try {
		    // create a new Server object
		    obj = new HavocadoFlesh();
		    // dynamically generate the stub (client proxy)
		    ResourceManager rm = (ResourceManager) UnicastRemoteObject.exportObject(obj, 0);
	
		    // Bind the remote object's stub in the registry
		    Registry registry = LocateRegistry.getRegistry();
		    registry.rebind("HavocadoFlesh", rm);
	
		    registry = LocateRegistry.getRegistry(carSeed);
		    rmCars = (ResourceManager) registry.lookup("HavocadoSeedCar");
		    // TODO: Check for null rm.
		    registry = LocateRegistry.getRegistry(flightSeed);
		    rmFlights = (ResourceManager) registry.lookup("HavocadoSeedFlight");
		    // TODO: Check for null rm.
		    registry = LocateRegistry.getRegistry(roomSeed);
		    rmRooms = (ResourceManager) registry.lookup("HavocadoSeedRoom");
		    // TODO: Check for null rm.
		    
		    // Start the overseer thread
		    obj.overseer.start();
		} 
		catch (Exception e) 
	    {
			System.err.println("Server exception: " + e.toString());
			e.printStackTrace();
	    }
    }
    
    
    public HavocadoFlesh() throws RemoteException {
    }

    // Create a new flight, or add seats to existing flight
    //  NOTE: if flightPrice <= 0 and the flight already exists, it maintains its current price
    public ReturnTuple<Boolean> addFlight(int id, int flightNum, int flightSeats, int flightPrice, Timestamp timestamp)
	throws RemoteException, TransactionAbortedException, InvalidTransactionException
    {
	timestamp.stamp();
	AddFlightRMICommand af = new AddFlightRMICommand(rmFlights, id, flightNum, flightSeats, flightPrice);
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


	
    public ReturnTuple<Boolean> deleteFlight(int id, int flightNum, Timestamp timestamp)
	throws RemoteException, TransactionAbortedException, InvalidTransactionException
    {
    	timestamp.stamp();
		DeleteFlightRMICommand df = new DeleteFlightRMICommand(rmFlights, id, flightNum);
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



    // Create a new room location or add rooms to an existing location
    //  NOTE: if price <= 0 and the room location already exists, it maintains its current price
    public ReturnTuple<Boolean> addRooms(int id, String location, int count, int price, Timestamp timestamp)
	throws RemoteException, TransactionAbortedException, InvalidTransactionException
    {
    	timestamp.stamp();
		AddRoomsRMICommand ar = new AddRoomsRMICommand(rmRooms, id, location, count, price);
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

    // Delete rooms from a location
    public ReturnTuple<Boolean> deleteRooms(int id, String location, Timestamp timestamp)
	throws RemoteException, TransactionAbortedException, InvalidTransactionException
    {
    	timestamp.stamp();
		DeleteRoomsRMICommand dr = new DeleteRoomsRMICommand(rmRooms, id, location);
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

    // Create a new car location or add cars to an existing location
    //  NOTE: if price <= 0 and the location already exists, it maintains its current price
    public ReturnTuple<Boolean> addCars(int id, String location, int count, int price, Timestamp timestamp)
	throws RemoteException, TransactionAbortedException, InvalidTransactionException
    {
    	timestamp.stamp();
		AddCarsRMICommand ac = new AddCarsRMICommand(rmCars, id, location, count, price);
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


    // Delete cars from a location
    public ReturnTuple<Boolean> deleteCars(int id, String location, Timestamp timestamp)
	throws RemoteException, TransactionAbortedException, InvalidTransactionException
    {
    	timestamp.stamp();
		DeleteCarsRMICommand dc = new DeleteCarsRMICommand(rmCars, id, location);
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



    // Returns the number of empty seats on this flight
    public ReturnTuple<Integer> queryFlight(int id, int flightNum, Timestamp timestamp)
	throws RemoteException, TransactionAbortedException, InvalidTransactionException
    {
    	timestamp.stamp();
		QueryFlightRMICommand qf = new QueryFlightRMICommand(rmFlights, id, flightNum);
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
    	timestamp.stamp();
		QueryFlightPriceRMICommand qfp = new QueryFlightPriceRMICommand(rmFlights, id, flightNum);
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


    // Returns the number of rooms available at a location
    public ReturnTuple<Integer> queryRooms(int id, String location, Timestamp timestamp)
	throws RemoteException, TransactionAbortedException, InvalidTransactionException
    {
    	timestamp.stamp();
		QueryRoomsRMICommand qr = new QueryRoomsRMICommand(rmRooms, id, location);
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


	
	
    // Returns room price at this location
    public ReturnTuple<Integer> queryRoomsPrice(int id, String location, Timestamp timestamp)
	throws RemoteException, TransactionAbortedException, InvalidTransactionException
    {
    	timestamp.stamp();
		QueryRoomsPriceRMICommand qrp = new QueryRoomsPriceRMICommand(rmRooms, id, location);
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


    // Returns the number of cars available at a location
    public ReturnTuple<Integer> queryCars(int id, String location, Timestamp timestamp)
	throws RemoteException, TransactionAbortedException, InvalidTransactionException
    {
    	timestamp.stamp();
		QueryCarsRMICommand qc = new QueryCarsRMICommand(rmCars, id, location);
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


    // Returns price of cars at this location
    public ReturnTuple<Integer> queryCarsPrice(int id, String location, Timestamp timestamp)
	throws RemoteException, TransactionAbortedException, InvalidTransactionException
    {
		QueryCarsPriceRMICommand qcp = new QueryCarsPriceRMICommand(rmRooms, id, location);
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

    // return a bill
    public ReturnTuple<String> queryCustomerInfo(int id, int customerID, Timestamp timestamp)
	throws RemoteException, TransactionAbortedException, InvalidTransactionException
    {
    	timestamp.stamp();
		QueryCustomerInfoRMICommand qci = new QueryCustomerInfoRMICommand(rmCars, rmFlights, rmRooms, id, customerID);
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

    // customer functions
    // new customer just returns a unique customer identifier
	
    public ReturnTuple<Integer> newCustomer(int id, Timestamp timestamp)
	throws RemoteException, TransactionAbortedException, InvalidTransactionException
    {
    	timestamp.stamp();
		int rid = Integer.parseInt( String.valueOf(id) +
					    String.valueOf(Calendar.getInstance().get(Calendar.MILLISECOND)) +
					    String.valueOf( Math.round( Math.random() * 100 + 1 )));
		NewCustomerWithIdRMICommand nc = new NewCustomerWithIdRMICommand(rmCars, rmFlights, rmRooms, id, rid);
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

    // I opted to pass in customerID instead. This makes testing easier
    public ReturnTuple<Boolean> newCustomer(int id, int customerID, Timestamp timestamp )
	throws RemoteException, TransactionAbortedException, InvalidTransactionException
    {
    	timestamp.stamp();
		NewCustomerWithIdRMICommand ncwi = new NewCustomerWithIdRMICommand(rmCars, rmFlights, rmRooms, id, customerID);
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


    // Deletes customer from the database. 
    public ReturnTuple<Boolean> deleteCustomer(int id, int customerID, Timestamp timestamp)
	throws RemoteException, TransactionAbortedException, InvalidTransactionException
    {
    	timestamp.stamp();
		DeleteCustomerRMICommand dc = new DeleteCustomerRMICommand(rmCars, rmFlights, rmRooms, id, customerID);
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
    	timestamp.stamp();
		ReserveCarRMICommand rc = new ReserveCarRMICommand(rmCars, id, customerID, location);
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


    // Adds room reservation to this customer. 
    public ReturnTuple<Boolean> reserveRoom(int id, int customerID, String location, Timestamp timestamp)
	throws RemoteException, TransactionAbortedException, InvalidTransactionException
    {
    	timestamp.stamp();
		ReserveRoomRMICommand rr = new ReserveRoomRMICommand(rmRooms, id, customerID, location);
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
    
    
    // Adds flight reservation to this customer.  
    public ReturnTuple<Boolean> reserveFlight(int id, int customerID, int flightNum, Timestamp timestamp)
	throws RemoteException, TransactionAbortedException, InvalidTransactionException
    {
    	timestamp.stamp();
		ReserveFlightRMICommand rf = new ReserveFlightRMICommand(rmFlights, id, customerID, flightNum);
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
	
    /* reserve an itinerary */
    public ReturnTuple<Boolean> itinerary(int id,int customer,Vector flightNumbers,String location,boolean Car,boolean Room, Timestamp timestamp)
	throws RemoteException, TransactionAbortedException, InvalidTransactionException {
    	timestamp.stamp();
		ItineraryRMICommand i = new ItineraryRMICommand(rmCars, rmFlights, rmRooms, id, customer, flightNumbers, location, Car, Room);
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


	public ReturnTuple<Object> abort(int id, Timestamp timestamp) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
		timestamp.stamp();
		overseer.abort(id);
		timestamp.stamp();
		return new ReturnTuple<Object>(null, timestamp);
	}


	public ReturnTuple<Boolean> commit(int id, Timestamp timestamp) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
		timestamp.stamp();
		boolean result = overseer.commit(id);
		timestamp.stamp();
		return new ReturnTuple<Boolean>(result, timestamp);
	}


	public boolean shutdown(String server) throws RemoteException {
		if (server.equalsIgnoreCase("middleware")) {
			try {
				rmCars.shutdown(null);
			} catch (Exception e) {
				// nothing
			}
			try {
				rmRooms.shutdown(null);
			} catch (Exception e) {
				// nothing
			}
			try {
				rmFlights.shutdown(null);
			} catch (Exception e) {
				// nothing
			}
			System.exit(0);
		}
		else if (server.equalsIgnoreCase("cars"))
			rmCars.shutdown(null);
		else if (server.equalsIgnoreCase("rooms"))
			rmRooms.shutdown(null);
		else if (server.equalsIgnoreCase("flights"))
			rmFlights.shutdown(null);
		return false;
	}


	public ReturnTuple<Integer> start(Timestamp timestamp) throws RemoteException {
		timestamp.stamp();
		int tId;
		tId = overseer.createTransaction(lm);
		timestamp.stamp();
		return new ReturnTuple<Integer>(tId, timestamp);
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


	public void crash(MemberInfo m) throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	public List<MemberInfo> getGroupMembers() throws RemoteException {
		return this.;
	}

}
