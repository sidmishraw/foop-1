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
 * Needs to be initialized using the <i>InstanceFactory</i> just like the
 * <i>StateManager</i>
 * <br>
 * <br>
 * <b>Note: This is not thread safe for creating transactions</b>
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
    private int         transactionVersion = 0;
    
    /**
     * <p>
     * The static Transaction is going to be used to provide a non thread safe
     * builder style of making transactions by method chaining
     */
    private Transaction t                  = null;
    
    /**
     * <p>
     * Creates a new <i>Transaction</i> and sets the description of the
     * transaction, the operational logic and the reference to the
     * <i>StateManager</i> that is in charge of the world.
     * 
     * @param description
     *            The description of the transaction
     * 
     * @param manager
     *            The reference to the <i>StateManager</i> that is in charge of
     *            the world, i.e the stateTable, memory and stm.
     * 
     * @return The <i>Transactions</i> utility for builder method chaining
     */
    public final Transactions newTransaction(String description, StateManager manager) {
        
        Record record = new Record();
        record.setDescription(description);
        record.setVersion(transactionVersion);
        
        this.transactionVersion++;
        
        this.t = new Transaction();
        
        this.t.setRecord(record);
        this.t.setManager(manager);
        
        return this;
    }
    
    /**
     * <p>
     * Adds the <i>Variable</i>s or `MemCell`s to the `writeSet` of the
     * transaction.
     * 
     * @param variableNames
     *            The names of the `MemCell`s or <i>Variable</i>s
     * 
     * @return The <i>Transactions</i> utility for builder method chaining
     */
    public final Transactions addWriteSetMembers(String... variableNames) {
        
        this.t.addWriteSetMembers(variableNames);
        
        return this;
    }
    
    /**
     * <p>
     * Adds the <i>Variable</i>s or `MemCell`s to the `readSet` of the
     * transaction.
     * 
     * @param variableNames
     *            The names of the `MemCell`s or <i>Variable</i>s
     * 
     * @return The <i>Transactions</i> utility for builder method chaining
     */
    public final Transactions addReadSetMembers(String... variableNames) {
        
        this.t.addReadSetMembers(variableNames);
        
        return this;
    }
    
    /**
     * <p>
     * Adds the transaction's operational logic
     * 
     * @param operation
     *            The operational logic of the transaction
     * 
     * @return The <i>Transactions</i> utility for builder method chaining
     */
    public final Transactions addTransactionOperation(Transaction.TransactionOperation operation) {
        
        this.t.setOperation(operation);
        
        return this;
    }
    
    /**
     * <p>
     * The terminal method of the chaining, gives the constructed transaction
     * 
     * @return The constructed transaction
     */
    public final Transaction get() {
        
        return this.t;
    }
}
