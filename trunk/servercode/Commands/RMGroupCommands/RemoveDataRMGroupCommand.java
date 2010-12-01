package Commands.RMGroupCommands;

import ResImpl.HavocadoSeed;

/**
 * Be careful using this class! The removeData method returns a value, but this command doesn't.
 * @author simon
 */
public class RemoveDataRMGroupCommand extends AbstractRMGroupCommand {

	private static final long serialVersionUID = 1L;
	private int id;
	private String key;

	public RemoveDataRMGroupCommand(int id, String key) {
		this.key = key;
		this.id = id;
	}

	@Override
	public void doCommand(HavocadoSeed hs) throws Exception {
		hs.removeData(id, key);
	}
	
	public String toString() {
		return "RemoveData "+id;
	}

}
