package ResImpl;

import java.util.Stack;

import Commands.RMICommands.AbstractRMICommand;
import LockManager.LockManager;

public class Transaction {
	/** The time at which the transaction last had a command sent to it. */
	private long timeOfLastUpdate;
	
	/** The transaction ID  */
	private int tId;
	
	/** A stack of all commands associated with this transaction. */
	private Stack<AbstractRMICommand> commandStack;
	
	/** A reference to the lock manager. */
	private LockManager lockManager;
	
	public Transaction(LockManager lockManager, int tId) {
		this.lockManager = lockManager;
		this.tId = tId;
		commandStack = new Stack<AbstractRMICommand>();
	}
	
	private void undoAll() {
		//for ()
	}
	
	private void releaseLocks() {
		
	}
	
	public void abort() {
		undoAll();
	}
	
	public boolean commit() {
		return true;
	}
	
	public void setTime() {
		timeOfLastUpdate = System.currentTimeMillis();
	}
	
	public long getTime() {
		return timeOfLastUpdate;
	}
	
	public void addCommand(AbstractRMICommand command) {
		commandStack.push(command);
		setTime();
	}
}
