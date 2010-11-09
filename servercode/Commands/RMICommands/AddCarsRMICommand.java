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
  
  private int previousNumCars;
  private int previousPrice;
  
  public void doCommand() throws Exception {
	  
	  success = rm.addCars(id, location, numCars, price);
  }
  
  public void undo() {
	  try {
		  // I want to revert back to how many cars there were before, with the previous price.
		  // step 1: delete the cars that I added.
		  // step 2: set the price back to the previous price.
	  } catch (Exception e) {
		  e.printStackTrace();
	  }
  }

	@Override
	public RequiredLock getRequiredLock() {
		return RequiredLock.WRITE;
	}
}
