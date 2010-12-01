package Commands.RMGroupCommands;

import ResImpl.HavocadoSeed;

/**
 * Be careful using this class! The deleteItem method returns a value, but this command doesn't.
 * @author simon
 */
public class DeleteItemRMGroupCommand extends AbstractRMGroupCommand {

	private static final long serialVersionUID = 1L;
	private int id;
	private String key;
	
	public DeleteItemRMGroupCommand(int id, String key) {
		this.id = id;
		this.key = key;
	}

	@Override
	public void doCommand(HavocadoSeed hs) throws Exception {
		hs.deleteItem(id, key);
	}
	
	public String toString() {
		return "DeleteItem "+id;
	}

}
