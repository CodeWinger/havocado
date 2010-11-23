package Commands.RMGroupCommands;

import ResImpl.HavocadoSeed;

public class EditPriceRMGroupCommand extends AbstractRMGroupCommand {

	private static final long serialVersionUID = 1L;
	private int id;
	private String key;
	private int price;
	
	public EditPriceRMGroupCommand(int id, String key, int price) {
		this.id = id;
		this.key = key;
		this.price = price;
	}

	@Override
	public void doCommand(HavocadoSeed hs) throws Exception {
		hs.editPrice(id, key, price);
	}

}
