package Commands.RMGroupCommands;

import ResImpl.HavocadoSeed;

public class UnreserveItemRMGroupCommand extends AbstractRMGroupCommand {

	private static final long serialVersionUID = 1L;
	private int id;
	private int customerID;
	private String key;
	private String location;
	
	public UnreserveItemRMGroupCommand(int id, int customerID, String key, String location) {
		this.id = id;
		this.customerID = customerID;
		this.key = key;
		this.location = location;
	}

	@Override
	public void doCommand(HavocadoSeed hs) throws Exception {
		hs.unreserveItem(id, customerID, key, location);
	}
	
	public String toString() {
		return "UnreserveItem";
	}

}
