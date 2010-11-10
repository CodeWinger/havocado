package ResInterface;


import java.rmi.Remote;
import java.rmi.RemoteException;

import java.util.*;
import exceptions.*;
/** 
 * Simplified version from CSE 593 Univ. of Washington
 *
 * Distributed  System in Java.
 * 
 * failure reporting is done using two pieces, exceptions and boolean 
 * return values.  Exceptions are used for systemy things. Return
 * values are used for operations that would affect the consistency
 * 
 * If there is a boolean return value and you're not sure how it 
 * would be used in your implementation, ignore it.  I used boolean
 * return values in the interface generously to allow flexibility in 
 * implementation.  But don't forget to return true when the operation
 * has succeeded.
 */

public interface ResourceManager extends Remote 
{
    /* Add seats to a flight.  In general this will be used to create a new
     * flight, but it should be possible to add seats to an existing flight.
     * Adding to an existing flight should overwrite the current price of the
     * available seats.
     *
     * @return success.
     */
    public ReturnTuple<Boolean> addFlight(int id, int flightNum, int flightSeats, int flightPrice, Timestamp timestamp) 
	throws RemoteException, TransactionAbortedException, InvalidTransactionException; 
    
    /* Add cars to a location.  
     * This should look a lot like addFlight, only keyed on a string location
     * instead of a flight number.
     */
    public ReturnTuple<Boolean> addCars(int id, String location, int numCars, int price, Timestamp timestamp) 
	throws RemoteException, TransactionAbortedException, InvalidTransactionException; 
   
    /* Add rooms to a location.  
     * This should look a lot like addFlight, only keyed on a string location
     * instead of a flight number.
     */
    public ReturnTuple<Boolean> addRooms(int id, String location, int numRooms, int price, Timestamp timestamp) 
	throws RemoteException, TransactionAbortedException, InvalidTransactionException; 			    

			    
    /* new customer just returns a unique customer identifier */
    public ReturnTuple<Integer> newCustomer(int id, Timestamp timestamp) 
	throws RemoteException, TransactionAbortedException, InvalidTransactionException; 
    
    /* new customer with providing id */
    public ReturnTuple<Boolean> newCustomer(int id, int cid, Timestamp timestamp)
    throws RemoteException, TransactionAbortedException, InvalidTransactionException;

    /**
     *   Delete the entire flight.
     *   deleteflight implies whole deletion of the flight.  
     *   all seats, all reservations.  If there is a reservation on the flight, 
     *   then the flight cannot be deleted
     * @param timestamp TODO
     *
     * @return success.
     */   
    public ReturnTuple<Boolean> deleteFlight(int id, int flightNum, Timestamp timestamp) 
	throws RemoteException, TransactionAbortedException, InvalidTransactionException; 
    
    /* Delete all Cars from a location.
     * It may not succeed if there are reservations for this location
     *
     * @return success
     */		    
    public ReturnTuple<Boolean> deleteCars(int id, String location, Timestamp timestamp) 
	throws RemoteException, TransactionAbortedException, InvalidTransactionException; 

    /* Delete all Rooms from a location.
     * It may not succeed if there are reservations for this location.
     *
     * @return success
     */
    public ReturnTuple<Boolean> deleteRooms(int id, String location, Timestamp timestamp) 
	throws RemoteException, TransactionAbortedException, InvalidTransactionException; 
    
    /* deleteCustomer removes the customer and associated reservations */
    public ReturnTuple<Boolean> deleteCustomer(int id,int customer, Timestamp timestamp) 
	throws RemoteException, TransactionAbortedException, InvalidTransactionException; 

    /* queryFlight returns the number of empty seats. */
    public ReturnTuple<Integer> queryFlight(int id, int flightNumber, Timestamp timestamp) 
	throws RemoteException, TransactionAbortedException, InvalidTransactionException; 

    /* return the number of cars available at a location */
    public ReturnTuple<Integer> queryCars(int id, String location, Timestamp timestamp) 
	throws RemoteException, TransactionAbortedException, InvalidTransactionException; 

    /* return the number of rooms available at a location */
    public ReturnTuple<Integer> queryRooms(int id, String location, Timestamp timestamp) 
	throws RemoteException, TransactionAbortedException, InvalidTransactionException; 

