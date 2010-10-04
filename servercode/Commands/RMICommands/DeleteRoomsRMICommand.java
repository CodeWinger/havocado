package Commands.RMICommands;

import ResInterface.*;

public class DeleteRoomsRMICommand extends AbstractRMICommand {

  int id;
  String location;
  
  boolean success;

  public DeleteRoomsRMICommand(ResourceManager pRm, int pId, String pLocation) {
    super(pRm);
    // Store our attributes.
    id = pId;
    location = pLocation;
    
    success = false;
  }
  
  public void doCommand() throws Exception {
    success = rm.deleteRooms(id, location);
  }
}
