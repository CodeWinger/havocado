package Commands.RMICommands;

import ResInterface.*;

public class AddFlightRMICommand extends AbstractRMICommand {

  public int id;
  public int flightNum;
  public int flightSeats;
  public int flightPrice;

  public boolean success;

  public AddFlightRMICommand(ResourceManager pRm, int pId, int pFlightNum, int pFlightSeats, int pFlightPrice) {
    super(pRm);
    // Store our attributes.
    id = pId;
    flightNum = pFlightNum;
    flightSeats = pFlightSeats;
    flightPrice = pFlightPrice;
    
    success = false;
  }
  
  public void doCommand() throws Exception {
    // Perform the command.
    success = rm.addFlight(id, flightNum, flightSeats, flightPrice);
  }
  
  public void undo() {
	  // TODO: undo this operation.
  }
}
