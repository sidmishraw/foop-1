/**
 * Project: Foops_1
 * Package: foop.core
 * File: Transaction.java
 * 
 * @author sidmishraw
 *         Last modified: Sep 28, 2017 7:59:04 PM
 */
package foop.core;

import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.Setter;

/**
 * @author sidmishraw
 *
 *         Qualified Name: foop.core.Transaction
 *
 */
public class Transaction extends Thread {
    
    /*** Log and administrative stuff *****/
    private static final Logger logger              = LoggerFactory.getLogger(Transaction.class);
    private static final long   MAX_SLEEP_WAIT_TIME = 1000;
    /*** Log and administrative stuff *****/
    
    /**
     * <p>
     * record holds the metadata of the transaction
     */
    private @Setter Record      record;
    
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
    public static interface TransactionOperation {
        
        /**
         * <p>
         * Applies the operation logic
         * 
         * @return true if the operation was completed successfully, else return
         *         false
         * 
         */
        public boolean apply();
    }
    
    /**
     * <p>
     * <i>operation</i>: The functional interface that is used to define the
     * transaction's operational logic(execution logic).
     */
    private @Setter TransactionOperation operation;
    
    /**
     * <p>
     * The reference to the StateManager that takes care of global operations.
     */
    private @Setter StateManager         manager;
    
    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {
        
        logger.debug("Transaction:: " + this.getName() + " has started...");
        
        while (!this.record.getStatus()) {
            
            logger.info(String.format("Initiating transaction:: %s", this.getName()));
            logger.info(String.format("Taking ownership of `writeSet` members of transaction:: %s", this.getName()));
            
            // take ownership of `writeSet` members
            Boolean ownershipStatus = this.takeOwnership();
            
            if (!ownershipStatus) {
                
                // failed to take ownership, rollback and goto sleep, and then
                // retry from beginning
                
                logger.info(String.format(
                        "MODERATE:: Transaction %s has failed to take ownership of all of its writeSet members, retrying after sometime",
                        this.getName()));
                
                // since there has been no modification to the writeSet members
                // yet, the only change that needs to be reverted is
                // the release of writeSet members' ownership.
                this.releaseOwnership();
                
                try {
                    
                    Thread.sleep(MAX_SLEEP_WAIT_TIME);
                } catch (InterruptedException e) {
                    
                    logger.error(e.getMessage(), e);
                }
                
                continue;
            }
            
            logger.info(
                    String.format("Transaction:: %s has taken ownership successfully, now moving on to taking backups",
                            this.getName()));
            
            // take backup of the states of the read and write sets
            this.takeBackup();
            
            logger.info(
                    String.format("Transaction:: %s has taken backup, starting transaction operation", this.getName()));
            
            // apply the transaction's operational logic to the writeSet and
            // readSet members
            Boolean operationStatus = this.operation.apply();
            
            if (!operationStatus) {
                
                // failed to operate successfully, this transaction is flawed,
                // bailing out
                logger.error(String.format(
                        "CRITICAL:: Transaction:: %s has faulty operational logic, bailing out after rolling back",
                        this.getName()));
                
                this.rollback();
                
                this.record.setStatus(true);
                
                break;
            }
            
            logger.info(
                    String.format("Transaction:: %s operation completed, moving to commit changes...", this.getName()));
            
            // commit changes
            Boolean commitStatus = this.commit();
            
            if (!commitStatus) {
                
                // failed to commit changes to the writeSet, hence rolling back
                // and then retrying
                logger.error(String.format(
                        "MODERATE:: Transaction:: %s couldn't commit its changes, rolling back and retrying...",
                        this.getName()));
                
                this.rollback();
                
                try {
                    
                    Thread.sleep(MAX_SLEEP_WAIT_TIME);
                } catch (InterruptedException e) {
                    
                    logger.error(e.getMessage(), e);
                }
                
                continue;
            }
            
            logger.info(String.format(
                    "transaction:: %s has successfully committed its changes made to the writeSet members, marking transaction as completed.",
                    this.getName()));
            
            // since the commit was successful, the transaction releases all its
            // writeSet members of its ownership and marks itself as complete
            this.releaseOwnership();
            
            // marks itself as complete
            this.record.setStatus(true);
        }
        
        logger.debug("Transaction:: " + this.getName() + " has ended...");
    }
    
