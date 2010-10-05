package Commands.RMICommands;

import ResInterface.*;

public class NewCustomerWithIdRMICommand extends AbstractRMICommand {

  int id;
  int cid;
  
  boolean success;

  public NewCustomerWithIdRMICommand(ResourceManager pRm, int pId, int pCid) {
    super(pRm);
    // Store our attributes.
    id = pId;
    cid = pCid;
    
    success = false;
  }
  
  public void doCommand() throws Exception {
    success = rm.newCustomer(id, cid);
  }
}
