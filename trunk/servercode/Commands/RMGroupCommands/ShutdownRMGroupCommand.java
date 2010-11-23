package Commands.RMGroupCommands;

import ResImpl.HavocadoSeed;

public class ShutdownRMGroupCommand extends AbstractRMGroupCommand {

	private static final long serialVersionUID = 1L;

	@Override
	public void doCommand(HavocadoSeed hs) throws Exception {
		System.exit(0);
	}

}
