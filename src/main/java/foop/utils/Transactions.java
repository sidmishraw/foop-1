/**
 * Project: Foops_1
 * Package: foop.utils
 * File: Transactions.java
 * 
 * @author sidmishraw
 *         Last modified: Oct 3, 2017 4:47:06 PM
 */
package foop.utils;

import foop.core.Record;
import foop.core.StateManager;
import foop.core.Transaction;

/**
 * <p>
 * Provides utilities for creating <i>Transaction</i>s.
 * 
 * @author sidmishraw
 *
 *         Qualified Name: foop.utils.Transactions
 *
 */
public final class Transactions {
    
    /**
     * <p>
     * Just for the sake of simplicity, the transaction version is going to be a
     * simple int that will get updated for each transaction we make.
     */
    private static int transactionVersion = 0;
    
    /**
     * <p>
     * Creates a new <i>Transaction</i> and sets the description of the
     * transaction, the operational logic and the reference to the
     * <i>StateManager</i> that is in charge of the world.
     * 
     * @param description
     *            The description of the transaction
     * @param operation
     *            The operational logic of the transaction
     * @param manager
     *            The reference to the <i>StateManager</i> that is in charge of
     *            the world, i.e the stateTable, memory and stm.
     * 
     * @return The new transaction
     */
    public static final Transaction newTransaction(String description, Transaction.TransactionOperation operation,
            StateManager manager) {
        
        Record record = new Record();
        record.setDescription(description);
        record.setVersion(transactionVersion);
        
        transactionVersion++;
        
        Transaction t = new Transaction();
        
        t.setRecord(record);
        t.setOperation(operation);
        t.setManager(manager);
        
        return t;
    }
    
    /**
     * <p>
     * Adds the <i>Variable</i>s or `MemCell`s to the `writeSet` of the
     * transaction.
     * 
     * @param t
     *            The transaction
     * @param variableNames
     *            The names of the `MemCell`s or <i>Variable</i>s
     * @return The modified transaction
     */
    public static final Transaction addWriteSetMembers(Transaction t, String... variableNames) {
        
        t.addWriteSetMembers(variableNames);
        
        return t;
    }
    
    /**
     * <p>
     * Adds the <i>Variable</i>s or `MemCell`s to the `readSet` of the
     * transaction.
     * 
     * @param t
     *            The transaction
     * @param variableNames
     *            The names of the `MemCell`s or <i>Variable</i>s
     * @return The modified transaction
     */
    public static final Transaction addReadSetMembers(Transaction t, String... variableNames) {
        
        t.addReadSetMembers(variableNames);
        
        return t;
    }
}
