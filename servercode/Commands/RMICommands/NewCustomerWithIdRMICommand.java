package Commands.RMICommands;

import ResInterface.*;

public class NewCustomerWithIdRMICommand extends AbstractRMICommand {

  public int id;
  public int cid;
  
  public boolean success;

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
