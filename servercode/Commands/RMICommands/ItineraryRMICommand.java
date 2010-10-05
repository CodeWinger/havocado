package Commands.RMICommands;

import ResInterface.*;

public class ItineraryRMICommand extends AbstractRMICommand {

  public ResourceManager carRm;
  public ResourceManager flightRm;
	public ResourceManager roomRm;

  public int id;
  public int customer;
  public Vector flightNumbers;
  public String location;
  public boolean car;
  public boolean room;
  
  public boolean success;

  public ItineraryRMICommand(
		ResourceManager pCarRm, 
		ResourceManager pFlightRm, 
		ResourceManager pRoomRm, 
		int pId, 
		int pCustomer, 
		Vector pFlightNumbers, 
		String pLocation, 
		boolean pCar, 
		boolean pRoom) 
  {
    // Store the resource managers we have to call.
    super(pCarRm); // initialize the abstract constructor - this is only to set the error code to false.
    carRm = pCarRm;
    flightRm = pFlightRm;
    roomRm = pRoomRm;

    // Store our attributes.
    id = pId;
    customer = pCustomer;
    flightNumbers = pFlightNumbers;
    location = pLocation;
    car = pCar;
    room = pRoom;
    
    success = false;
  }

  public void doCommand() throws Exception {
    // do the flights.
    success = true;
    for(int i = 0; i < flightNumbers.size(); i++){
      int flightNum = ((Integer) flightNumbers.elementAt(i)).intValue();
      success = success && flightRm.reserveFlight(id, customer, flightNum);
    }
    if(car && success) {
      success = success && carRm.reserveCar(id, customer, location);
    }
    if(room && success) {
      success = success && roomRm.reserveRoom(id, customer, location);
    }
  }
}
