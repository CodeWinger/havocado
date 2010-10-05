package Commands.RMICommands;

import ResInterface.*;

public class QueryFlightRMICommand extends AbstractRMICommand {

  int id;
  int flightNumber;
  
  int numSeats;

  public QueryFlightRMICommand(ResourceManager pRm, int pId, int pFlightNumber) {
    super(pRm);
    // Store our attributes.
    id = pId;
    flightNumber = pFlightNumber;
    numSeats = -1;
  }
  
  public void doCommand() throws Exception {
    numSeats = rm.queryFlight(id, flightNumber);
  }
}
