package Commands.RMICommands;

import ResInterface.*;

public class QueryCarsRMICommand extends AbstractRMICommand {

  public int id;
  public String location;
  
  public int numCars;

  public QueryCarsRMICommand(ResourceManager pRm, int pId, String pLocation) {
    super(pRm);
    // Store our attributes.
    id = pId;
    location = pLocation;
    
    numCars = -1;
  }
  
  public void doCommand() throws Exception {
    numCars = rm.queryCars(id, location);
  }
  
  public void undo() {
	  // TODO: undo this operation.
  }
}
