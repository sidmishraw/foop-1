/**
 * Project: Foops_1
 * Package: foop.core
 * File: StateManager.java
 * 
 * @author sidmishraw
 *         Last modified: Sep 28, 2017 7:51:42 PM
 */
package foop.core;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * The manager is responsible for maintaing the object-state mappings, uses STM
 * to achieve this.
 * 
 * @author sidmishraw
 *
 *         Qualified Name: foop.core.StateManager
 */
public class StateManager {
    
    /**
     * <p>
     * The statetable associates the variable with its state.
     */
    private Map<Integer, State>       stateTable;
    
    /**
     * Internally uses the Software Transactional Memory(STM)
     * 
     * smt holds the mapping between the MemoryCell and the transaction that has
     * taken ownership of the cell.
     */
    
    private Map<Integer, Transaction> stm;
    
    /**
     * The private constructor to make the {@link StateManager} singleton
     */
    public StateManager() {
        
        this.stm = new HashMap<>();
        this.stateTable = new HashMap<>();
    }
    
    /**
     * <p>
     * Gets the owner of the memory Cell, null if no owner
     * 
     * @param memoryCellId
     *            The identifier of the MemoryCell
     * @return the owning transaction or <b>null
     */
    public Transaction getOwner(Integer memoryCellId) {
        
        return this.stm.get(memoryCellId);
    }
    
    /**
     * <p>
     * Sets the owner of the memoryCell
     * 
     * @param memoryCellId
     *            The identifier of the Memory cell
     * @param owner
     *            The transaction that now owns the memory cell
     */
    public void setOwner(Integer memoryCellId, Transaction owner) {
        
        this.stm.put(memoryCellId, owner);
    }
    
    /**
     * <p>
     * Removes the owner transaction for the memorycell given the memory cell Id
     * 
     * @param memoryCellId
     *            the memory cell identifier
     */
    public void releaseOwnership(Integer memoryCellId) {
        
        this.stm.remove(memoryCellId);
    }
    
    /****** Object - State, state table related *******/
    
    /**
     * <p>
     * Fetches the current state of the variable
     * 
     * @param variable
     *            The variable whose state is needed
     * @return The current state of the variable
     */
    public State read(Variable variable) {
        
        return this.stateTable.get(variable.getId());
    }
    
    /**
     * <p>
     * Writes the new state of the variable, updating its state in the state
     * table.
     * 
     * @param variable
     *            The new state
     * @param state
     *            The new state of the variable that is associated with it in
     *            the state table.
     */
    public <V> void write(Variable variable, State state) {
        
        this.stateTable.put(variable.getId(), state);
    }
    
    /****** Object - State, state table related *******/
}
