package ResImpl;

import java.util.HashMap;
import java.util.HashSet;

import Commands.RMICommands.AbstractRMICommand;
import LockManager.LockManager;

public class Overseer extends Thread{
	/**
	 * The number of milliseconds after which a transaction will be aborted if
	 * no new commands are sent.
	 */
	private final static long timeout = 10000;
	
	/**
	 * A set of the currently active transactions. All methods that modify this
	 * should be synchronized, since HashSets aren't.
	 */
	private HashSet<Transaction> currentTransactions;
	
	/** The next transaction id to be returned */
	private int nextTId = 0;
	
	/** A mapping between transactions and their Ids. */
	private HashMap<Integer, Transaction> transactionIdMap;
	
	/** A boolean that is used to kill the thread. */
	private boolean alive = true;
	
	private void updateTTL(Transaction t) {
		
	}
	
	/**
	 * Check each transaction and kills ones that have been inactive for
	 * too long.
	 */
	public void run() {
		while(alive) {
			synchronized (this) {
				for (Transaction t : currentTransactions) {
					if (System.currentTimeMillis() - t.getTime() > timeout)
						t.abort();
				}
			}
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				// Do nothing
			}
		}
	}
	
	/**
	 * Crates a new transaction and returns its ID.
	 * @param lockManager A reference to the lock manager.
	 * @return The ID of the new transaction
	 */
	public synchronized int createTransaction(LockManager lockManager) {
		//TODO: Avoid tIds already in use
		Transaction t = new Transaction(lockManager, nextTId);
		currentTransactions.add(t);
		transactionIdMap.put(new Integer(nextTId), t);
		return nextTId++;
	}
	
	/**
	 * Removes a transaction from the hashset and the id from the hashmap.
	 * Should only be called in commit() or abort().
	 * @param tId The ID of the transaction to be deleted.
	 */
	private synchronized void deleteTransaction(int tId) {
		Integer i = new Integer(tId);
		Transaction t = transactionIdMap.get(i);
		currentTransactions.remove(t);
		transactionIdMap.remove(i);
	}
	
	/**
	 * Commits the transaction with tId.
	 * @param tId The ID of the transaction
	 * @return false if tId is invalid or if commit fails, true otherewise.
	 */
	public synchronized boolean commit(int tId) {
		//TODO: Maybe add check that tId is valid.
		Integer i = new Integer(tId);
		// Check validity of tId.
		if (!transactionIdMap.containsKey(i))
			return false;
		// Get transaction and commit.
		Transaction t = transactionIdMap.get(i);
		boolean result = t.commit();
		deleteTransaction(tId);
		return result;
	}
	
	/**
	 * Aborts the transaction with tId.
	 * @param tId The ID of the transaction to be aborted.
	 */
	public synchronized void abort(int tId) {
		Integer i = new Integer(tId);
		Transaction t = transactionIdMap.get(i);
		t.abort();
		deleteTransaction(tId);
		return;
	}
	
	/**
	 * Adds the given command to the transaction with tId.
	 * @param tId The ID of the transaction.
	 * @param command A command to add to the transaction.
	 * @return false if the ID is invalid, true otherwise.
	 */
	public synchronized boolean addCommandToTransaction(int tId, AbstractRMICommand command) {
		Integer i = new Integer(tId);
		// Check validity of tId.
		if (!transactionIdMap.containsKey(i))
			return false;
		Transaction t = transactionIdMap.get(i);
		t.addCommand(command);
		t.setTime();
		return true;
	}
	
	/** Kills the Overseer thread. */
	public void kill() {
		alive = false;
	}
}
