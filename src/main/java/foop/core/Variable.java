/**
 * Project: Foops_1
 * Package: foop.core
 * File: Variable.java
 * 
 * @author sidmishraw
 *         Last modified: Sep 29, 2017 8:25:09 PM
 */
package foop.core;

/**
 * <p>
 * The variable, represents the row identifier in the state table.
 * 
 * <br>
 * 
 * <p>
 * It is supposed to contain all the values that represent the <em> Object </em>
 * uniquely in the world. It is associated with its <em> State </em> which is
 * maintained in the StateTable.
 * 
 * 
 * @author sidmishraw
 *
 *         Qualified Name: foop.core.Variable
 *
 */
public interface Variable {
    
    public abstract Integer getId();
}
