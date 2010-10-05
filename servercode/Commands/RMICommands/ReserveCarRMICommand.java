package Commands.RMICommands;

import ResInterface.*;

public class ReserveCarRMICommand extends AbstractRMICommand {

  int id;
  int customer;
  String location;
  
  boolean success;

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
