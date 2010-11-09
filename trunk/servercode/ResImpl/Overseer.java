package ResImpl;

import java.util.HashSet;
import java.util.Hashtable;

import Commands.RMICommands.AbstractRMICommand;
import exceptions.*;
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
	private Hashtable<Integer, Transaction> currentTransactions;
	
	/** The IDs of the transactions that have been aborted. */
	private HashSet<Integer> abortedIds;
	
	/** The next transaction id to be returned */
	private int nextTId = 0;
	
	/** A boolean that is used to kill the thread. */
	private boolean alive = true;
	
	/**
	 * Check each transaction and kills ones that have been inactive for
	 * too long.
	 */
	public void run() {
		while(alive) {
			synchronized (this) {
				for (Transaction t : currentTransactions.values()) {
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
		currentTransactions.put(new Integer(nextTId), t);
		return nextTId++;
	}
	
	/**
	 * Removes a transaction from the hashset and the id from the hashmap.
	 * Should only be called in commit() or abort().
	 * @param tId The ID of the transaction to be deleted.
	 */
	private synchronized void deleteTransaction(int tId) {
		Integer i = new Integer(tId);
		currentTransactions.remove(i);
	}
	
	/**
	 * Commits the transaction with tId.
	 * @param tId The ID of the transaction
	 * @return false if tId is invalid or if commit fails, true otherewise.
	 */
	public synchronized boolean commit(int tId) throws TransactionAbortedException, InvalidTransactionException {
		Integer i = new Integer(tId);
		Transaction t = currentTransactions.get(i);
		// Check validity of tId.
		if (t == null) {
			if (abortedIds.contains(i))
				return false;
			else
				throw new InvalidTransactionException();
		}
		// Commit and delete transaction.
		boolean result = t.commit();
		deleteTransaction(tId);
		return result;
	}
	
	/**
	 * Aborts the transaction with tId.
	 * @param tId The ID of the transaction to be aborted.
	 */
	public synchronized void abort(int tId) throws TransactionAbortedException, InvalidTransactionException {
		Integer i = new Integer(tId);
		Transaction t = currentTransactions.get(i);
		// Check validity of tId.
		if (t == null) {
			if (abortedIds.contains(i))
				throw new TransactionAbortedException();
			else
				throw new InvalidTransactionException();
		}
		// Abort and delete transaction.
		abortedIds.add(i);
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
	public synchronized boolean addCommandToTransaction(int tId, AbstractRMICommand command) throws TransactionAbortedException, InvalidTransactionException {
		Integer i = new Integer(tId);
		Transaction t = currentTransactions.get(i);
		// Check validity of tId.
		if (t == null) {
			if (abortedIds.contains(i))
				throw new TransactionAbortedException();
			else
				throw new InvalidTransactionException();
		}
		// Add command and update transaction TTL.
		t.addCommand(command);
		t.setTime();
		return true;
	}
	
	/** Kills the Overseer thread. */
	public void kill() {
		alive = false;
	}
}
