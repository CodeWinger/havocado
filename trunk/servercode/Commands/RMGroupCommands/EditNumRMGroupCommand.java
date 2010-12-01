package Commands.RMGroupCommands;

import ResImpl.HavocadoSeed;

public class EditNumRMGroupCommand extends AbstractRMGroupCommand {

	private static final long serialVersionUID = 1L;
	private int id;
	private String key;
	private int qty;
	
	public EditNumRMGroupCommand(int id, String key, int qty) {
		this.id = id;
		this.key = key;
		this.qty = qty;
	}

	@Override
	public void doCommand(HavocadoSeed hs) throws Exception {
		hs.editNum(id, key, qty);
	}
	
	public String toString() {
		return "EditNum "+id+" key: "+key+" qty: "+qty;
	}

}
