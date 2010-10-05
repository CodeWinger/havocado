package Commands.RMICommands;

import ResInterface.*;

public class ReserveFlightRMICommand extends AbstractRMICommand {

  public int id;
  public int customer;
  public int flightNumber;
  
  public boolean success;

  public ReserveFlightRMICommand(ResourceManager pRm, int pId, int pCustomer, int pFlightNumber) {
    super(pRm);
    // Store our attributes.
    id = pId;
    customer = pCustomer;
    flightNumber = pFlightNumber;
    
    success = false;
  }
  
  public void doCommand() throws Exception {
    success = rm.reserveFlight(id, customer, flightNumber);
  }
}
