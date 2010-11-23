package Commands.RMGroupCommands;

import ResImpl.HavocadoSeed;

/**
 * Be careful using this class! The reserveItem method returns a value, but this command doesn't.
 * @author simon
 */
public class ReserveItemRMGroupCommand extends AbstractRMGroupCommand {

	private static final long serialVersionUID = 1L;
	private int id;
	private int customerID;
	private String key;
	private String location;
	
	public ReserveItemRMGroupCommand(int id, int customerID, String key, String location) {
		this.id = id;
		this.customerID = customerID;
		this.key = key;
		this.location = location;
	}

	@Override
	public void doCommand(HavocadoSeed hs) throws Exception {
		hs.reserveItem(id, customerID, key, location);
	}

}
