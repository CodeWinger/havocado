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
	
	/**
	 * Creates a new transaction with the given ID.
	 * @param lockManager A reference to the lock manager
	 * @param tId The ID of the transaction
	 */
	public Transaction(LockManager lockManager, int tId) {
		this.lockManager = lockManager;
		this.tId = tId;
		setTime();
		commandStack = new Stack<AbstractRMICommand>();
	}
	
	/**
	 * Calls undo on all the commands on the stack. Also clears the stack.
	 */
	private void undoAll() {
		AbstractRMICommand c;
		while (!commandStack.isEmpty()) {
			c = commandStack.pop();
			c.undo();
		}
	}
	
	/**
	 * Releases all locks associated with this transaction.
	 */
	private void releaseLocks() {
		lockManager.UnlockAll(tId);
	}
	
	/**
	 * Aborts the transaction. Undoes all the commands and releases all the
	 * locks it holds.
	 */
	public void abort() {
		undoAll();
		releaseLocks();
	}
	
	/**
	 * Commits the transaction. Releases all the locks it holds.
	 * @return true
	 */
	public boolean commit() {
		releaseLocks();
		return true;
	}
	
	/**
	 * Sets the time of the last command executed for this transaction.
	 */
	public void setTime() {
		timeOfLastUpdate = System.currentTimeMillis();
	}
	
	/**
	 * Gets the time of the last command executed for this transaction.
	 * @return The time of the last command executed for this transaction.
	 */
	public long getTime() {
		return timeOfLastUpdate;
	}
	
	/**
	 * Adds the command to this transaction and records the time.
	 * @param command The command to be added to this transaction.
	 */
	public void addCommand(AbstractRMICommand command) {
		commandStack.push(command);
		setTime();
	}
}
