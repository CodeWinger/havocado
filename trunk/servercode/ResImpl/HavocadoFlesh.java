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

import Commands.*;
import Commands.RMICommands.*;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import LockManager.LockManager;

import exceptions.InvalidTransactionException;
import exceptions.TransactionAbortedException;

//public class HavocadoFlesh extends java.rmi.server.UnicastRemoteObject
public class HavocadoFlesh
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
	throws RemoteException
    {
	AddFlightRMICommand af = new AddFlightRMICommand(rmFlights, id, flightNum, flightSeats, flightPrice);
	// TODO: toSeeds.add(af);
	af.waitFor();
	if (af.error())
	    throw new RemoteException();
	return new ReturnTuple<Boolean>(af.success, null) ;
    }


	
    public ReturnTuple<Boolean> deleteFlight(int id, int flightNum, Timestamp timestamp)
	throws RemoteException
    {
	DeleteFlightRMICommand df = new DeleteFlightRMICommand(rmFlights, id, flightNum);
	// TODO: toSeeds.add(df);
	df.waitFor();
	if (df.error())
	    throw new RemoteException();
	return new ReturnTuple<Boolean>(df.success, timestamp);  // TODO: TIMESTAMP LOGIC.
    }



    // Create a new room location or add rooms to an existing location
    //  NOTE: if price <= 0 and the room location already exists, it maintains its current price
    public ReturnTuple<Boolean> addRooms(int id, String location, int count, int price, Timestamp timestamp)
	throws RemoteException
    {
	AddRoomsRMICommand ar = new AddRoomsRMICommand(rmRooms, id, location, count, price);
	// TODO: toSeeds.add(ar);
	ar.waitFor();
	if (ar.error())
	    throw new RemoteException();
	return new ReturnTuple<Boolean>(ar.success, timestamp);  // TODO: TIMESTAMP LOGIC.
    }

    // Delete rooms from a location
    public ReturnTuple<Boolean> deleteRooms(int id, String location, Timestamp timestamp)
	throws RemoteException
    {
	DeleteRoomsRMICommand dr = new DeleteRoomsRMICommand(rmRooms, id, location);
	// TODO: toSeeds.add(dr);
	dr.waitFor();
	if (dr.error())
	    throw new RemoteException();
	return new ReturnTuple<Boolean>(dr.success, timestamp); // TODO: TIMESTAMP LOGIC.
		
    }

    // Create a new car location or add cars to an existing location
    //  NOTE: if price <= 0 and the location already exists, it maintains its current price
    public ReturnTuple<Boolean> addCars(int id, String location, int count, int price, Timestamp timestamp)
	throws RemoteException
    {
	AddCarsRMICommand ac = new AddCarsRMICommand(rmCars, id, location, count, price);
	// TODO: toSeeds.add(ac);
	ac.waitFor();
	if (ac.error())
	    throw new RemoteException();
	return new ReturnTuple<Boolean>(ac.success, timestamp);  // TODO: TIMESTAMP LOGIC.
    }


    // Delete cars from a location
    public ReturnTuple<Boolean> deleteCars(int id, String location, Timestamp timestamp)
	throws RemoteException
    {
	DeleteCarsRMICommand dc = new DeleteCarsRMICommand(rmCars, id, location);
	// TODO: toSeeds.add(dc);
	dc.waitFor();
	if (dc.error())
	    throw new RemoteException();
	return new ReturnTuple<Boolean>(dc.success, timestamp); // TODO: TIMESTAMP LOGIC.
    }



    // Returns the number of empty seats on this flight
    public ReturnTuple<Integer> queryFlight(int id, int flightNum, Timestamp timestamp)
	throws RemoteException
    {
	QueryFlightRMICommand qf = new QueryFlightRMICommand(rmFlights, id, flightNum);
	// TODO: toSeeds.add(qf);
	qf.waitFor();
	if (qf.error())
	    throw new RemoteException();
	return new ReturnTuple<Integer>(qf.numSeats, timestamp); // TODO: TIMESTAMP LOGIC.
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
	throws RemoteException
    {
	QueryFlightPriceRMICommand qfp = new QueryFlightPriceRMICommand(rmFlights, id, flightNum);
	// TODO: toSeeds.add(qfp);
	qfp.waitFor();
	if (qfp.error())
	    throw new RemoteException();
	return new ReturnTuple<Integer>(qfp.price, timestamp); // TODO: TIMESTAMP LOGIC.
    }


    // Returns the number of rooms available at a location
    public ReturnTuple<Integer> queryRooms(int id, String location, Timestamp timestamp)
	throws RemoteException
    {
	QueryRoomsRMICommand qr = new QueryRoomsRMICommand(rmRooms, id, location);
	// TODO: toSeeds.add(qr);
	qr.waitFor();
	if (qr.error())
	    throw new RemoteException();
	return new ReturnTuple<Integer>(qr.numRooms, timestamp); // TODO: TIMESTAMP LOGIC.
    }


	
	
    // Returns room price at this location
    public ReturnTuple<Integer> queryRoomsPrice(int id, String location, Timestamp timestamp)
	throws RemoteException
    {
	QueryRoomsPriceRMICommand qrp = new QueryRoomsPriceRMICommand(rmRooms, id, location);
	// TODO: toSeeds.add(qrp);
	qrp.waitFor();
	if (qrp.error())
	    throw new RemoteException();
	return new ReturnTuple<Integer>(qrp.price, timestamp); // TODO: TIMESTAMP LOGIC.
    }


    // Returns the number of cars available at a location
    public ReturnTuple<Integer> queryCars(int id, String location, Timestamp timestamp)
	throws RemoteException
    {
	QueryCarsRMICommand qc = new QueryCarsRMICommand(rmCars, id, location);
	// TODO: toSeeds.add(qc);
	qc.waitFor();
	if (qc.error())
	    throw new RemoteException();
	return new ReturnTuple<Integer>(qc.numCars, timestamp); // TODO: TIMESTAMP LOGIC.
    }


    // Returns price of cars at this location
    public ReturnTuple<Integer> queryCarsPrice(int id, String location, Timestamp timestamp)
	throws RemoteException
    {
	QueryCarsPriceRMICommand qcp = new QueryCarsPriceRMICommand(rmRooms, id, location);
	// TODO: toSeeds.add(qcp);
	qcp.waitFor();
	if (qcp.error())
	    throw new RemoteException();
	return new ReturnTuple<Integer>(qcp.price, timestamp); // TODO: TIMESTAMP LOGIC.
    }

    // return a bill
    public ReturnTuple<String> queryCustomerInfo(int id, int customerID, Timestamp timestamp)
	throws RemoteException
    {
	QueryCustomerInfoRMICommand qci = new QueryCustomerInfoRMICommand(rmCars, rmFlights, rmRooms, id, customerID);
	// TODO: toSeeds.add(qci);
	qci.waitFor();
	if (qci.error())
	    throw new RemoteException();
	return new ReturnTuple<String>(qci.customerInfo, timestamp); // TODO: TIMESTAMP LOGIC.
    }

    // customer functions
    // new customer just returns a unique customer identifier
	
    public ReturnTuple<Integer> newCustomer(int id, Timestamp timestamp)
	throws RemoteException
    {
	int rid = Integer.parseInt( String.valueOf(id) +
				    String.valueOf(Calendar.getInstance().get(Calendar.MILLISECOND)) +
				    String.valueOf( Math.round( Math.random() * 100 + 1 )));
	NewCustomerWithIdRMICommand nc = new NewCustomerWithIdRMICommand(rmCars, rmFlights, rmRooms, id, rid);
	// TODO: toSeeds.add(nc);
	nc.waitFor();
	if (nc.error())
	    throw new RemoteException();
	return new ReturnTuple<Integer>(rid, timestamp);  // TODO: TIMESTAMP LOGIC.
    }

    // I opted to pass in customerID instead. This makes testing easier
    public ReturnTuple<Boolean> newCustomer(int id, int customerID, Timestamp timestamp )
	throws RemoteException
    {
	NewCustomerWithIdRMICommand ncwi = new NewCustomerWithIdRMICommand(rmCars, rmFlights, rmRooms, id, customerID);
	// TODO: toSeeds.add(ncwi);
	ncwi.waitFor();
	if (ncwi.error())
	    throw new RemoteException();
	return new ReturnTuple<Boolean>(ncwi.success, timestamp); // TODO: TIMESTAMP LOGIC.
    }


    // Deletes customer from the database. 
    public ReturnTuple<Boolean> deleteCustomer(int id, int customerID, Timestamp timestamp)
	throws RemoteException
    {
	DeleteCustomerRMICommand dc = new DeleteCustomerRMICommand(rmCars, rmFlights, rmRooms, id, customerID);
	// TODO: toSeeds.add(dc);
	dc.waitFor();
	if(dc.error())
	    throw new RemoteException();
	return new ReturnTuple<Boolean>(dc.success, timestamp); // TODO: TIMESTAMP LOGIC.
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
	throws RemoteException
    {
	ReserveCarRMICommand rc = new ReserveCarRMICommand(rmCars, id, customerID, location);
	// TODO: toSeeds.add(rc);
	rc.waitFor();
	if (rc.error())
	    throw new RemoteException();
	return new ReturnTuple<Boolean>(rc.success, timestamp); // TODO: TIMESTAMP LOGIC.
    }


    // Adds room reservation to this customer. 
    public ReturnTuple<Boolean> reserveRoom(int id, int customerID, String location, Timestamp timestamp)
	throws RemoteException
    {
	ReserveRoomRMICommand rr = new ReserveRoomRMICommand(rmRooms, id, customerID, location);
	// TODO: toSeeds.add(rr);
	rr.waitFor();
	if (rr.error())
	    throw new RemoteException();
	return new ReturnTuple<Boolean>(rr.success, timestamp); // TODO: TIMESTAMP LOGIC.
    }
    // Adds flight reservation to this customer.  
    public ReturnTuple<Boolean> reserveFlight(int id, int customerID, int flightNum, Timestamp timestamp)
	throws RemoteException
    {
	ReserveFlightRMICommand rf = new ReserveFlightRMICommand(rmFlights, id, customerID, flightNum);
	// TODO: toSeeds.add(rf);
	rf.waitFor();
	if (rf.error())
	    throw new RemoteException();
	return new ReturnTuple<Boolean>(rf.success, timestamp); // TODO: TIMESTAMP LOGIC.
    }
	
    /* reserve an itinerary */
    public ReturnTuple<Boolean> itinerary(int id,int customer,Vector flightNumbers,String location,boolean Car,boolean Room, Timestamp timestamp)
	throws RemoteException {
	ItineraryRMICommand i = new ItineraryRMICommand(rmCars, rmFlights, rmRooms, id, customer, flightNumbers, location, Car, Room);
	// TODO: toSeeds.add(i);
	i.waitFor();
	if (i.error())
	    throw new RemoteException();
	return new ReturnTuple<Boolean>(i.success, timestamp); // TODO: TIMESTAMP LOGIC.
    }


	public ReturnTuple<Object> abort(int id, Timestamp timestamp) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
		// TODO Abort the transaction!
		return new ReturnTuple<Object>(null, timestamp); // TODO: TIMESTAMP LOGIC.
	}


	public ReturnTuple<Boolean> commit(int id, Timestamp timestamp) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
		// TODO Auto-generated method stub
		return new ReturnTuple<Boolean>(false, timestamp); // TODO: TIMESTAMP LOGIC.
	}


	public boolean shutdown(String server) throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}


	public ReturnTuple<Integer> start(Timestamp timestamp) throws RemoteException {
		// TODO: call the transaction manager!
		return new ReturnTuple<Integer>(0, timestamp); // TODO: TIMESTAMP LOGIC.
	}


	public ReturnTuple<Object> unreserveCar(int id, int customer, String location, Timestamp timestamp) throws RemoteException {
		// DO NOTHING.
		return new ReturnTuple<Object>(null, timestamp); // TODO: TIMESTAMP LOGIC.
	}


	public ReturnTuple<Object> unreserveFlight(int id, int customer, int flightNumber, Timestamp timestamp) throws RemoteException {
		// DO NOTHING.
		return new ReturnTuple<Object>(null, timestamp); // TODO: TIMESTAMP LOGIC.
	}


	public ReturnTuple<Object> unreserveRoom(int id, int customer, String locationd, Timestamp timestamp) throws RemoteException {
		// DO NOTHING.
		return new ReturnTuple<Object>(null, timestamp); // TODO: TIMESTAMP LOGIC.
	}


	public ReturnTuple<Object> setCars(int id, String location, int count, int price, Timestamp timestamp) throws RemoteException {
		// DO NOTHING.
		return new ReturnTuple<Object>(null, timestamp); // TODO: TIMESTAMP LOGIC.
	}


	public ReturnTuple<Object> setFlight(int id, int flightNum, int count, int price, Timestamp timestamp) throws RemoteException {
		// DO NOTHING.
		return new ReturnTuple<Object>(null, timestamp); // TODO: TIMESTAMP LOGIC.
	}


	public ReturnTuple<Object> setRooms(int id, String location, int count, int price, Timestamp timestamp) throws RemoteException {
		// DO NOTHING.
		return new ReturnTuple<Object>(null, timestamp); // TODO: TIMESTAMP LOGIC.
	}

}
