package Commands.RMICommands;

import ResInterface.*;

public class ReserveCarRMICommand extends AbstractRMICommand {

  public int id;
  public int customer;
  public String location;
  
  public boolean success;

  public ReserveCarRMICommand(ResourceManager pRm, int pId, int pCustomer, String pLocation) {
    super(pRm);
    // Store our attributes.
    id = pId;
    customer = pCustomer;
    location = pLocation;
    
    success = false;
  }
  
  public void doCommand() throws Exception {
    success = rm.reserveCar(id, customer, location);
  }
}
