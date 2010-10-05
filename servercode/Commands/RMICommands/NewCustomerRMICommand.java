package Commands.RMICommands;

import ResInterface.*;

public class NewCustomerRMICommand extends AbstractRMICommand {

  public int id;
  
  public int customer;

  public NewCustomerRMICommand(ResourceManager pRm, int pId) {
    super(pRm);
    // Store our attributes.
    id = pId;
    
    customer = -1;
  }
  
  public void doCommand() throws Exception {
    customer = rm.newCustomer(id);
  }
}
