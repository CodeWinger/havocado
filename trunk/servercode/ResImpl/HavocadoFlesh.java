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
    public boolean addFlight(int id, int flightNum, int flightSeats, int flightPrice)
	throws RemoteException
    {
	AddFlightRMICommand af = new AddFlightRMICommand(rmFlights, id, flightNum, flightSeats, flightPrice);
	// TODO: toSeeds.add(af);
	af.waitFor();
	if (af.error())
	    throw new RemoteException();
	return af.success;
    }


	
    public boolean deleteFlight(int id, int flightNum)
	throws RemoteException
    {
	DeleteFlightRMICommand df = new DeleteFlightRMICommand(rmFlights, id, flightNum);
	// TODO: toSeeds.add(df);
	df.waitFor();
	if (df.error())
	    throw new RemoteException();
	return df.success;
    }



    // Create a new room location or add rooms to an existing location
    //  NOTE: if price <= 0 and the room location already exists, it maintains its current price
    public boolean addRooms(int id, String location, int count, int price)
	throws RemoteException
    {
	AddRoomsRMICommand ar = new AddRoomsRMICommand(rmRooms, id, location, count, price);
	// TODO: toSeeds.add(ar);
	ar.waitFor();
	if (ar.error())
	    throw new RemoteException();
	return ar.success;
    }

    // Delete rooms from a location
    public boolean deleteRooms(int id, String location)
	throws RemoteException
    {
	DeleteRoomsRMICommand dr = new DeleteRoomsRMICommand(rmRooms, id, location);
	// TODO: toSeeds.add(dr);
	dr.waitFor();
	if (dr.error())
	    throw new RemoteException();
	return dr.success;
		
    }

    // Create a new car location or add cars to an existing location
    //  NOTE: if price <= 0 and the location already exists, it maintains its current price
    public boolean addCars(int id, String location, int count, int price)
	throws RemoteException
    {
	AddCarsRMICommand ac = new AddCarsRMICommand(rmCars, id, location, count, price);
	// TODO: toSeeds.add(ac);
	ac.waitFor();
	if (ac.error())
	    throw new RemoteException();
	return ac.success;
    }


    // Delete cars from a location
    public boolean deleteCars(int id, String location)
	throws RemoteException
    {
	DeleteCarsRMICommand dc = new DeleteCarsRMICommand(rmCars, id, location);
	// TODO: toSeeds.add(dc);
	dc.waitFor();
	if (dc.error())
	    throw new RemoteException();
	return dc.success;
    }



    // Returns the number of empty seats on this flight
    public int queryFlight(int id, int flightNum)
	throws RemoteException
    {
	QueryFlightRMICommand qf = new QueryFlightRMICommand(rmFlights, id, flightNum);
	// TODO: toSeeds.add(qf);
	qf.waitFor();
	if (qf.error())
	    throw new RemoteException();
	return qf.numSeats;
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
    public int queryFlightPrice(int id, int flightNum )
	throws RemoteException
    {
	QueryFlightPriceRMICommand qfp = new QueryFlightPriceRMICommand(rmFlights, id, flightNum);
	// TODO: toSeeds.add(qfp);
	qfp.waitFor();
	if (qfp.error())
	    throw new RemoteException();
	return qfp.price;
    }


    // Returns the number of rooms available at a location
    public int queryRooms(int id, String location)
	throws RemoteException
    {
	QueryRoomsRMICommand qr = new QueryRoomsRMICommand(rmRooms, id, location);
	// TODO: toSeeds.add(qr);
	qr.waitFor();
	if (qr.error())
	    throw new RemoteException();
	return qr.numRooms;
    }


	
	
    // Returns room price at this location
    public int queryRoomsPrice(int id, String location)
	throws RemoteException
    {
	QueryRoomsPriceRMICommand qrp = new QueryRoomsPriceRMICommand(rmRooms, id, location);
	// TODO: toSeeds.add(qrp);
	qrp.waitFor();
	if (qrp.error())
	    throw new RemoteException();
	return qrp.price;
    }


    // Returns the number of cars available at a location
    public int queryCars(int id, String location)
	throws RemoteException
    {
	QueryCarsRMICommand qc = new QueryCarsRMICommand(rmCars, id, location);
	// TODO: toSeeds.add(qc);
	qc.waitFor();
	if (qc.error())
	    throw new RemoteException();
	return qc.numCars;
    }


    // Returns price of cars at this location
    public int queryCarsPrice(int id, String location)
	throws RemoteException
    {
	QueryCarsPriceRMICommand qcp = new QueryCarsPriceRMICommand(rmRooms, id, location);
	// TODO: toSeeds.add(qcp);
	qcp.waitFor();
	if (qcp.error())
	    throw new RemoteException();
	return qcp.price;
    }

    // return a bill
    public String queryCustomerInfo(int id, int customerID)
	throws RemoteException
    {
	QueryCustomerInfoRMICommand qci = new QueryCustomerInfoRMICommand(rmCars, rmFlights, rmRooms, id, customerID);
	// TODO: toSeeds.add(qci);
	qci.waitFor();
	if (qci.error())
	    throw new RemoteException();
	return qci.customerInfo;
    }

    // customer functions
    // new customer just returns a unique customer identifier
	
    public int newCustomer(int id)
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
	return rid;
    }

    // I opted to pass in customerID instead. This makes testing easier
    public boolean newCustomer(int id, int customerID )
	throws RemoteException
    {
	NewCustomerWithIdRMICommand ncwi = new NewCustomerWithIdRMICommand(rmCars, rmFlights, rmRooms, id, customerID);
	// TODO: toSeeds.add(ncwi);
	ncwi.waitFor();
	if (ncwi.error())
	    throw new RemoteException();
	return ncwi.success;
    }


    // Deletes customer from the database. 
    public boolean deleteCustomer(int id, int customerID)
	throws RemoteException
    {
	DeleteCustomerRMICommand dc = new DeleteCustomerRMICommand(rmCars, rmFlights, rmRooms, id, customerID);
	// TODO: toSeeds.add(dc);
	dc.waitFor();
	if(dc.error())
	    throw new RemoteException();
	return dc.success;
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
    public boolean reserveCar(int id, int customerID, String location)
	throws RemoteException
    {
	ReserveCarRMICommand rc = new ReserveCarRMICommand(rmCars, id, customerID, location);
	// TODO: toSeeds.add(rc);
	rc.waitFor();
	if (rc.error())
	    throw new RemoteException();
	return rc.success;
    }


    // Adds room reservation to this customer. 
    public boolean reserveRoom(int id, int customerID, String location)
	throws RemoteException
    {
	ReserveRoomRMICommand rr = new ReserveRoomRMICommand(rmRooms, id, customerID, location);
	// TODO: toSeeds.add(rr);
	rr.waitFor();
	if (rr.error())
	    throw new RemoteException();
	return rr.success;
    }
    // Adds flight reservation to this customer.  
    public boolean reserveFlight(int id, int customerID, int flightNum)
	throws RemoteException
    {
	ReserveFlightRMICommand rf = new ReserveFlightRMICommand(rmFlights, id, customerID, flightNum);
	// TODO: toSeeds.add(rf);
	rf.waitFor();
	if (rf.error())
	    throw new RemoteException();
	return rf.success;
    }
	
    /* reserve an itinerary */
    public boolean itinerary(int id,int customer,Vector flightNumbers,String location,boolean Car,boolean Room)
	throws RemoteException {
	ItineraryRMICommand i = new ItineraryRMICommand(rmCars, rmFlights, rmRooms, id, customer, flightNumbers, location, Car, Room);
	// TODO: toSeeds.add(i);
	i.waitFor();
	if (i.error())
	    throw new RemoteException();
	return i.success;
    }


	public void abort(int id) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
		// TODO Auto-generated method stub
		
	}


	public boolean commit(int id) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
		// TODO Auto-generated method stub
		return false;
	}


	public boolean shutdown(String server) throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}


	public int start() throws RemoteException {
		// TODO Auto-generated method stub
		return 0;
	}


	public void unreserveCar(int id, int customer, String location) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
		// DO NOTHING.
	}


	public void unreserveFlight(int id, int customer, int flightNumber) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
		// DO NOTHING.
	}


	public void unreserveRoom(int id, int customer, String locationd) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
		// DO NOTHING.
	}

}
