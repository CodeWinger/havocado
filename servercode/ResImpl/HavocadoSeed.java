// -------------------------------
// adapated from Kevin T. Manley
// CSE 593
//
package ResImpl;

import Commands.RMGroupCommands.*;
import ResInterface.*;

import java.util.*;

import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import org.jgroups.ChannelClosedException;
import org.jgroups.ChannelNotConnectedException;
import org.jgroups.Message;


//public class HavocadoSeed extends java.rmi.server.UnicastRemoteObject
public class HavocadoSeed extends GroupMember
	implements ResourceManager {
	
	protected RMHashtable m_itemHT = new RMHashtable();


	public static void main(String args[]) {
        // Figure out where server is running
		boolean isMaster = false;
		String groupName = "";
        String rmiName = "";
        String configFile = "";

        if (args.length == 4) {
        	if (args[0].compareToIgnoreCase("master") == 0)
        		isMaster = true;
        	else if (args[0].compareToIgnoreCase("slave") == 0)
        		isMaster = false;
        	else {
        		System.err.println("Wrong usage");
        		System.out.println("<role> must be \"master\" or \"slave\", not "+args[0]);
        		System.exit(1);
        	}
        	
        	groupName = args[1];
        	rmiName = args[2];
        	configFile = args[3];
        } else {
        	System.err.println ("Wrong usage");
        	System.out.println("Usage: java ResImpl.HavocadoSeed <role> <groupName> <rmiName> <configFile>");
            System.exit(1);
        }
		
		try 
		{
			// create a new Server object
			new HavocadoSeed(isMaster, rmiName, groupName, configFile);
		} 
		catch (Exception e) 
		{
			System.err.println("Server exception: " + e.toString());
			e.printStackTrace();
		}
    }

    
    public HavocadoSeed(boolean isMaster, String myRMIServiceName, String groupName, String configFile) throws RemoteException {
    	super(isMaster, myRMIServiceName, groupName, configFile);
		// dynamically generate the stub (client proxy)
		ResourceManager rm = (ResourceManager) UnicastRemoteObject.exportObject(this, 0);

		// Bind the remote object's stub in the registry
		Registry registry = LocateRegistry.getRegistry();
		registry.rebind(myRMIServiceName, rm);

		System.err.println("Server ready");
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
	
	public void editNum(int id, String key, int qty) {
		ReservableItem curObj = (ReservableItem) readData(id, key);
		if(curObj == null) {
			// object doesn't exist.
		} else {
			curObj.setCount(qty);
		}
	}
	
	public void editPrice(int id, String key, int price) {
		ReservableItem curObj = (ReservableItem) readData(id, key);
		if(curObj == null) {
			// object doesn't exist.
		} else {
			curObj.setPrice(price);
		}
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
		Trace.info("RM::queryPrice(" + id + ", " + key + ") called" );
		ReservableItem curObj = (ReservableItem) readData( id, key);
		int value = 0; 
		if( curObj != null ) {
			value = curObj.getPrice();
		} // else
		Trace.info("RM::queryPrice(" + id + ", " + key + ") returns cost=$" + value );
		return value;		
	}
	
	// reserve an item
	public boolean reserveItem(int id, int customerID, String key, String location){
		Trace.info("RM::reserveItem( " + id + ", customer=" + customerID + ", " +key+ ", "+location+" ) called" );		
		// Read customer object if it exists (and read lock it)
		Customer cust = (Customer) readData( id, Customer.getKey(customerID) );		
		if( cust == null ) {
			Trace.warn("RM::reserveItem( " + id + ", " + customerID + ", " + key + ", "+location+")  failed--customer doesn't exist" );
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
	
	/**
	 * Unreserve an item which has previously been reserved.
	 * @param id
	 * @param customerID
	 * @param key
	 * @param location
	 * @return
	 */
	public void unreserveItem(int id, int customerID, String key, String location) {
		Trace.info("RM::unreserveItem( " + id + ", customer=" + customerID + ", " +key+ ", "+location+" ) called" );
		Customer cust = (Customer) readData(id, Customer.getKey(customerID));
		if(cust == null) {
			// we've got nothing to do.
			Trace.warn("RM::unreserveItem( " + id + ", " + customerID + ", " + key + ", "+location+")  --customer doesn't exist" );
			return;
		}
		
		ReservableItem item = (ReservableItem)readData(id,key);
		if(item == null) {
			// nothing to do again!
			Trace.warn("RM::unreserveItem( " + id + ", " + customerID + ", " + key+", " +location+") --item doesn't exist" );
			return;
		} else if (item.getCount() == 0) {
			// the item can't be un-reserved.
			Trace.warn("RM::unreserveItem( " + id + ", " + customerID + ", " + key+", " + location+") --No more items" );
			return;
		} else {
			cust.unreserve(key, location, item.getPrice());
			writeData(id, cust.getKey(), cust);
			
			// increase the number of available items in the storage.
			item.setCount(item.getCount() + 1);
			item.setReserved(item.getReserved() - 1);
			Trace.info("RM::unreserveItem( " + id + ", " + customerID + ", " + key + ", " +location+") succeeded" );
			return;
		}
	}
	
	// TODO Create interface for RMGroupCommands.
	
	private void sendRMGroupCommand(AbstractRMGroupCommand c) {
		try {
			channel.send(null, null, c);
		} catch (ChannelNotConnectedException e) {
			e.printStackTrace();
		} catch (ChannelClosedException e) {
			e.printStackTrace();
		}
	}
	
	// Create a new flight, or add seats to existing flight
	//  NOTE: if flightPrice <= 0 and the flight already exists, it maintains its current price
	public ReturnTuple<Boolean> addFlight(int id, int flightNum, int flightSeats, int flightPrice, Timestamp timestamp)
		throws RemoteException
	{
		timestamp.stamp();
		if (isMaster) {
			Trace.info("RM::addFlight(" + id + ", " + flightNum + ", $"
					+ flightPrice + ", " + flightSeats + ") called");
			Flight curObj = (Flight) readData(id, Flight.getKey(flightNum));
			if (curObj == null) {
				// doesn't exist...add it
				Flight newObj = new Flight(flightNum, flightSeats, flightPrice);
				writeData(id, newObj.getKey(), newObj);
				sendRMGroupCommand(new WriteDataRMGroupCommand(id, newObj.getKey(), newObj));
				Trace.info("RM::addFlight(" + id + ") created new flight "
						+ flightNum + ", seats=" + flightSeats + ", price=$"
						+ flightPrice);
			} else {
				// add seats to existing flight and update the price...
				curObj.setCount(curObj.getCount() + flightSeats);
				if (flightPrice > 0) {
					curObj.setPrice(flightPrice);
				} // if
				writeData(id, curObj.getKey(), curObj);
				sendRMGroupCommand(new WriteDataRMGroupCommand(id, curObj.getKey(), curObj));
				Trace.info("RM::addFlight(" + id
						+ ") modified existing flight " + flightNum
						+ ", seats=" + curObj.getCount() + ", price=$"
						+ flightPrice);
			} // else
			timestamp.stamp();
			return new ReturnTuple<Boolean>(true, timestamp);
		}
		else {
			ReturnTuple<Boolean> result = null;
			for (MemberInfo mi : getGroupMembers()) {
				try {
					result = GroupMember.memberInfoToResourceManager(mi).addFlight(id, flightNum, flightSeats, flightPrice, timestamp);
					break;
				} catch (RemoteException e) {
					continue;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			result.timestamp.stamp();
			return result;
		}
	}


	
	public ReturnTuple<Boolean> deleteFlight(int id, int flightNum, Timestamp timestamp)
		throws RemoteException
	{
		timestamp.stamp();
		if (isMaster) {
			ReturnTuple<Boolean> result = new ReturnTuple<Boolean>(deleteItem(id, Flight.getKey(flightNum)), timestamp);
			sendRMGroupCommand(new DeleteItemRMGroupCommand(id, Flight.getKey(flightNum)));
			timestamp.stamp();
			return result;
		}
		else {
			ReturnTuple<Boolean> result = null;
			for (MemberInfo mi : getGroupMembers()) {
				try {
					result = GroupMember.memberInfoToResourceManager(mi).deleteFlight(id, flightNum, timestamp);
				} catch (RemoteException e) {
					continue;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			result.timestamp.stamp();
			return result;
		}
	}

	public ReturnTuple<Object> setFlight(int id, int flightNum, int count, int price, Timestamp timestamp) throws RemoteException {
		timestamp.stamp();
		if (isMaster) {
			Flight curObj = (Flight) readData(id, Flight.getKey(flightNum));
			if (curObj == null) {
				// flight doesn't exist. we're done here.
			} else {
				editNum(id, Flight.getKey(flightNum), count);
				editPrice(id, Flight.getKey(flightNum), price);
				sendRMGroupCommand(new EditNumRMGroupCommand(id, Flight.getKey(flightNum), count));
				sendRMGroupCommand(new EditPriceRMGroupCommand(id, Flight.getKey(flightNum), price));
			}
			timestamp.stamp();
			return new ReturnTuple<Object>(null, timestamp);
		}
		else {
			ReturnTuple<Object> result = null;
			for (MemberInfo mi : getGroupMembers()) {
				try {
					result = GroupMember.memberInfoToResourceManager(mi)
							.setFlight(id, flightNum, count, price, timestamp);
				} catch (RemoteException e) {
					continue;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			result.timestamp.stamp();
			return result;
		}
	}



	// Create a new room location or add rooms to an existing location
	//  NOTE: if price <= 0 and the room location already exists, it maintains its current price
	public ReturnTuple<Boolean> addRooms(int id, String location, int count, int price, Timestamp timestamp)
		throws RemoteException
	{
		timestamp.stamp();
		if (isMaster) {
			Trace.info("RM::addRooms(" + id + ", " + location + ", " + count
					+ ", $" + price + ") called");
			Hotel curObj = (Hotel) readData(id, Hotel.getKey(location));
			if (curObj == null) {
				// doesn't exist...add it
				Hotel newObj = new Hotel(location, count, price);
				writeData(id, newObj.getKey(), newObj);
				sendRMGroupCommand(new WriteDataRMGroupCommand(id, newObj.getKey(), newObj));
				Trace.info("RM::addRooms(" + id
						+ ") created new room location " + location
						+ ", count=" + count + ", price=$" + price);
			} else {
				// add count to existing object and update price...
				curObj.setCount(curObj.getCount() + count);
				if (price > 0) {
					curObj.setPrice(price);
				} // if
				writeData(id, curObj.getKey(), curObj);
				sendRMGroupCommand(new WriteDataRMGroupCommand(id, curObj.getKey(), curObj));
				Trace.info("RM::addRooms(" + id
						+ ") modified existing location " + location
						+ ", count=" + curObj.getCount() + ", price=$" + price);
			} // else
			timestamp.stamp();
			return new ReturnTuple<Boolean>(true, timestamp);
		}
		else {
			ReturnTuple<Boolean> result = null;
			for (MemberInfo mi : getGroupMembers()) {
				try {
					result = GroupMember.memberInfoToResourceManager(mi).addRooms(id, location, count, price, timestamp);
				} catch (RemoteException e) {
					continue;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			result.timestamp.stamp();
			return result;
		}
	}

	// Delete rooms from a location
	public ReturnTuple<Boolean> deleteRooms(int id, String location, Timestamp timestamp)
		throws RemoteException
	{
		timestamp.stamp();
		if (isMaster) {
			ReturnTuple<Boolean> result = new ReturnTuple<Boolean>(deleteItem(id, Hotel.getKey(location)), timestamp);
			sendRMGroupCommand(new DeleteItemRMGroupCommand(id, Hotel.getKey(location)));
			timestamp.stamp();
			return result;
		}
		else {
			ReturnTuple<Boolean> result = null;
			for (MemberInfo mi : getGroupMembers()) {
				try {
					result = GroupMember.memberInfoToResourceManager(mi).deleteRooms(id, location, timestamp);
				} catch (RemoteException e) {
					continue;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			result.timestamp.stamp();
			return result;
		}
		
	}

	public ReturnTuple<Object> setRooms(int id, String location, int count, int price, Timestamp timestamp) throws RemoteException {
		timestamp.stamp();
		if (isMaster) {
			Hotel curObj = (Hotel) readData(id, Hotel.getKey(location));
			if (curObj == null) {
				// hotel doesn't exist. we're done here.
			} else {
				editNum(id, Hotel.getKey(location), count);
				editPrice(id, Hotel.getKey(location), price);
				sendRMGroupCommand(new EditNumRMGroupCommand(id, Hotel.getKey(location), count));
				sendRMGroupCommand(new EditPriceRMGroupCommand(id, Hotel.getKey(location), price));
			}
			timestamp.stamp();
			return new ReturnTuple<Object>(null, timestamp);
		}
		else {
			ReturnTuple<Object> result = null;
			for (MemberInfo mi : getGroupMembers()) {
				try {
					result = GroupMember.memberInfoToResourceManager(mi).setRooms(id, location, count, price, timestamp);
				} catch (RemoteException e) {
					continue;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			result.timestamp.stamp();
			return result;
		}
	}
	
	// Create a new car location or add cars to an existing location
	//  NOTE: if price <= 0 and the location already exists, it maintains its current price
	public ReturnTuple<Boolean> addCars(int id, String location, int count, int price, Timestamp timestamp)
		throws RemoteException
	{
		timestamp.stamp();
		if (isMaster) {
			Trace.info("RM::addCars(" + id + ", " + location + ", " + count
					+ ", $" + price + ") called");
			Car curObj = (Car) readData(id, Car.getKey(location));
			if (curObj == null) {
				// car location doesn't exist...add it
				Car newObj = new Car(location, count, price);
				writeData(id, newObj.getKey(), newObj);
				sendRMGroupCommand(new WriteDataRMGroupCommand(id, newObj.getKey(), newObj));
				Trace.info("RM::addCars(" + id + ") created new location "
						+ location + ", count=" + count + ", price=$" + price);
			} else {
				// add count to existing car location and update price...
				curObj.setCount(curObj.getCount() + count);
				if (price > 0) {
					curObj.setPrice(price);
				} // if
				writeData(id, curObj.getKey(), curObj);
				sendRMGroupCommand(new WriteDataRMGroupCommand(id, curObj.getKey(), curObj));
				Trace.info("RM::addCars(" + id
						+ ") modified existing location " + location
						+ ", count=" + curObj.getCount() + ", price=$" + price);
			} // else
			timestamp.stamp();
			return new ReturnTuple<Boolean>(true, timestamp);
		}
		else {
			ReturnTuple<Boolean> result = null;
			for (MemberInfo mi : getGroupMembers()) {
				try {
					result = GroupMember.memberInfoToResourceManager(mi).addCars(id, location, count, price, timestamp);
				} catch (RemoteException e) {
					continue;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			result.timestamp.stamp();
			return result;
		}
	}


	// Delete cars from a location
	public ReturnTuple<Boolean> deleteCars(int id, String location, Timestamp timestamp)
		throws RemoteException
	{
		timestamp.stamp();
		if (isMaster) {
			ReturnTuple<Boolean> result = new ReturnTuple<Boolean>(deleteItem(id, Car.getKey(location)), timestamp);
			sendRMGroupCommand(new DeleteItemRMGroupCommand(id, Car.getKey(location)));
			timestamp.stamp();
			return result;
		}
		else {
			ReturnTuple<Boolean> result = null;
			for (MemberInfo mi : getGroupMembers()) {
				try {
					result = GroupMember.memberInfoToResourceManager(mi).deleteCars(id, location, timestamp);
				} catch (RemoteException e) {
					continue;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			result.timestamp.stamp();
			return result;
		}
	}
	
	public ReturnTuple<Object> setCars(int id, String location, int count, int price, Timestamp timestamp) throws RemoteException {
		timestamp.stamp();
		if (isMaster) {
			Car curObj = (Car) readData(id, Car.getKey(location));
			if (curObj == null) {
				// car doesn't exist. we're done here.
			} else {
				editNum(id, Car.getKey(location), count);
				editPrice(id, Car.getKey(location), price);
				sendRMGroupCommand(new EditNumRMGroupCommand(id, Car.getKey(location), count));
				sendRMGroupCommand(new EditPriceRMGroupCommand(id, Car.getKey(location), price));
			}
			timestamp.stamp();
			return new ReturnTuple<Object>(null, timestamp);
		}
		else {
			ReturnTuple<Object> result = null;
			for (MemberInfo mi : getGroupMembers()) {
				try {
					result = GroupMember.memberInfoToResourceManager(mi).setCars(id, location, count, price, timestamp);
				} catch (RemoteException e) {
					continue;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			result.timestamp.stamp();
			return result;
		}
	}


	// Returns the number of empty seats on this flight
	public ReturnTuple<Integer> queryFlight(int id, int flightNum, Timestamp timestamp)
		throws RemoteException
	{
		timestamp.stamp();
		ReturnTuple<Integer> result = new ReturnTuple<Integer>(queryNum(id, Flight.getKey(flightNum)), timestamp);
		timestamp.stamp();
		return result;
	}

	// Returns price of this flight
	public ReturnTuple<Integer> queryFlightPrice(int id, int flightNum, Timestamp timestamp )
		throws RemoteException
	{
		timestamp.stamp();
		ReturnTuple<Integer> result = new ReturnTuple<Integer>(queryPrice(id, Flight.getKey(flightNum)), timestamp);
		timestamp.stamp();
		return result;
	}


	// Returns the number of rooms available at a location
	public ReturnTuple<Integer> queryRooms(int id, String location, Timestamp timestamp)
		throws RemoteException
	{
		timestamp.stamp();
		ReturnTuple<Integer> result = new ReturnTuple<Integer>(queryNum(id, Hotel.getKey(location)), timestamp);
		timestamp.stamp();
		return result;
	}


	
	
	// Returns room price at this location
	public ReturnTuple<Integer> queryRoomsPrice(int id, String location, Timestamp timestamp)
		throws RemoteException
	{
		timestamp.stamp();
		ReturnTuple<Integer> result = new ReturnTuple<Integer>(queryPrice(id, Hotel.getKey(location)), timestamp);
		return result;
	}


	// Returns the number of cars available at a location
	public ReturnTuple<Integer> queryCars(int id, String location, Timestamp timestamp)
		throws RemoteException
	{
		timestamp.stamp();
		ReturnTuple<Integer> result = new ReturnTuple<Integer>(queryNum(id, Car.getKey(location)), timestamp);
		timestamp.stamp();
		return result;
	}


	// Returns price of cars at this location
	public ReturnTuple<Integer> queryCarsPrice(int id, String location, Timestamp timestamp)
		throws RemoteException
	{
		timestamp.stamp();
		ReturnTuple<Integer> result = new ReturnTuple<Integer>(queryPrice(id, Car.getKey(location)), timestamp);
		timestamp.stamp();
		return result;
	}

	// Returns data structure containing customer reservation info. Returns null if the
	//  customer doesn't exist. Returns empty RMHashtable if customer exists but has no
	//  reservations.
	public RMHashtable getCustomerReservations(int id, int customerID)
		throws RemoteException
	{
		Trace.info("RM::getCustomerReservations(" + id + ", " + customerID + ") called" );
		Customer cust = (Customer) readData( id, Customer.getKey(customerID) );
		if( cust == null ) {
			Trace.warn("RM::getCustomerReservations failed(" + id + ", " + customerID + ") failed--customer doesn't exist" );
			return null;
		} else {
			return cust.getReservations();
		} // if
	}

	// return a bill
	public ReturnTuple<String> queryCustomerInfo(int id, int customerID, Timestamp timestamp)
		throws RemoteException
	{
		timestamp.stamp();
		Trace.info("RM::queryCustomerInfo(" + id + ", " + customerID + ") called" );
		Customer cust = (Customer) readData( id, Customer.getKey(customerID) );
		if( cust == null ) {
			Trace.warn("RM::queryCustomerInfo(" + id + ", " + customerID + ") failed--customer doesn't exist" );
			 timestamp.stamp();
			return new ReturnTuple<String>("", timestamp);   // NOTE: don't change this--WC counts on this value indicating a customer does not exist...
		} else {
				String s = cust.printBill();
				Trace.info("RM::queryCustomerInfo(" + id + ", " + customerID + "), bill follows..." );
				System.out.println( s );
				timestamp.stamp();
				return new ReturnTuple<String>(s, timestamp);
		} // if
	}

  // customer functions
  // new customer just returns a unique customer identifier
	
  public ReturnTuple<Integer> newCustomer(int id, Timestamp timestamp)
		throws RemoteException
	{
	  timestamp.stamp();
		if (isMaster) {
			Trace.info("INFO: RM::newCustomer(" + id + ") called");
			// Generate a globally unique ID for the new customer
			int cid = Integer.parseInt(String.valueOf(id)
					+ String.valueOf(Calendar.getInstance().get(
							Calendar.MILLISECOND))
					+ String.valueOf(Math.round(Math.random() * 100 + 1)));
			Customer cust = new Customer(cid);
			writeData(id, cust.getKey(), cust);
			sendRMGroupCommand(new WriteDataRMGroupCommand(id, cust.getKey(), cust));
			Trace.info("RM::newCustomer(" + cid + ") returns ID=" + cid);
			timestamp.stamp();
			return new ReturnTuple<Integer>(cid, timestamp);
		}
		else {
			ReturnTuple<Integer> result = null;
			for (MemberInfo mi : getGroupMembers()) {
				try {
					result = GroupMember.memberInfoToResourceManager(mi).newCustomer(id, timestamp);
				} catch (RemoteException e) {
					continue;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			result.timestamp.stamp();
			return result;
		}
	}

	// I opted to pass in customerID instead. This makes testing easier
  public ReturnTuple<Boolean> newCustomer(int id, int customerID, Timestamp timestamp )
		throws RemoteException
	{
	  timestamp.stamp();
		if (isMaster) {
			Trace.info("INFO: RM::newCustomer(" + id + ", " + customerID
					+ ") called");
			Customer cust = (Customer) readData(id, Customer.getKey(customerID));
			if (cust == null) {
				cust = new Customer(customerID);
				writeData(id, cust.getKey(), cust);
				sendRMGroupCommand(new WriteDataRMGroupCommand(id, cust.getKey(), cust));
				Trace.info("INFO: RM::newCustomer(" + id + ", " + customerID
						+ ") created a new customer");
				timestamp.stamp();
				return new ReturnTuple<Boolean>(true, timestamp);
			} else {
				Trace.info("INFO: RM::newCustomer(" + id + ", " + customerID
						+ ") failed--customer already exists");
				timestamp.stamp();
				return new ReturnTuple<Boolean>(false, timestamp);
			} // else
		}
		else {
			ReturnTuple<Boolean> result = null;
			for (MemberInfo mi : getGroupMembers()) {
				try {
					result = GroupMember.memberInfoToResourceManager(mi).newCustomer(id, customerID, timestamp);
				} catch (RemoteException e) {
					continue;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			result.timestamp.stamp();
			return result;
		}
	}


	// Deletes customer from the database. 
	public ReturnTuple<Boolean> deleteCustomer(int id, int customerID, Timestamp timestamp)
			throws RemoteException
	{
		timestamp.stamp();
		if (isMaster) {
			Trace.info("RM::deleteCustomer(" + id + ", " + customerID
					+ ") called");
			Customer cust = (Customer) readData(id, Customer.getKey(customerID));
			if (cust == null) {
				Trace.warn("RM::deleteCustomer(" + id + ", " + customerID
						+ ") failed--customer doesn't exist");
				timestamp.stamp();
				return new ReturnTuple<Boolean>(false, timestamp);
			} else {
				// Increase the reserved numbers of all reservable items which the customer reserved. 
				RMHashtable reservationHT = cust.getReservations();
				for (Enumeration e = reservationHT.keys(); e.hasMoreElements();) {
					String reservedkey = (String) (e.nextElement());
					ReservedItem reserveditem = cust.getReservedItem(reservedkey);
					Trace.info("RM::deleteCustomer(" + id + ", " + customerID
							+ ") has reserved " + reserveditem.getKey() + " "
							+ reserveditem.getCount() + " times");
					ReservableItem item = (ReservableItem) readData(id, reserveditem.getKey());
					Trace.info("RM::deleteCustomer(" + id + ", " + customerID
							+ ") has reserved " + reserveditem.getKey()
							+ "which is reserved" + item.getReserved()
							+ " times and is still available "
							+ item.getCount() + " times");
					item.setReserved(item.getReserved() - reserveditem.getCount());
					item.setCount(item.getCount() + reserveditem.getCount());
				}

				// remove the customer from the storage
				removeData(id, cust.getKey());
				sendRMGroupCommand(new RemoveDataRMGroupCommand(id, cust.getKey()));

				Trace.info("RM::deleteCustomer(" + id + ", " + customerID
						+ ") succeeded");
				timestamp.stamp();
				return new ReturnTuple<Boolean>(true, timestamp);
			} // if
		}
		else {
			ReturnTuple<Boolean> result = null;
			for (MemberInfo mi : getGroupMembers()) {
				try {
					result = GroupMember.memberInfoToResourceManager(mi).deleteCustomer(id, customerID, timestamp);
				} catch (RemoteException e) {
					continue;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			result.timestamp.stamp();
			return result;
		}
	}
	
	// Adds car reservation to this customer. 
	public ReturnTuple<Boolean> reserveCar(int id, int customerID, String location, Timestamp timestamp)
		throws RemoteException
	{
		timestamp.stamp();
		if (isMaster) {
			ReturnTuple<Boolean> result = new ReturnTuple<Boolean>(reserveItem(id, customerID, Car.getKey(location), location), timestamp);
			sendRMGroupCommand(new ReserveItemRMGroupCommand(id, customerID, Car.getKey(location), location));
			timestamp.stamp();
			return result;
		}
		else {
			ReturnTuple<Boolean> result = null;
			for (MemberInfo mi : getGroupMembers()) {
				try {
					result = GroupMember.memberInfoToResourceManager(mi).reserveCar(id, customerID, location, timestamp);
				} catch (RemoteException e) {
					continue;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			result.timestamp.stamp();
			return result;
		}
	}


	// Adds room reservation to this customer. 
	public ReturnTuple<Boolean> reserveRoom(int id, int customerID, String location, Timestamp timestamp)
		throws RemoteException
	{
		timestamp.stamp();
		if (isMaster) {
			ReturnTuple<Boolean> result = new ReturnTuple<Boolean>(reserveItem(id, customerID, Hotel.getKey(location), location), timestamp);
			sendRMGroupCommand(new ReserveItemRMGroupCommand(id, customerID, Car.getKey(location), location));
			timestamp.stamp();
			return result;
		}
		else {
			ReturnTuple<Boolean> result = null;
			for (MemberInfo mi : getGroupMembers()) {
				try {
					result = GroupMember.memberInfoToResourceManager(mi).reserveRoom(id, customerID, location, timestamp);
				} catch (RemoteException e) {
					continue;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			result.timestamp.stamp();
			return result;
		}
	}
	
	// Adds flight reservation to this customer.  
	public ReturnTuple<Boolean> reserveFlight(int id, int customerID, int flightNum, Timestamp timestamp)
		throws RemoteException
	{
		timestamp.stamp();
		if (isMaster) {
			ReturnTuple<Boolean> result = new ReturnTuple<Boolean>(reserveItem(id, customerID, Flight.getKey(flightNum), String.valueOf(flightNum)), timestamp);
			sendRMGroupCommand(new ReserveItemRMGroupCommand(id, customerID, Flight.getKey(flightNum), String.valueOf(flightNum)));
			timestamp.stamp();
			return result;
		}
		else {
			ReturnTuple<Boolean> result = null;
			for (MemberInfo mi : getGroupMembers()) {
				try {
					result = GroupMember.memberInfoToResourceManager(mi).reserveFlight(id, customerID, flightNum, timestamp);
				} catch (RemoteException e) {
					continue;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			result.timestamp.stamp();
			return result;
		}
	}
	
	/* reserve an itinerary */
    public ReturnTuple<Boolean> itinerary(int id,int customer,Vector flightNumbers,String location,boolean Car,boolean Room, Timestamp timestamp)
	throws RemoteException {
    	// Do nothing. Should never be called by middleware on resource managers.
    	return null;
    }

	public ReturnTuple<Object> unreserveCar(int id, int customer, String location, Timestamp timestamp) throws RemoteException {
		timestamp.stamp();
		if (isMaster) {
			unreserveItem(id, customer, Car.getKey(location), location);
			sendRMGroupCommand(new UnreserveItemRMGroupCommand(id, customer, Car.getKey(location), location));
			timestamp.stamp();
			return new ReturnTuple<Object>(null, timestamp);
		}
		else {
			ReturnTuple<Object> result = null;
			for (MemberInfo mi : getGroupMembers()) {
				try {
					result = GroupMember.memberInfoToResourceManager(mi).unreserveCar(id, customer, location, timestamp);
				} catch (RemoteException e) {
					continue;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			result.timestamp.stamp();
			return result;
		}
	}


	public ReturnTuple<Object> unreserveFlight(int id, int customer, int flightNumber, Timestamp timestamp) throws RemoteException {
		timestamp.stamp();
		if (isMaster) {
			unreserveItem(id, customer, Flight.getKey(flightNumber), String.valueOf(flightNumber));
			sendRMGroupCommand(new UnreserveItemRMGroupCommand(id, customer, Flight.getKey(flightNumber), String.valueOf(flightNumber)));
			timestamp.stamp();
			return new ReturnTuple<Object>(null, timestamp);
		}
		else {
			ReturnTuple<Object> result = null;
			for (MemberInfo mi : getGroupMembers()) {
				try {
					result = GroupMember.memberInfoToResourceManager(mi).unreserveFlight(id, customer, flightNumber, timestamp);
				} catch (RemoteException e) {
					continue;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			result.timestamp.stamp();
			return result;
		}
	}


	public ReturnTuple<Object> unreserveRoom(int id, int customer, String location, Timestamp timestamp) throws RemoteException {
		timestamp.stamp();
		if (isMaster) {
			unreserveItem(id, customer, Hotel.getKey(location), location);
			sendRMGroupCommand(new UnreserveItemRMGroupCommand(id, customer, Hotel.getKey(location), location));
			timestamp.stamp();
			return new ReturnTuple<Object>(null, timestamp);
		}
		else {
			ReturnTuple<Object> result = null;
			for (MemberInfo mi : getGroupMembers()) {
				try {
					result = GroupMember.memberInfoToResourceManager(mi).unreserveRoom(id, customer, location, timestamp);
				} catch (RemoteException e) {
					continue;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			result.timestamp.stamp();
			return result;
		}
	}
    

	public ReturnTuple<Object> abort(int id, Timestamp timestamp) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
		// do nothing.
		return null;
	}


	public ReturnTuple<Boolean> commit(int id, Timestamp timestamp) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
		// do nothing.
		return null;
	}


	public boolean shutdown(String server) throws RemoteException {
		// shut the resource manager down, and disregard the string.
		if (isMaster) {
			sendRMGroupCommand(new ShutdownRMGroupCommand());
			System.exit(0);
			return true;
		}
		else {
			for (MemberInfo mi : getGroupMembers()) {
				try {
					GroupMember.memberInfoToResourceManager(mi).shutdown(server);
					break;
				} catch (RemoteException e) {
					continue;
				}
			}
			return true;
		}
	}


	public ReturnTuple<Integer> start(Timestamp timestamp) throws RemoteException {
		// do nothing. Should never be called by middleware on resource manager.
		return null;
	}
	
	public ReturnTuple<Vector<String>> customerCarReservations(int id, int customer, Timestamp timestamp)
	throws RemoteException {
		timestamp.stamp();
		Customer cust = (Customer) readData( id, Customer.getKey(customer) );
		RMHashtable reservations = cust.getReservations();
		Object key = null;
		Vector<String> results = new Vector<String>();
		for (Enumeration e = reservations.keys(); e.hasMoreElements(); ) {
			key = e.nextElement();
			ReservedItem item = (ReservedItem) reservations.get( key );
			for (int i = 0; i < item.getCount(); i++) {
				results.add(item.getLocation());
			}
		}
		timestamp.stamp();
		return new ReturnTuple<Vector<String>>(results, timestamp);
	}
	
	public ReturnTuple<Vector<String>> customerRoomReservations(int id, int customer, Timestamp timestamp)
	throws RemoteException {
		timestamp.stamp();
		Customer cust = (Customer) readData( id, Customer.getKey(customer) );
		RMHashtable reservations = cust.getReservations();
		Object key = null;
		Vector<String> results = new Vector<String>();
		for (Enumeration e = reservations.keys(); e.hasMoreElements(); ) {
			key = e.nextElement();
			ReservedItem item = (ReservedItem) reservations.get( key );
			for (int i = 0; i < item.getCount(); i++) {
				results.add(item.getLocation());
			}
		}
		timestamp.stamp();
		return new ReturnTuple<Vector<String>>(results, timestamp);
	}
	
	public ReturnTuple<Vector<Integer>> customerFlightReservations(int id, int customer, Timestamp timestamp)
	throws RemoteException {
		timestamp.stamp();
		Customer cust = (Customer) readData( id, Customer.getKey(customer) );
		RMHashtable reservations = cust.getReservations();
		Object key = null;
		Vector<Integer> results = new Vector<Integer>();
		for (Enumeration e = reservations.keys(); e.hasMoreElements(); ) {
			key = e.nextElement();
			ReservedItem item = (ReservedItem) reservations.get( key );
			for (int i = 0; i < item.getCount(); i++) {
				results.add(Integer.parseInt(item.getLocation()));
			}
		}
		timestamp.stamp();
		return new ReturnTuple<Vector<Integer>>(results, timestamp);
	}


	public void crash() throws RemoteException {
		System.exit(0);
	}


	public LinkedList<MemberInfo> getGroupMembers() throws RemoteException {
		return currentMembers;
	}


	@Override
	protected void specialReceive(Object arg0) {
		if (!isMaster) {
			if (arg0 instanceof AbstractRMGroupCommand) {
				AbstractRMGroupCommand c = (AbstractRMGroupCommand)arg0;
				System.out.println("Special Receive: " + c.toString());
				try {
					c.doCommand(this);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}


	@Override
	public void poke() throws RemoteException {
		// do nothing - just tests if i exist.
	}


	@Override
	public void specialPromoteToMaster() {
		// do nothing.
	}

}
