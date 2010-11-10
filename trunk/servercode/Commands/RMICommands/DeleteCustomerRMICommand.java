package Commands.RMICommands;

import ResInterface.*;

public class DeleteCustomerRMICommand extends AbstractRMICommand {

  public ResourceManager carRm;
  public ResourceManager flightRm;
	public ResourceManager roomRm;

  public int id;
  public int customer;
  
  public ReturnTuple<Boolean> success;

  public DeleteCustomerRMICommand(
		ResourceManager pCarRm, 
		ResourceManager pFlightRm, 
		ResourceManager pRoomRm, 
		int pId, int pCustomer) 
	{
    super(pCarRm); // initialize the abstract constructor - this is only to set the error code to false.
    carRm = pCarRm;
    flightRm = pFlightRm;
    roomRm = pRoomRm;
    
    // Store our attributes.
    id = pId;
    customer = pCustomer;
    
    success = new ReturnTuple<Boolean>(false, null);
  }
  
  public void doCommand() throws Exception {
	  timestamp.stamp();
      ReturnTuple<Boolean> r1 = carRm.deleteCustomer(id, customer, timestamp);
      r1.timestamp.stamp();
      setTimestamp(r1.timestamp);
      
      ReturnTuple<Boolean> r2 = flightRm.deleteCustomer(id, customer, timestamp);
      r2.timestamp.stamp();
      setTimestamp(r2.timestamp);
      
      ReturnTuple<Boolean> r3 = roomRm.deleteCustomer(id, customer, timestamp);
      r3.timestamp.stamp();
      setTimestamp(r3.timestamp);
      
      success.result = r1.result && r2.result && r3.result;
  }
  
  public void undo() {
	  // TODO: undo this operation.
  }

	@Override
	public int getRequiredLock() {
		return LockManager.LockManager.WRITE;
	}
}
