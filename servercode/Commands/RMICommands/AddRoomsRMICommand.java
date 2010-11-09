package Commands.RMICommands;

import ResInterface.*;

public class AddRoomsRMICommand extends AbstractRMICommand {

  public int id;
  public String location;
  public int numRooms;
  public int price;

  public boolean success;

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
    success = rm.addRooms(id, location, numRooms, price, null).result;
  }
  
  public void undo() {
	  try {
		  rm.deleteRooms(id, location, null);
	  } catch (Exception e) {
		  e.printStackTrace();
	  }
  }

	@Override
	public RequiredLock getRequiredLock() {
		return RequiredLock.WRITE;
	}
}
