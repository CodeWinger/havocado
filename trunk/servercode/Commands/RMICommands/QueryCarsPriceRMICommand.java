package Commands.RMICommands;

import ResInterface.*;

public class QueryCarsPriceRMICommand extends AbstractRMICommand {

  public int id;
  public String location;
  
  public int price;

  public QueryCarsPriceRMICommand(ResourceManager pRm, int pId, String pLocation) {
    super(pRm);
    // Store our attributes.
    id = pId;
    location = pLocation;
    
    price = -1;
  }
  
  public void doCommand() throws Exception {
    price = rm.queryCarsPrice(id, location, null).result; // TODO: TIMESTAMP LOGIC.
  }
  
  public void undo() {
	  // TODO: undo this operation.
  }

	@Override
	public RequiredLock getRequiredLock() {
		return RequiredLock.READ;
	}
}