    /* return a bill */
    public ReturnTuple<String> queryCustomerInfo(int id,int customer, Timestamp timestamp) 
	throws RemoteException, TransactionAbortedException, InvalidTransactionException; 
    
    /* queryFlightPrice returns the price of a seat on this flight. */
    public ReturnTuple<Integer> queryFlightPrice(int id, int flightNumber, Timestamp timestamp) 
	throws RemoteException, TransactionAbortedException, InvalidTransactionException; 

    /* return the price of a car at a location */
    public ReturnTuple<Integer> queryCarsPrice(int id, String location, Timestamp timestamp) 
	throws RemoteException, TransactionAbortedException, InvalidTransactionException; 

    /* return the price of a room at a location */
    public ReturnTuple<Integer> queryRoomsPrice(int id, String location, Timestamp timestamp) 
	throws RemoteException, TransactionAbortedException, InvalidTransactionException; 

    /* Reserve a seat on this flight*/
    public ReturnTuple<Boolean> reserveFlight(int id, int customer, int flightNumber, Timestamp timestamp) 
	throws RemoteException, TransactionAbortedException, InvalidTransactionException; 

    /* reserve a car at this location */
    public ReturnTuple<Boolean> reserveCar(int id, int customer, String location, Timestamp timestamp) 
	throws RemoteException, TransactionAbortedException, InvalidTransactionException; 

    /* reserve a room certain at this location */
    public ReturnTuple<Boolean> reserveRoom(int id, int customer, String locationd, Timestamp timestamp) 
	throws RemoteException, TransactionAbortedException, InvalidTransactionException; 


    /* reserve an itinerary */
    public ReturnTuple<Boolean> itinerary(int id,int customer,Vector flightNumbers,String location, boolean Car, boolean Room, Timestamp timestamp)
	throws RemoteException, TransactionAbortedException, InvalidTransactionException; 
    
    public ReturnTuple<Vector<String>> customerCarReservations(int id, int customer, Timestamp timestamp) throws RemoteException;
    
    public ReturnTuple<Vector<Integer>> customerFlightReservations(int id, int customer, Timestamp timestamp) throws RemoteException;
    
    public ReturnTuple<Vector<String>> customerRoomReservations(int id, int customer, Timestamp timestamp) throws RemoteException;
    
    public ReturnTuple<Object> unreserveRoom(int id, int customer, String locationd, Timestamp timestamp) throws RemoteException;
    
    public ReturnTuple<Object> unreserveCar(int id, int customer, String location, Timestamp timestamp) throws RemoteException;
    
    public ReturnTuple<Object> unreserveFlight(int id, int customer, int flightNumber, Timestamp timestamp) throws RemoteException;
    
    public ReturnTuple<Object> setCars(int id, String location, int count, int price, Timestamp timestamp) throws RemoteException;
    
    public ReturnTuple<Object> setRooms(int id, String location, int count, int price, Timestamp timestamp) throws RemoteException;
    
    public ReturnTuple<Object> setFlight(int id, int flightNum, int count, int price, Timestamp timestamp) throws RemoteException;
    
    /**
     * Start a transaction.
     * @param timestamp TODO
     * @return an integer representing the new transaction ID.
     * @throws RemoteException
     */
    public ReturnTuple<Integer> start(Timestamp timestamp) throws RemoteException;
    
    /**
     * Commit a transaction
     * @param id the transaction ID
     * @param timestamp TODO
     * @return The success of the commit operation. If false, the transaction was aborted. 
     * @throws RemoteException
     * @throws TransactionAbortedException
     * @throws InvalidTransactionException
     */
    public ReturnTuple<Boolean> commit(int id, Timestamp timestamp) throws RemoteException, TransactionAbortedException, InvalidTransactionException;

    /**
     * Abort a transaction
     * @param id the transaction ID
     * @param timestamp TODO
     * @return TODO
     * @throws RemoteException
     * @throws TransactionAbortedException
     * @throws InvalidTransactionException
     */
    public ReturnTuple<Object> abort(int id, Timestamp timestamp) throws RemoteException, TransactionAbortedException, InvalidTransactionException;
    
    /**
     * Shutdown a specific server.
     * @param server
     * @return
     * @throws RemoteException
     */
    public boolean shutdown(String server) throws RemoteException;
    
}
