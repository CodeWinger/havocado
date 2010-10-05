package Commands.RMICommands;

import ResInterface.*;

public class QueryRoomsRMICommand extends AbstractRMICommand {

  int id;
  String location;
  
  int numRooms;

  public QueryRoomsRMICommand(ResourceManager pRm, int pId, String pLocation) {
    super(pRm);
    // Store our attributes.
    id = pId;
    location = pLocation;
    
    numRooms = -1;
  }
  
  public void doCommand() throws Exception {
    numRooms = rm.queryRooms(id, location);
  }
}