    /**
     * <p>
     * Takes ownership of all the `MemCells` referenced in the transaction's
     * `writeSet`.
     * 
     * @return <b>true</b> if all the transaction was able to take ownership of
     *         all writeSet members, else return <b>false</b>
     */
    private Boolean takeOwnership() {
        
        Queue<String> writeSet = new LinkedBlockingQueue<>(this.record.getWriteSet());
        
        int maxOwnershipCount = writeSet.size();
        
        // the status of the ownership phase, by default we assume that it fails
        // it is only successful if all the writeSet members are successfully
        // owned by the transaction
        Boolean status = false;
        
        while (!writeSet.isEmpty() && maxOwnershipCount > 0) {
            
            // the variableName of the `Variable` that needs to be
            // owned by this transaction
            String variableName = writeSet.poll();
            
            Optional<Transaction> currentOwner = this.manager.getOwner(variableName);
            
            if (!currentOwner.isPresent()) {
                
                // set owner -- successfully took ownership
                this.manager.setOwner(variableName, this);
                
                // log
                logger.debug(String.format("Transaction:: %s took ownership of Variable:: %s",
                        this.record.getDescription(), variableName));
                
                // reduce the number of ownerships remaining
                maxOwnershipCount--;
            }
        }
        
        if (maxOwnershipCount <= 0) {
            
            // all the members of the writeSet were owned successfully by this
            // transaction
            status = true;
        }
        
        return status;
    }
    
    /**
     * <p>
     * Takes the backup of all the members in the read set and write set.
     * <br>
     * <p>
     * In case of a rollback, the `<i>writeSet</i>` member contents are
     * restored, from the <i>record.oldValues</i>.
     * <p>
     * While committing, the values of the read set and oldValues is checked, if
     * they are different commit fails.
     */
    private void takeBackup() {
        
        Queue<String> writeSet = new LinkedBlockingQueue<>(this.record.getWriteSet());
        Queue<String> readSet = new LinkedBlockingQueue<>(this.record.getReadSet());
        
        while (!writeSet.isEmpty()) {
            
            // Backing up `writeSet` members
            String variableName = writeSet.poll();
            
            // get the current state of the `Variable` or `MemCell` for creating
            // the backup, need to be careful since the Transaction extends
            // Thread class it has a `State` enum internally and that will cause
            // name conflicts with my foop.core.State. The solution is to use
            // fully qualified name of my State class.
            Optional<foop.core.State> currentState = this.manager.read(variableName);
            
            if (currentState.isPresent()) {
                
                // if we got something for the current state, we need to store
                // it as backup
                this.record.getOldValues().put(variableName, currentState.get());
            }
        }
        
        while (!readSet.isEmpty()) {
            
            // Backing up `readSet` members
            String variableName = readSet.poll();
            
            Optional<foop.core.State> currentState = this.manager.read(variableName);
            
            if (currentState.isPresent()) {
                
                this.record.getOldValues().put(variableName, currentState.get());
            }
        }
    }
    
    /**
     * <p>
     * Rolls back all changes made by the transaction and releases ownerships of
     * the writeSet members as well.
     */
    private void rollback() {
        
        logger.debug("Initiating rollback for transaction:: " + this.getName());
        
        Queue<String> writeSet = new LinkedBlockingQueue<>(this.record.getWriteSet());
        
        while (!writeSet.isEmpty()) {
            
            String variableName = writeSet.poll();
            
            // fetch the backup
            foop.core.State backup = this.record.getOldValues().get(variableName);
            
            // restore the backup
            this.manager.write(variableName, backup);
        }
        
        // release all the writeSet members from ownership
        this.releaseOwnership();
        
        logger.debug("Rollback complete for transaction:: " + this.getName());
    }
    
