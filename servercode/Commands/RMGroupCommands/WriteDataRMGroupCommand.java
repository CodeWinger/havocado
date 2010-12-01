package Commands.RMGroupCommands;

import ResImpl.HavocadoSeed;
import ResImpl.RMItem;

public class WriteDataRMGroupCommand extends AbstractRMGroupCommand {

	private static final long serialVersionUID = 1L;
	private int id;
	private String key;
	private RMItem value;
	
	public WriteDataRMGroupCommand(int id, String key, RMItem value) {
		this.id = id;
		this.key = key;
		this.value = value;
	}

	@Override
	public void doCommand(HavocadoSeed hs) throws Exception {
		hs.writeData(id, key, value);
	}
	
	public String toString() {
		return "WriteData";
	}

}
