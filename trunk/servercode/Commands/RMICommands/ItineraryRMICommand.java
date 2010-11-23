package Commands.RMICommands;

import ResInterface.*;

import java.util.LinkedList;
import java.util.Vector;

public class ItineraryRMICommand extends AbstractRMICommand {

  public LinkedList<MemberInfo> carRmGroup;
  public LinkedList<MemberInfo> flightRmGroup;
  public LinkedList<MemberInfo> roomRmGroup;
  
  public ResourceManager carRm;
  public ResourceManager flightRm;
  public ResourceManager roomRm;

  public int id;
  public int customer;
  public Vector flightNumbers;
  public String location;
  public boolean car;
  public boolean room;
  
  public ReturnTuple<Boolean> success;

  public ItineraryRMICommand(
		LinkedList<MemberInfo> pCarRmGroup, 
		LinkedList<MemberInfo> pFlightRmGroup,
		LinkedList<MemberInfo> pRoomRmGroup,
		int pId, 
		int pCustomer, 
		Vector pFlightNumbers, 
		String pLocation, 
		boolean pCar, 
		boolean pRoom) 
  {
    // Store the resource managers we have to call.
    super(pCarRmGroup); // initialize the abstract constructor - this is only to set the error code to false.
    carRmGroup = pCarRmGroup;
    flightRmGroup = pFlightRmGroup;
    roomRmGroup = pRoomRmGroup;

    // Store our attributes.
    id = pId;
    customer = pCustomer;
    flightNumbers = pFlightNumbers;
    location = pLocation;
    car = pCar;
    room = pRoom;
    
    success = new ReturnTuple<Boolean>(false, null);
  }

  /**
   * Override the populateResourceManagers function.
   */
  @Override
  protected void populateResourceManagers() throws Exception {
	  ResourceManager c = getAvailableRM(carRmGroup);
	  ResourceManager f = getAvailableRM(flightRmGroup);
	  ResourceManager r = getAvailableRM(roomRmGroup);
	  if (c == null || f == null || r == null) {
		  throw new Exception("One resource manager is unavailable");
	  }
	  carRm = c;
	  flightRm = f;
	  roomRm = r;
  }
  
  private Vector<Integer> reservedFlights = new Vector<Integer>();
  private Vector<String> reservedCars = new Vector<String>();
  private Vector<String> reservedRooms = new Vector<String>();
  public void doCommand() throws Exception {
	  timestamp.stamp();
	  
	  success.result = true;
	  ReturnTuple<Boolean> temp;
	
	  for(int i = 0; i < flightNumbers.size(); i++){
		  int flightNum = Integer.parseInt((String) flightNumbers.elementAt(i));
	  	  temp = flightRm.reserveFlight(id, customer, flightNum, timestamp);
	  	  setTimestamp(temp.timestamp);
	  	  if(temp.result) {
			  reservedFlights.add(flightNum);
	  	  } else {
	  		  success.result = false;
	  		  break;
		  }
	  }
	  
	  if(car && success.result) {
		  temp = carRm.reserveCar(id, customer, location, timestamp);
		  setTimestamp(temp.timestamp);
		  if(temp.result) {
			  reservedCars.add(location);
		  } else {
			  success.result = false;
		  }
	  }
	
	  if(room && success.result) {
		  temp = roomRm.reserveRoom(id, customer, location, timestamp);
		  setTimestamp(temp.timestamp);
		  if(temp.result) {
			  reservedRooms.add(location);
		  } else {
			  success.result = false;
		  }
	  }
	  
	  // something awful happened.
	  ReturnTuple<Object> r;
	  if(success.result == false) {
		  // unreserve the flights.
		  for(Integer flightNum : reservedFlights) {
			  r = flightRm.unreserveFlight(id, customer, flightNum, timestamp);
			  setTimestamp(r.timestamp);
		  }
		  // unreserve the cars.
		  for(String location : reservedCars) {
			  r = carRm.unreserveCar(id, customer, location, timestamp);
			  setTimestamp(r.timestamp);
		  }
		  // unreserve the rooms.
		  for(String location: reservedRooms) {
			  r = roomRm.unreserveRoom(id, customer, location, timestamp);
			  setTimestamp(r.timestamp);
		  }
	  }
	  
	  timestamp.stamp();
	  success.timestamp = timestamp;
  }
  
  public void undo() {
	  try {
		  if(success.result) {
			  timestamp.stamp();
			  
			  ReturnTuple<Object> r;
			  // unreserve the flights.
			  for(Integer flightNum : reservedFlights) {
				  r = flightRm.unreserveFlight(id, customer, flightNum, timestamp);
				  setTimestamp(r.timestamp);
			  }
			  // unreserve the cars.
			  for(String location : reservedCars) {
				  r = carRm.unreserveCar(id, customer, location, timestamp);
				  setTimestamp(r.timestamp);
			  }
			  // unreserve the rooms.
			  for(String location: reservedRooms) {
				  r = roomRm.unreserveRoom(id, customer, location, timestamp);
				  setTimestamp(r.timestamp);
			  }
			  
			  timestamp.stamp();
		  }
	  } catch (Exception e) {
		  e.printStackTrace();
	  }
  }

	@Override
	public int getRequiredLock() {
		return LockManager.LockManager.WRITE;
	}
}
