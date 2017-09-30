/**
 * Project: Foops_1
 * Package: foop.core
 * File: Record.java
 * 
 * @author sidmishraw
 *         Last modified: Sep 29, 2017 1:06:59 PM
 */
package foop.core;

import java.util.Map;
import java.util.Set;

/**
 * <p>
 * `Record` holds the metadata for a transaction.
 * 
 * <p>
 * Contents of record are as follows:
 * <ul>
 * <li><b> status </b>: The status of the transaction.
 * 
 * <li><b> version </b>: The version counter that reflects the starting order of
 * the transaction.
 * 
 * <li><b> description </b>: The description of the transaction.
 * 
 * <li><b>writeSet</b>: The set of MemoryCells/addresses that the transaction
 * intends to write to.
 * 
 * <li><b> readSet </b>: The set of MemoryCells/addresses that the transaction
 * intends to read from.
 * 
 * <li><b> oldValues </b>: The set of oldValues of the MemoryCells/addresses
 * that acts as a backup incase the transaction fails to commit.
 * 
 * <br>
 * <br>
 * 
 * @author sidmishraw
 *
 *         Qualified Name: foop.core.Record
 *
 */
public class Record {
    
    private Boolean              status;
    private Integer              version;
    private String               description;
    private Set<Integer>         writeSet;
    private Set<Integer>         readSet;
    private Map<Integer, Object> oldValues;
    
    /**
     * @param status
     * @param version
     * @param description
     * @param writeSet
     * @param readSet
     * @param oldValues
     */
    private Record(Boolean status, Integer version, String description, Set<Integer> writeSet, Set<Integer> readSet,
            Map<Integer, Object> oldValues) {
        this.status = status;
        this.version = version;
        this.description = description;
        this.writeSet = writeSet;
        this.readSet = readSet;
        this.oldValues = oldValues;
    }
    
    /**
     * @return the status
     */
    public Boolean getStatus() {
        return this.status;
    }
    
    /**
     * @return the version
     */
    public Integer getVersion() {
        return this.version;
    }
    
    /**
     * @return the description
     */
    public String getDescription() {
        return this.description;
    }
    
    /**
     * @return the writeSet
     */
    public Set<Integer> getWriteSet() {
        return this.writeSet;
    }
    
    /**
     * @return the readSet
     */
    public Set<Integer> getReadSet() {
        return this.readSet;
    }
    
    /**
     * @return the oldValues
     */
    public Map<Integer, Object> getOldValues() {
        return this.oldValues;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.description == null) ? 0 : this.description.hashCode());
        result = prime * result + ((this.oldValues == null) ? 0 : this.oldValues.hashCode());
        result = prime * result + ((this.readSet == null) ? 0 : this.readSet.hashCode());
        result = prime * result + ((this.status == null) ? 0 : this.status.hashCode());
        result = prime * result + ((this.version == null) ? 0 : this.version.hashCode());
        result = prime * result + ((this.writeSet == null) ? 0 : this.writeSet.hashCode());
        return result;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Record)) {
            return false;
        }
        Record other = (Record) obj;
        if (this.description == null) {
            if (other.description != null) {
                return false;
            }
        } else if (!this.description.equals(other.description)) {
            return false;
        }
        if (this.oldValues == null) {
            if (other.oldValues != null) {
                return false;
            }
        } else if (!this.oldValues.equals(other.oldValues)) {
            return false;
        }
        if (this.readSet == null) {
            if (other.readSet != null) {
                return false;
            }
        } else if (!this.readSet.equals(other.readSet)) {
            return false;
        }
        if (this.status == null) {
            if (other.status != null) {
                return false;
            }
        } else if (!this.status.equals(other.status)) {
            return false;
        }
        if (this.version == null) {
            if (other.version != null) {
                return false;
            }
        } else if (!this.version.equals(other.version)) {
            return false;
        }
        if (this.writeSet == null) {
            if (other.writeSet != null) {
                return false;
            }
        } else if (!this.writeSet.equals(other.writeSet)) {
            return false;
        }
        return true;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Record [" + (this.status != null ? "status=" + this.status + ", " : "")
                + (this.version != null ? "version=" + this.version + ", " : "")
                + (this.description != null ? "description=" + this.description + ", " : "")
                + (this.writeSet != null ? "writeSet=" + this.writeSet + ", " : "")
                + (this.readSet != null ? "readSet=" + this.readSet + ", " : "")
                + (this.oldValues != null ? "oldValues=" + this.oldValues : "") + "]";
    }
}