    /**
     * <p>
     * Commits the changes made by the transaction to its writeSet members after
     * referring to the state's of its readSet members.
     * 
     * @return true if commit was successful else returns false
     */
    private Boolean commit() {
        
        logger.debug(String.format("Initiating commit for transaction:: %s", this.getName()));
        
        Boolean status = true;
        
        Queue<String> readSet = new LinkedBlockingQueue<>(this.record.getReadSet());
        
        while (!readSet.isEmpty()) {
            
            String variableName = readSet.poll();
            
            Optional<foop.core.State> currentState = this.manager.read(variableName);
            
            foop.core.State backup = this.record.getOldValues().get(variableName);
            
            if (currentState.isPresent()) {
                
                // there is some non-null state in the statetable.
                if (null == backup) {
                    
                    // this means that when the backup was taken, the readSet
                    // member was un-initiaized but now it has some non-null
                    // state this means that it has been modified in some way
                    // and this
                    // might not be good since the new state of the readSet
                    // member might cause some
                    // problem with states of the writeSet members.
                    
                    // commit failed
                    status = false;
                    break;
                } else if (!currentState.get().equals(backup)) {
                    
                    // backup is not null, now check if their values are equal
                    status = false;
                    break;
                } else {
                    
                    // backup of the readSet member matches its current state
                    status = true;
                }
            } else {
                
                // currentstate is empty or null, if old state was not null,
                // then there has been a change in state
                if (null == backup) {
                    
                    // means that both currentState of the readSet member and
                    // its backup were empty
                    status = true;
                } else {
                    
                    status = false;
                }
            }
        }
        
        logger.debug(String.format("Completing commit for transaction:: %s", this.getName()));
        
        return status;
    }
    
    /**
     * <p>
     * Releases the ownerships of all the writeSet member `MemCells`
     */
    private void releaseOwnership() {
        
        logger.debug(String.format("Initiating release of ownership of writeSet members of transaction:: %s",
                this.getName()));
        
        Queue<String> writeSet = new LinkedBlockingQueue<>(this.record.getWriteSet());
        
        while (!writeSet.isEmpty()) {
            
            String variableName = writeSet.poll();
            
            if (this.manager.getOwner(variableName).isPresent()
                    && this.manager.getOwner(variableName).get().equals(this)) {
                
                // release ownership only if this transaction owns it
                // this is to prevent race conditions(?)
                this.manager.releaseOwnership(variableName);
            }
        }
        
        logger.debug(
                String.format("Finished release of ownership of writeSet members of transaction:: %s", this.getName()));
    }
    
    /*** Book keeping methods **/
    
    /**
     * <p>
     * Adds the member <i>Variable</i> or `MemCell`s names to the writeSet of
     * the transaction.
     * 
     * @param variableNames
     *            The names of the `MemCell`s that this transaction intends to
     *            modify/write
     */
    public final void addWriteSetMembers(String... variableNames) {
        
        Set<String> writeSet = this.record.getWriteSet();
        
        for (String variableName : variableNames) {
            
            writeSet.add(variableName);
        }
    }
    
    /**
     * <p>
     * Adds the member <i>Variable</i> or `MemCell`s names to the `readSet` of
     * the transaction.
     * 
     * @param variableNames
     *            The names of the `MemCell`s that this transaction intends to
     *            read from.
     */
    public final void addReadSetMembers(String... variableNames) {
        
        Set<String> readSet = this.record.getReadSet();
        
        for (String variableName : variableNames) {
            
            // since the variables that are needed by the transaction in its
            // writeSet are going to be updated anyways
            // it would be a better idea to have them owned only once, hence the
            // variables that are already a part of the writeSet are not going
            // to be added to the readSet
            if (!this.record.getWriteSet().contains(variableName)) {
                
                readSet.add(variableName);
            }
        }
    }
    
    /*** Book keeping methods **/
}
