package Commands.RMICommands;

import ResInterface.*;

public class QueryRoomsRMICommand extends AbstractRMICommand {

  public int id;
  public String location;
  
  public int numRooms;

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
  
  public void undo() {
	  // TODO: undo this operation.
  }

	@Override
	public RequiredLock getRequiredLock() {
		return RequiredLock.READ;
	}
}
