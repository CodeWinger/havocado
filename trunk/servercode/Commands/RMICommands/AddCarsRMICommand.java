package Commands.RMICommands;

import ResInterface.*;

public class AddCarsRMICommand extends AbstractRMICommand {

  public int id;
  public String location;
  public int numCars;
  public int price;
  
  public boolean success;

  public AddCarsRMICommand(ResourceManager pRm, int pId, String pLocation, int pNumCars, int pPrice) {
    super(pRm);
    // Store our attributes.
    id = pId;
    location = pLocation;
    numCars = pNumCars;
    price = pPrice;
    
    success = false;
  }
  
  public void doCommand() throws Exception {
    success = rm.addCars(id, location, numCars, price);
  }
  
  public void undo() {
	  try {
		  rm.deleteCars(id, location);
	  } catch (Exception e) {
		  e.printStackTrace();
	  }
  }

	@Override
	public RequiredLock getRequiredLock() {
		return RequiredLock.WRITE;
	}
}
