package Commands.RMICommands;

import ResInterface.*;

public class QueryCarsRMICommand extends AbstractRMICommand {

  int id;
  String location;
  
  int numCars;

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
}
