package Commands.RMICommands;

import ResInterface.*;

public class DeleteCustomerRMICommand extends AbstractRMICommand {

  int id;
  int customer;
  
  boolean success;

  public DeleteCustomerRMICommand(ResourceManager pRm, int pId, int pCustomer) {
    super(pRm);
    // Store our attributes.
    id = pId;
    customer = pCustomer;
    
    success = false;
  }
  
  public void doCommand() throws Exception {
    success = rm.deleteCustomer(id, customer);
  }
}
