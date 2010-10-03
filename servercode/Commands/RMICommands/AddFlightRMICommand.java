package Commands.RMICommands;

import ResInterface.*;

public class AddFlightRMICommand extends AbstractRMICommand {

  int id;
  int flightNum;
  int flightSeats;
  int flightPrice;

  public AddFlightRMICommand(ResourceManager pRm, int pId, int pFlightNum, int pFlightSeats, int pFlightPrice) {
    super(pRm);
    // Store our attributes.
    id = pId;
    flightNum = pFlightNum;
    flightSeats = pFlightSeats;
    flightPrice = pFlightPrice;
  }
  
  public void doCommand() throws Exception {
    // Perform the command.
    rm.addFlight(id, flightNum, flightSeats, flightPrice);
  }
}
