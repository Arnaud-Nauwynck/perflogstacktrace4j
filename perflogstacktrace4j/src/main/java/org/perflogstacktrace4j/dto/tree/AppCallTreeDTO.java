package org.perflogstacktrace4j.dto.tree;

import java.io.Serializable;
import java.util.Date;

/**
 * DTO for a Call Tree statistics
 * 
 * see corresponding class AppCallTree
 */
public class AppCallTreeDTO implements Serializable {

    /** */
	private static final long serialVersionUID = 1L;

	private AppCallTreeNodeDTO rootNode;

    private String name;

    private Date fromSnapshotDate;

    private Date toSnapshotDate;

    // ------------------------------------------------------------------------

    public AppCallTreeDTO() {
    }

    // ------------------------------------------------------------------------

    public AppCallTreeNodeDTO getRootNode() {
        return rootNode;
    }

    public void setRootNode(AppCallTreeNodeDTO rootNode) {
        this.rootNode = rootNode;
    }

    public Date getFromSnapshotDate() {
        return fromSnapshotDate;
    }

    public void setFromSnapshotDate(Date fromSnapshotDate) {
        this.fromSnapshotDate = fromSnapshotDate;
    }

    public Date getToSnapshotDate() {
        return toSnapshotDate;
    }

    public void setToSnapshotDate(Date toSnapshotDate) {
        this.toSnapshotDate = toSnapshotDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // ------------------------------------------------------------------------

    @Override
    public String toString() {
        return "AppCallTreeDTO [" 
            + "name=" + name 
            + ", fromSnapshotDate=" + fromSnapshotDate 
            + ", toSnapshotDate=" + toSnapshotDate 
            + "]";
    }

}
