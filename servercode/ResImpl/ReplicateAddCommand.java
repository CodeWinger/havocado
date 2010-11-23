package ResImpl;

import Commands.RMICommands.AbstractRMICommand;

public class ReplicateAddCommand implements ReplicationCommand {

	public int id;
	public AbstractRMICommand command;
	
	public ReplicateAddCommand(int pId, AbstractRMICommand pCommand) {
		id = pId;
		command = pCommand;
	}
	
	public void execute(HavocadoFlesh f) {
		// the LinkedList<MemberInfo> is already serialized in the pCommand
		// so we don't actually have to repopulate the pCommand's list
		// of member infos
		f.overseer.replicateAddCommandToTransaction(id, command);
	}

}
