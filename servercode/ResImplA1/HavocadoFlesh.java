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

//public class HavocadoFlesh extends java.rmi.server.UnicastRemoteObject
public class HavocadoFlesh
    implements ResourceManager {
	
    static ConcurrentLinkedQueue<Command> toSeeds = new ConcurrentLinkedQueue<Command>();
    static ResourceManager rmCars, rmFlights, rmRooms;
    static int carPort = 11111, flightPort = 22222, roomPort = 33333, port = 1111;
    static Socket rmCarSocket, rmFlightSocket, rmRoomSocket;

    protected RMHashtable m_itemHT = new RMHashtable();

    public static void main(String args[]) {
        // Figure out where server is running
        String server = "localhost";
	String carSeed, flightSeed, roomSeed;
	carSeed = flightSeed = roomSeed = "localhost";
	// TODO: Set these strings to cl arguments.

	if (args.length == 4) {
	    port = Integer.parseInt(args[0]);
	    carSeed = args[1];
	    flightSeed = args[2];
	    roomSeed = args[3];
	}
	else if (args.length == 2) {
	    //	    server = server + ":" + args[0];
	    carSeed = flightSeed = roomSeed = args[1];
	    try {
		port = Integer.parseInt(args[0]);
	    }
	    catch (Exception e) {
		e.printStackTrace();
	    }
	} else if (args.length != 0 &&  args.length != 1) {
	    System.err.println ("Wrong usage");
	    System.out.println("Usage: java ResImpl.HavocadoFlesh [port]");
	    System.exit(1);
	}
	
	// Set up RMI.	 
	HavocadoFlesh obj = null;
	try {
	    System.out.println("HELLO "+carSeed);
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

	// Set up TCP sockets.
	try {
	    rmCarSocket = new Socket(carSeed, carPort);
	    rmFlightSocket = new Socket(flightSeed, flightPort);
	    rmRoomSocket = new Socket(roomSeed, roomPort);
	    
	    System.err.println("Server ready");
	}
	catch (Exception e) {
	    System.err.println("Server exception: " + e.toString());
	    e.printStackTrace();
	}

	ToSeedsThread tst = new ToSeedsThread(toSeeds, obj);
	tst.start();

	try {
	    ServerSocket ss = new ServerSocket(port);
	    ObjectInputStream carIn = new ObjectInputStream(rmCarSocket.getInputStream());
	    ObjectOutputStream carOut = new ObjectOutputStream(rmCarSocket.getOutputStream());
	    ObjectInputStream flightIn = new ObjectInputStream(rmFlightSocket.getInputStream());
	    ObjectOutputStream flightOut = new ObjectOutputStream(rmFlightSocket.getOutputStream());
	    ObjectInputStream roomIn = new ObjectInputStream(rmRoomSocket.getInputStream());
	    ObjectOutputStream roomOut = new ObjectOutputStream(rmRoomSocket.getOutputStream());
	    while (true) {
		new FleshTCPThread(toSeeds, ss.accept(), carIn, carOut, flightIn, flightOut, roomIn, roomOut);
	    }
	}
	catch (Exception e) {
	    e.printStackTrace();
	}

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


    // Reads a data item
    public RMItem readData( int id, String key )
    {
	synchronized(m_itemHT){
	    return (RMItem) m_itemHT.get(key);
	}
    }

    // Writes a data item
    public void writeData( int id, String key, RMItem value )
    {
	synchronized(m_itemHT){
	    m_itemHT.put(key, value);
	}
    }
	
    // Remove the item out of storage
    public RMItem removeData(int id, String key){
	synchronized(m_itemHT){
	    return (RMItem)m_itemHT.remove(key);
	}
    }
	
	
    // deletes the entire item
    public boolean deleteItem(int id, String key)
    {
	Trace.info("RM::deleteItem(" + id + ", " + key + ") called" );
	ReservableItem curObj = (ReservableItem) readData( id, key );
	// Check if there is such an item in the storage
	if( curObj == null ) {
	    Trace.warn("RM::deleteItem(" + id + ", " + key + ") failed--item doesn't exist" );
	    return false;
	} else {
	    if(curObj.getReserved()==0){
		removeData(id, curObj.getKey());
		Trace.info("RM::deleteItem(" + id + ", " + key + ") item deleted" );
		return true;
	    }
	    else{
		Trace.info("RM::deleteItem(" + id + ", " + key + ") item can't be deleted because some customers reserved it" );
		return false;
	    }
	} // if
    }
	

    // query the number of available seats/rooms/cars
    public int queryNum(int id, String key) {
	Trace.info("RM::queryNum(" + id + ", " + key + ") called" );
	ReservableItem curObj = (ReservableItem) readData( id, key);
	int value = 0;  
	if( curObj != null ) {
	    value = curObj.getCount();
	} // else
	Trace.info("RM::queryNum(" + id + ", " + key + ") returns count=" + value);
	return value;
    }	
	
    // query the price of an item
    public int queryPrice(int id, String key){
	Trace.info("RM::queryCarsPrice(" + id + ", " + key + ") called" );
	ReservableItem curObj = (ReservableItem) readData( id, key);
	int value = 0; 
	if( curObj != null ) {
	    value = curObj.getPrice();
	} // else
	Trace.info("RM::queryCarsPrice(" + id + ", " + key + ") returns cost=$" + value );
	return value;		
    }
	
    // reserve an item
    public boolean reserveItem(int id, int customerID, String key, String location){
	Trace.info("RM::reserveItem( " + id + ", customer=" + customerID + ", " +key+ ", "+location+" ) called" );		
	// Read customer object if it exists (and read lock it)
	Customer cust = (Customer) readData( id, Customer.getKey(customerID) );		
	if( cust == null ) {
	    Trace.warn("RM::reserveCar( " + id + ", " + customerID + ", " + key + ", "+location+")  failed--customer doesn't exist" );
	    return false;
	} 
		
	// check if the item is available
	ReservableItem item = (ReservableItem)readData(id, key);
	if(item==null){
	    Trace.warn("RM::reserveItem( " + id + ", " + customerID + ", " + key+", " +location+") failed--item doesn't exist" );
	    return false;
	}else if(item.getCount()==0){
	    Trace.warn("RM::reserveItem( " + id + ", " + customerID + ", " + key+", " + location+") failed--No more items" );
	    return false;
	}else{			
	    cust.reserve( key, location, item.getPrice());		
	    writeData( id, cust.getKey(), cust );
			
	    // decrease the number of available items in the storage
	    item.setCount(item.getCount() - 1);
	    item.setReserved(item.getReserved()+1);
			
	    Trace.info("RM::reserveItem( " + id + ", " + customerID + ", " + key + ", " +location+") succeeded" );
	    return true;
	}		
    }


    // Create a new flight, or add seats to existing flight
    //  NOTE: if flightPrice <= 0 and the flight already exists, it maintains its current price
    public boolean addFlight(int id, int flightNum, int flightSeats, int flightPrice)
	throws RemoteException
    {
	AddFlightRMICommand af = new AddFlightRMICommand(rmFlights, id, flightNum, flightSeats, flightPrice);
	toSeeds.add(af);
	af.waitFor();
	if (af.error())
	    throw new RemoteException();
	return af.success;
    }


	
    public boolean deleteFlight(int id, int flightNum)
	throws RemoteException
    {
	DeleteFlightRMICommand df = new DeleteFlightRMICommand(rmFlights, id, flightNum);
	toSeeds.add(df);
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
	toSeeds.add(ar);
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
	toSeeds.add(dr);
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
	toSeeds.add(ac);
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
	toSeeds.add(dc);
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
	toSeeds.add(qf);
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
	toSeeds.add(qfp);
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
	toSeeds.add(qr);
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
	toSeeds.add(qrp);
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
	toSeeds.add(qc);
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
	toSeeds.add(qcp);
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
	toSeeds.add(qci);
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
	toSeeds.add(nc);
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
	toSeeds.add(ncwi);
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
	toSeeds.add(dc);
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
	toSeeds.add(rc);
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
	toSeeds.add(rr);
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
	toSeeds.add(rf);
	rf.waitFor();
	if (rf.error())
	    throw new RemoteException();
	return rf.success;
    }
	
    /* reserve an itinerary */
    public boolean itinerary(int id,int customer,Vector flightNumbers,String location,boolean Car,boolean Room)
	throws RemoteException {
	ItineraryRMICommand i = new ItineraryRMICommand(rmCars, rmFlights, rmRooms, id, customer, flightNumbers, location, Car, Room);
	toSeeds.add(i);
	i.waitFor();
	if (i.error())
	    throw new RemoteException();
	return i.success;
    }

}
