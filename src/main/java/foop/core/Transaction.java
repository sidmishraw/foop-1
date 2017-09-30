/**
 * Project: Foops_1
 * Package: foop.core
 * File: Transaction.java
 * 
 * @author sidmishraw
 *         Last modified: Sep 28, 2017 7:59:04 PM
 */
package foop.core;

import java.util.Objects;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import foop.utils.Counter;

/**
 * @author sidmishraw
 *
 *         Qualified Name: foop.core.Transaction
 *
 */
public class Transaction extends Thread {
    
    private static final Logger logger = LoggerFactory.getLogger(Transaction.class);
    
    /**
     * <p>
     * record holds the metadata of the transaction
     */
    private Record              record;
    
    /**
     * <p>
     * Functional interface that is used to define the operations done by the
     * transaction.
     * 
     * @author sidmishraw
     *
     *         Qualified Name: foop.core.TransactionOperation
     *
     */
    @FunctionalInterface
    private interface TransactionOperation {
        
        public boolean apply();
    }
    
    /**
     * <p>
     * operations: The functional interface that is used to define the
     * transaction's operations.
     */
    private TransactionOperation operations;
    
    /**
     * <p>
     * The reference to the STM manager that handles the global operations.
     */
    private StateManager         manager;
    
    /**
     * <p>
     * Takes ownership of all the memory cells in the write set.
     * 
     * @return status of operation, true if successfully took ownership of all
     *         the memorycells, otherwise false
     */
    private Boolean takeOwnership() {
        
        Set<Integer> writeSet = this.record.getWriteSet();
        
        Counter ownershipCount = new Counter(0);
        
        writeSet.stream().forEach(memoryCellId -> {
            
            Transaction owner = this.manager.getOwner(memoryCellId);
            
            // take ownership only if the object is not owned by other
            // transaction
            if (Objects.isNull(owner)) {
                
                this.manager.setOwner(memoryCellId, this);
                ownershipCount.increment();
            } else if (this.equals(owner)) {
                
                // owned by itself, do nothing and move on
                ownershipCount.increment();
            }
        });
        
        // return true if ownership of all the writeset members was obtained
        if (ownershipCount.getValue() == this.record.getWriteSet().size()) {
            
            return true;
        } else {
            
            return false;
        }
    }
    
    /**
     * <p>
     * Takes the backup of all the members in the read set and write set.
     * <br>
     * <p>
     * Incase of a rollback, the write set member contents are restored, from
     * the record.oldValues.
     * <p>
     * While committing, the values of the read set and oldValues is checked, if
     * they are different commit fails.
     */
    private void takeBackup() {
        
        Set<Integer> writeSet = this.record.getWriteSet();
        Set<Integer> readSet = this.record.getReadSet();
        
        writeSet.stream().forEach(memoryCellId -> {
            
            // take backup of the states of the MemoryCell of writeSet
        });
        
        readSet.stream().forEach(memoryCellId -> {
            
            // take backup of the states of the MemoryCell of readSet
        });
    }
    
    /**
     * <p>
     * Releases the ownership of the memory cells in the write set
     * 
     * @return true if all were released successfully, else false
     */
    private Boolean releaseOwnership() {
        
        Set<Integer> writeSet = this.record.getWriteSet();
        
        Counter ownershipCount = new Counter(0);
        
        writeSet.stream().forEach(memoryCellId -> {
            
            Transaction owner = this.manager.getOwner(memoryCellId);
            
            // release ownership only if the object is owned by this transaction
            if (Objects.isNull(owner)) {
                
                // no owner to release, just move on
                ownershipCount.increment();
            } else if (this.equals(owner)) {
                
                // release if it the memory cell is owned by this transaction
                this.manager.releaseOwnership(memoryCellId);
                
                ownershipCount.increment();
            }
        });
        
        if (ownershipCount.getValue() == this.record.getWriteSet().size()) {
            
            return true;
        } else {
            
            return false;
        }
    }
}
