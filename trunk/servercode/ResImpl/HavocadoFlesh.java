// -------------------------------
// adapated from Kevin T. Manley
// CSE 593
//
package ResImpl;

import ResInterface.*;

import java.util.*;
import java.rmi.*;

import Commands.*;
import Commands.RMICommands.*;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

//public class HavocadoFlesh extends java.rmi.server.UnicastRemoteObject
public class HavocadoFlesh
    implements ResourceManager {
	
    ConcurrentLinkedQueue<Command> toSeeds = new ConcurrentLinkedQueue<CommandInterface>();
    ResourceManager rmCars, rmFlights, rmRooms;


    public static void main(String args[]) {
        // Figure out where server is running
        String server = "localhost";
	String carSeed, flightSeed, roomSeed;
	// TODO: Set these strings to cl arguments.

	if (args.length == 1) {
	    server = server + ":" + args[0];
	} else if (args.length != 0 &&  args.length != 1) {
	    System.err.println ("Wrong usage");
	    System.out.println("Usage: java ResImpl.HavocadoFlesh [port]");
	    System.exit(1);
	}
		 
	try 
	    {
		// create a new Server object
		HavocadoFlesh obj = new HavocadoFlesh();
		// dynamically generate the stub (client proxy)
		ResourceManager rm = (ResourceManager) UnicastRemoteObject.exportObject(obj, 0);

		// Bind the remote object's stub in the registry
		Registry registry = LocateRegistry.getRegistry();
		registry.rebind("HavocadoFlesh", rm);

		registry = LocateRegistry.getRegistry(carSeed);
		rm = (ResourceManager) registry.lookup("HavocadoSeedCar");
		// TODO: Check for null rm.
		registry = LocateRegistry.getRegistry(flightSeed);
		rm = (ResourceManager) registry.lookup("HavocadoSeedFlight");
		// TODO: Check for null rm.
		registry = LocateRegistry.getRegistry(roomSeed);
		rm = (ResourceManager) registry.lookup("HavocadoSeedRoom");
		// TODO: Check for null rm.

		System.err.println("Server ready");
	    } 
	catch (Exception e) 
	    {
		System.err.println("Server exception: " + e.toString());
		e.printStackTrace();
	    }
	
	ToSeedsThread tst = new ToSeedsThread(); 
	tst.start();

	// Create and install a security manager
	//        if (System.getSecurityManager() == null) {
	//          System.setSecurityManager(new RMISecurityManager());
	//        }
	//        try {
	//               HavocadoFlesh obj = new HavocadoFlesh();
	//               Naming.rebind("rmi://" + server + "/RM", obj);
	//               System.out.println("RM bound");
	//        } 
	//        catch (Exception e) {
	//               System.out.println("RM not bound:" + e);
	//        }
    }

    
    public HavocadoFlesh() throws RemoteException {
    }


    // Create a new flight, or add seats to existing flight
    //  NOTE: if flightPrice <= 0 and the flight already exists, it maintains its current price
    public boolean addFlight(int id, int flightNum, int flightSeats, int flightPrice)
	throws RemoteException
    {
	AddFlightRMICommand af = new AddFlightRMICommnd(rmFlights, id, flightNum, flightSeats, flightPrice);
	toSeeds.add(af);
	af.waitFor();
	if (af.error())
	    throw new RemoteException();
	// TODO: Get actual value.
	return(true);
    }


	
    public boolean deleteFlight(int id, int flightNum)
	throws RemoteException
    {
	DeleteFlightRMICommand df = new DeleteFlightRMICommand(rmFlights, id, flightNum);
	toSeeds.add(df);
	df.waitFor();
	if (df.error())
	    throw new RemoteException();
	// TODO: Get actual value.
	return deleteItem(id, Flight.getKey(flightNum));
    }



    // Create a new room location or add rooms to an existing location
    //  NOTE: if price <= 0 and the room location already exists, it maintains its current price
    public boolean addRooms(int id, String location, int count, int price)
	throws RemoteException
    {
	AddRoomsRMICommand ar = new AddRoomsRMICommand(rmRooms, id, location, count, price);
	toSeeds.add(ar);
	ar.waitFor();
	if (ar.error())
	    throw new RemoteException();
	// TODO: Get actual value.
	return(true);
    }

    // Delete rooms from a location
    public boolean deleteRooms(int id, String location)
	throws RemoteException
    {
	DeleteRoomsRMICommand dr = new DeleteRoomsRMICommand(rmRooms, id, location);
	toSeeds.add(dr);
	dr.waitFor();
	if (dr.error())
	    throw new RemoteException();
	// TODO: Get actual value.
	return deleteItem(id, Hotel.getKey(location));
	return true;
		
    }

    // Create a new car location or add cars to an existing location
    //  NOTE: if price <= 0 and the location already exists, it maintains its current price
    public boolean addCars(int id, String location, int count, int price)
	throws RemoteException
    {
	/*	AddCarsRMICommand ac = new AddCarsRMICommand(rmCars, id, location, count, price);
	toSeeds.add(ac);
	ac.waitFor();
	if (ac.error())
	    throw new RemoteException();
	// TODO: Get actual value.*/
	return(true);
    }


    // Delete cars from a location
    public boolean deleteCars(int id, String location)
	throws RemoteException
    {
	/*	DeleteCarsRMICommand dc = new DeleteCarsRMICommand(rmCars, id, location);
	toSeeds.add(dc);
	dc.waitFor();
	if (dc.error())
	    throw new RemoteException();
	// TODO: Get actual value.
	return deleteItem(id, Car.getKey(location));*/
	return true;
    }



    // Returns the number of empty seats on this flight
    public int queryFlight(int id, int flightNum)
	throws RemoteException
    {
	/*	QueryFlightRMICommand qf = new QueryFlightRMICommand(rmFlights, id, flightNum);
	toSeeds.add(qf);
	qf.waitFor();
	if (qf.error())
	    throw new RemoteException();
	// TODO: Get actual value.
	return queryNum(id, Flight.getKey(flightNum));*/
	return 0;
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
	/*	QueryFlightPriceRMICommand qfp = new QueryFlightPriceRMICommand(rmFights, id, flightNum);
	toSeeds.add(qfp);
	qfp.waitFor();
	if (qfp.error())
	    throw new RemoteException();
	// TODO: Get actual value.
	return queryPrice(id, Flight.getKey(flightNum));*/
	return 0;
    }


    // Returns the number of rooms available at a location
    public int queryRooms(int id, String location)
	throws RemoteException
    {
	/*	QueryRoomsRMICommand qr = new QueryRoomsRMICommand(rmRooms, id, location);
	toSeeds.add(qr);
	qr.waitFor();
	if (qr.error())
	    throw new RemoteException();
	// TODO: Get actual value.
	return queryNum(id, Hotel.getKey(location));/*
	return 0;
    }


	
	
    // Returns room price at this location
    public int queryRoomsPrice(int id, String location)
	throws RemoteException
    {
	/*	QueryRoomsPriceRMICommand qrp = new QueryRoomsPriceRMICommand(rmRooms, id, location);
	toSeeds.add(qrp);
	qrp.waitFor();
	if (qrp.error())
	    throw new RemoteException();
	// TODO: Get actual value.
	return queryPrice(id, Hotel.getKey(location));*/
	return 0;
    }


    // Returns the number of cars available at a location
    public int queryCars(int id, String location)
	throws RemoteException
    {
	/*	QueryCarsRMICommand qc = new QueryCarsRMICommand(rmCars, id, location);
	toSeeds.add(qc);
	qc.waitFor();
	if (qc.error())
	    throw new RemoteException();
	// TODO: Get actual value.
	return queryNum(id, Car.getKey(location));*/
	return 0;
    }


    // Returns price of cars at this location
    public int queryCarsPrice(int id, String location)
	throws RemoteException
    {
	/*	QueryCarsPriceRMICommand qcp = new QueryCarsPriceRMICommand(rmRooms, id, location);
	toSeeds.add(qcp);
	qcp.waitFor();
	if (qcp.error())
	    throw new RemoteException();
	// TODO: Get actual value.
	return queryPrice(id, Car.getKey(location));*/
	return 0;
    }

    // return a bill
    public String queryCustomerInfo(int id, int customerID)
	throws RemoteException
    {
	/*	Trace.info("RM::queryCustomerInfo(" + id + ", " + customerID + ") called" );
	Customer cust = (Customer) readData( id, Customer.getKey(customerID) );
	if( cust == null ) {
	    Trace.warn("RM::queryCustomerInfo(" + id + ", " + customerID + ") failed--customer doesn't exist" );
	    return "";   // NOTE: don't change this--WC counts on this value indicating a customer does not exist...
	} else {
	    String s = cust.printBill();
	    Trace.info("RM::queryCustomerInfo(" + id + ", " + customerID + "), bill follows..." );
	    System.out.println( s );
	    return s;
	} // if*/
	return "";
    }

    // customer functions
    // new customer just returns a unique customer identifier
	
    public int newCustomer(int id)
	throws RemoteException
    {
	/*	Trace.info("INFO: RM::newCustomer(" + id + ") called" );
	// Generate a globally unique ID for the new customer
	int cid = Integer.parseInt( String.valueOf(id) +
				    String.valueOf(Calendar.getInstance().get(Calendar.MILLISECOND)) +
				    String.valueOf( Math.round( Math.random() * 100 + 1 )));
	Customer cust = new Customer( cid );
	writeData( id, cust.getKey(), cust );
	Trace.info("RM::newCustomer(" + cid + ") returns ID=" + cid );
	return cid;*/
	return 0;
    }

    // I opted to pass in customerID instead. This makes testing easier
    public boolean newCustomer(int id, int customerID )
	throws RemoteException
    {
	/*	Trace.info("INFO: RM::newCustomer(" + id + ", " + customerID + ") called" );
	Customer cust = (Customer) readData( id, Customer.getKey(customerID) );
	if( cust == null ) {
	    cust = new Customer(customerID);
	    writeData( id, cust.getKey(), cust );
	    Trace.info("INFO: RM::newCustomer(" + id + ", " + customerID + ") created a new customer" );
	    return true;
	} else {
	    Trace.info("INFO: RM::newCustomer(" + id + ", " + customerID + ") failed--customer already exists");
	    return false;
	} // else*/
	return true;
    }


    // Deletes customer from the database. 
    public boolean deleteCustomer(int id, int customerID)
	throws RemoteException
    {
	/*	Trace.info("RM::deleteCustomer(" + id + ", " + customerID + ") called" );
	Customer cust = (Customer) readData( id, Customer.getKey(customerID) );
	if( cust == null ) {
	    Trace.warn("RM::deleteCustomer(" + id + ", " + customerID + ") failed--customer doesn't exist" );
	    return false;
	} else {			
	    // Increase the reserved numbers of all reservable items which the customer reserved. 
	    RMHashtable reservationHT = cust.getReservations();
	    for(Enumeration e = reservationHT.keys(); e.hasMoreElements();){		
		String reservedkey = (String) (e.nextElement());
		ReservedItem reserveditem = cust.getReservedItem(reservedkey);
		Trace.info("RM::deleteCustomer(" + id + ", " + customerID + ") has reserved " + reserveditem.getKey() + " " +  reserveditem.getCount() +  " times"  );
		ReservableItem item  = (ReservableItem) readData(id, reserveditem.getKey());
		Trace.info("RM::deleteCustomer(" + id + ", " + customerID + ") has reserved " + reserveditem.getKey() + "which is reserved" +  item.getReserved() +  " times and is still available " + item.getCount() + " times"  );
		item.setReserved(item.getReserved()-reserveditem.getCount());
		item.setCount(item.getCount()+reserveditem.getCount());
	    }
			
	    // remove the customer from the storage
	    removeData(id, cust.getKey());
			
	    Trace.info("RM::deleteCustomer(" + id + ", " + customerID + ") succeeded" );*/
	    return true;
	} // if
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
    //	throws RemoteException
    {
	/*	ReserveCarRMICommand rc = new ReserveCarRMICommand(rmCars, id, customerID, location);
	toSeeds.add(rc);
	rc.waitFor();
	if (rc.error())
	    throw new RemoteException();
	// TODO: Get actual value.*/
	//	return reserveItem(id, customerID, Car.getKey(location), location);
	return true;
    }


    // Adds room reservation to this customer. 
    public boolean reserveRoom(int id, int customerID, String location)
	throws RemoteException
    {
	/*	ReserveRoomRMICommand rr = new ReserveRoomRMICommand(rmRooms, id, customerID, location);
	toSeeds.add(rr);
	rr.waitFor();
	if (rr.error())
	    throw new RemoteException();
	// TODO: Get actual value.*/
	//	return reserveItem(id, customerID, Hotel.getKey(location), location);
	return true;
    }
    // Adds flight reservation to this customer.  
    public boolean reserveFlight(int id, int customerID, int flightNum)
	throws RemoteException
    {
	/*	ReseverFlightRMICommand rf = new ReseverFlightRMICommand(rmFlights, id, customerID, flightNum);
	toSeeds.ad(rf);
	rf.waitFor();
	if (rf.error())
	    throw new RemoteException();
	    // TODO: Get actual value.*/
	return true;//	return reserveItem(id, customerID, Flight.getKey(flightNum), String.valueOf(flightNum));
    }
	
    /* reserve an itinerary */
    public boolean itinerary(int id,int customer,Vector flightNumbers,String location,boolean Car,boolean Room)
	throws RemoteException {
	// TODO: This.
    	return false;
    }

}
