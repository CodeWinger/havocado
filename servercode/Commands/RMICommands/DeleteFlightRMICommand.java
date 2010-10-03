package Commands.RMICommands;

import ResInterface.*;

public class DeleteFlightRMICommand extends AbstractRMICommand {

  int id;
  int flightNum;

  public DeleteFlightRMICommand(ResourceManager pRm, int pId, int pFlightNum) {
    super(pRm);
    // Store our attributes.
    id = pId;
    flightNum = pFlightNum;
  }
  
  public void doCommand() throws Exception {
    // Perform the command.
    rm.deleteFlight(id, flightNum);
  }
}

