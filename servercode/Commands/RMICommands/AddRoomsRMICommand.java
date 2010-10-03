package Commands.RMICommands;

import ResInterface.*;

public class AddRoomsRMICommand extends AbstractRMICommand {

  int id;
  String location;
  int numRooms;
  int price;

  public AddRoomsRMICommand(ResourceManager pRm, int pId, String pLocation, int pNumRooms, int pPrice) {
    super(pRm);
    // Store our attributes.
    id = pId;
    location = pLocation;
    numRooms = pNumRooms;
    price = pPrice;
  }
  
  public void doCommand() throws Exception {
    rm.addRooms(id, location, numRooms, price);
  }
}
