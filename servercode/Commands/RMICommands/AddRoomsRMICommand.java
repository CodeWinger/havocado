package Commands.RMICommands;

import ResInterface.*;

public class AddRoomsRMICommand extends AbstractRMICommand {

  int id;
  String location;
  int numRooms;
  int price;

  boolean success;

  public AddRoomsRMICommand(ResourceManager pRm, int pId, String pLocation, int pNumRooms, int pPrice) {
    super(pRm);
    // Store our attributes.
    id = pId;
    location = pLocation;
    numRooms = pNumRooms;
    price = pPrice;
    
    success = false;
  }
  
  public void doCommand() throws Exception {
    success = rm.addRooms(id, location, numRooms, price);
  }
}
