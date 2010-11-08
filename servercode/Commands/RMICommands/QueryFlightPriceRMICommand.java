package Commands.RMICommands;

import ResInterface.*;

public class QueryFlightPriceRMICommand extends AbstractRMICommand {

  public int id;
  public int flightNumber;
  
  public int price;

  public QueryFlightPriceRMICommand(ResourceManager pRm, int pId, int pFlightNumber) {
    super(pRm);
    // Store our attributes.
    id = pId;
    flightNumber = pFlightNumber;
    price = -1;
  }
  
  public void doCommand() throws Exception {
    price = rm.queryFlightPrice(id, flightNumber);
  }
  
  public void undo() {
	  // TODO: undo this operation.
  }
}
