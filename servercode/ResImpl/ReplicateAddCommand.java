package ResImpl;

import Commands.RMICommands.AbstractRMICommand;

public class ReplicateAddCommand implements ReplicationCommand {

	public AbstractRMICommand command;
	
	public ReplicateAddCommand(AbstractRMICommand pCommand) {
		command = pCommand;
	}
	
	public void execute(HavocadoFlesh f) {
		// TODO Auto-generated method stub

	}

}
