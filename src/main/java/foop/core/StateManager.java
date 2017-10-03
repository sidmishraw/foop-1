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
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * The manager is responsible for maintaing the object-state mappings, uses STM
 * to achieve this.
 * 
 * <br>
 * <br>
 * 
 * <p>
 * Extending the idea of `MemCell` assuming that the `MemCell` is the object
 * that we were continuously updating, we break it apart into 2 parts:
 * <ul>
 * <li>Immutable part -- Variable (Since `Object` is reserved by Java)
 * <li>Mutable part -- State
 * </ul>
 * 
 * and `associate` both parts with each other using a `<b>stateTable</b>`.
 * For now, let the `<i>Variable</i>` names be unique.
 * 
 * <br>
 * <br>
 * 
 * So, we have:
 * 
 * <br>
 * <br>
 * 
 * <code>
 * <b>Map&lt;String, Variable&gt; memory</b>
 * </code>
 * 
 * <br>
 * <br>
 * 
 * where <i>memory</i> is the collection of `MemCell`s.
 * 
 * <br>
 * <br>
 * 
 * Note: Need to hold the <i>Variable</i> objects some where else GC will
 * collect them xD
 * 
 * <br>
 * <br>
 * 
 * <code><b>Map&lt;String, State&gt; stateTable</b></code>
 * 
 * <br>
 * <br>
 * 
 * where <i>stateTable</i> `associates` <i>Variable</i> to <i>State</i> using
 * <i>Variable</i>'s `name` as key.
 * 
 * <br>
 * <br>
 * 
 * Now, since immutable part of `<i>MemCell</i>` represents the `<i>MemCell</i>`
 * itself, the <b>stm</b> becomes:
 * 
 * <br>
 * <br>
 * 
 * <code><b>Map&lt;String, Transaction&gt; stm</b></code>
 * 
 * <br>
 * <br>
 * 
 * The `<i>stm</i>` `associates` <i>Variable</i> to `<i>Transaction</i>` that
 * owns it during a particular time frame using the `<i>Variable</i>`'s `name`
 * as key.
 * 
 * <br>
 * <br>
 * 
 * So, `<b>effectively</b>` the collection of `MemCell`s is now represented by
 * the 2 Maps `stateTable` and `memory` (logically) and the `<i>stm</i>` still
 * represents the relation between the `<i>MemCell</i>`s and the
 * <i>Transaction</i>s.
 * 
 * <br>
 * <br>
 * 
 * The <i>stm</i>, <i>memory</i> and <i>stateTable</i> are managed by the
 * <i>StateManager</i>.
 * 
 * <br>
 * <br>
 * 
 * @author sidmishraw
 *
 *         Qualified Name: foop.core.StateManager
 */
public class StateManager {
    
    /** logging stuff **/
    private static final Logger      logger = LoggerFactory.getLogger(StateManager.class);
    /** logging stuff **/
    
    /**
     * <p>
     * The `memory` is used to hold the `Variable` object references so that
     * they don't get GC'ed. Moreover, the memory represents part of the
     * `MemCell` collections.
     */
    private Map<String, Variable>    memory;
    
    /**
     * <p>
     * The <i>stateTable</i> `associates` the immutable part of the `MemCell` to
     * its mutable part. It uses the <i>Variable</i>'s name as the key.
     */
    private Map<String, State>       stateTable;
    
    /**
     * Represents the Software Transactional Memory(STM)
     * 
     * The `<i>stm</i>` `associates` <i>Variable</i> to `<i>Transaction</i>`
     * that owns it during a particular time frame using the `<i>Variable</i>`'s
     * `name` as key.
     */
    private Map<String, Transaction> stm;
    
    /**
     * Initializes the StateManager with empty tables for each of the memory,
     * stm and stateTable.
     */
    public StateManager() {
        
        this.memory = new HashMap<>();
        this.stm = new HashMap<>();
        this.stateTable = new HashMap<>();
    }
    
    /****** STM operations START ********/
    
    /**
     * <p>
     * Gets the owner of the `MemCell`,
     * 
     * @param variableName
     *            The name of the `<i>Variable</i>` or `MemCell` that you want
     *            to <b>own</b>.
     * 
     * @return an Optional Transaction, which may be empty if the `MemCell` is
     *         not owned by any <i>Transaction</i>.
     */
    public Optional<Transaction> getOwner(String variableName) {
        
        return Optional.ofNullable(this.stm.get(variableName));
    }
    
    /**
     * <p>
     * Sets the owner of the `MemCell`,
     * 
     * @param variableName
     *            The name of the `<i>Variable</i>` or `MemCell` that you want
     *            to <b>own</b>.
     * 
     * @param owner
     *            The Transaction that now owns the `MemCell`.
     */
    public void setOwner(String variableName, Transaction owner) {
        
        this.stm.put(variableName, owner);
    }
    
    /**
     * <p>
     * Removes the owner transaction reference for the `MemCell`
     * 
     * @param variableName
     *            The name of the `<i>Variable</i>` or `MemCell` that you want
     *            to free from ownership
     * 
     */
    public void releaseOwnership(String variableName) {
        
        this.stm.remove(variableName);
    }
    
    /****** STM operations END ********/
    
    /****** Object - State, stateTable related START *******/
    
    /**
     * <p>
     * Makes you a brand new `<i>Variable</i>` or `MemCell` that is allocated in
     * the memory.(JK!)
     * 
     * @param variableName
     *            The name of the <i>Variable</i>
     * @param props
     *            The properties that can be present in the immutable part of
     *            the `MemCell` or <i>Variable</i>
     * 
     * @return The new <i>Variable</i>
     */
    public Variable make(String variableName, @SuppressWarnings("unchecked") Map.Entry<String, Object>... props) {
        
        Variable var = new Variable(variableName, props);
        
        // add the var to the memory
        this.memory.put(variableName, var);
        
        return var;
    }
    
    /**
     * <p>
     * Fetches the current state of the <i>Variable</i> or `MemCell`
     * 
     * @param variableName
     *            The name of the `<i>Variable</i>` whose current state is
     *            needed
     * 
     * @return The current state of the `<i>Variable</i>` which may be empty if
     *         the Variable never had any state, i.e The `MemCell` has not yet
     *         been initialized.
     */
    public Optional<State> read(String variableName) {
        
        logger.info(String.format("Variable :: name: %s, has state: %s", variableName,
                Optional.ofNullable(this.stateTable.get(variableName))));
        
        return Optional.ofNullable(this.stateTable.get(variableName));
    }
    
    /**
     * <p>
     * Writes the new state of the <i>Variable</i>, updating its state in the
     * `stateTable`.
     * <br>
     * This action symbolizes that the `MemCell`'s contents were updated.
     * 
     * @param variableName
     *            The name of the `<i>Variable</i>` whose state needs to be
     *            updated
     * 
     * @param state
     *            The new state of the `<i>Variable</i>`. This symbolizes that
     *            the `MemCell`'s contents have been updated to this value since
     *            `<i>State</i>` represents the `mutable` part of the `MemCell`.
     */
    public void write(String variableName, State state) {
        
        if (logger.isInfoEnabled()) {
            
            State oldState = Optional.ofNullable(this.stateTable.get(variableName)).orElse(null);
            
            logger.info(String.format("Updating Variable :: name: %s with current state: %s", variableName, oldState));
        }
        
        this.stateTable.put(variableName, state);
        
        logger.info(String.format("Updated Variable :: name: %s to new state: %s", variableName, state));
    }
    
    /****** Object - State, stateTable related END *******/
}
