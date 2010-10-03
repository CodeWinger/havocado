package Commands.RMICommands;

import ResInterface.*;

public class DeleteRoomsRMICommand extends AbstractRMICommand {

  int id;
  String location;

  public DeleteRoomsRMICommand(ResourceManager pRm, int pId, String pLocation) {
    super(pRm);
    // Store our attributes.
    id = pId;
    location = pLocation;
  }
  
  public void doCommand() throws Exception {
    rm.deleteRooms(id, location);
  }
}
