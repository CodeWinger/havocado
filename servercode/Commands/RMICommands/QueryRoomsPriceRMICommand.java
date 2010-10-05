package Commands.RMICommands;

import ResInterface.*;

public class QueryRoomsPriceRMICommand extends AbstractRMICommand {

  int id;
  String location;
  
  int price;

  public QueryRoomsPriceRMICommand(ResourceManager pRm, int pId, String pLocation) {
    super(pRm);
    // Store our attributes.
    id = pId;
    location = pLocation;
    
    price = -1;
  }
  
  public void doCommand() throws Exception {
    price = rm.queryRoomsPrice(id, location);
  }
}
