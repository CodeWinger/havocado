package Commands.RMICommands;

import ResInterface.*;

public class AddFlightRMICommand extends AbstractRMICommand {

  int id;
  int flightNum;
  int flightSeats;
  int flightPrice;

  boolean success;

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
}
