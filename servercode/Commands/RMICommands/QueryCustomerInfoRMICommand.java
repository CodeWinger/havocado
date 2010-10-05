package Commands.RMICommands;

import ResInterface.*;

public class QueryCustomerInfoRMICommand extends AbstractRMICommand {

  int id;
  int customer;
  
  String customerInfo;

  public QueryCustomerInfoRMICommand(ResourceManager pRm, int pId, int pCustomer) {
    super(pRm);
    // Store our attributes.
    id = pId;
    customer = pCustomer;
    
    customerInfo = null;
  }
  
  public void doCommand() throws Exception {
    customerInfo = rm.queryCustomerInfo(id, customer);
  }
}
