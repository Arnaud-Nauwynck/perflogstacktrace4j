package org.perflogstacktrace4j.model.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import org.perflogstacktrace4j.dto.tree.AppCallTreeNodeDTO;
import org.perflogstacktrace4j.model.stats.PerfTimeStats;

/**
 * Tree Node corresponding to statistics of similar AppCallElt(s)
 *
 */
public class AppCallTreeNode {

    private final AppCallTreeNode parent;
    private final String childName;

    private Object childMapLock = new Object();

    /** copy on write map (idem Commons collection FastHashMap..)
     * read => multi-thread safe
     * write => use childMapLock + clone + change pointer
     */
    private LinkedHashMap<String, AppCallTreeNode> childMap = new LinkedHashMap<>();

    private PerfTimeStats timeStats = new PerfTimeStats();

    // -------------------------------------------------------------------------

    public AppCallTreeNode(AppCallTreeNode parent, String childName) {
        this.parent = parent;
        this.childName = childName;
    }

    // -------------------------------------------------------------------------

    public AppCallTreeNode getParent() {
        return parent;
    }

    public String getChildName() {
        return childName;
    }

    public List<String> getInvPath() {
        List<String> res = new ArrayList<String>();
        for (AppCallTreeNode p = this; p != null && p.parent != null; p = p.parent) {
            res.add(p.childName);
        }
        return res;
    }

    public List<String> getPath() {
        List<String> res = getInvPath();
        Collections.reverse(res);
        return res;
    }

    public String getPathString() {
        StringBuilder sb = new StringBuilder();
        List<String> invPath = getInvPath();
        for (int i = invPath.size() - 1; i >= 0; i--) {
            sb.append('/');
            sb.append(invPath.get(i));
        }
        return sb.toString();
    }

    public LinkedHashMap<String, AppCallTreeNode> getChildMap() {
        return childMap; // unsafe... should use Unmodifiable Map?
    }

    public PerfTimeStats getTimeStats() {
        return timeStats;
    }

    public AppCallTreeNode findOrCreateChild(String name) {
        AppCallTreeNode res = childMap.get(name);
        if (res == null) {
            // redo find, double check with synchronized!
            synchronized(childMapLock) {
                res = childMap.get(name);
                if (res == null) {
                    // copy on write
                    LinkedHashMap<String, AppCallTreeNode> newChildMap =
                        new LinkedHashMap<String, AppCallTreeNode>(childMap);
                    res = new AppCallTreeNode(this, name);
                    newChildMap.put(name, res);
                    this.childMap = newChildMap;
                }
            }
        }
        return res;
    }

    public void incrTimeStats(long time, long threadUserTime, long threadCpuTime) {
        timeStats.incr(time, threadUserTime, threadCpuTime);
    }

    public void addRecursive(AppCallTreeNode src) {
        timeStats.incr(src.timeStats);

        for(AppCallTreeNode srcChild : src.childMap.values()) {
            AppCallTreeNode child = findOrCreateChild(srcChild.childName);
            child.addRecursive(srcChild);
        }
    }

    public void addRecursive(AppCallTreeNodeDTO src) {
        timeStats.incr(src.getPerfTimeStats());

        for(AppCallTreeNodeDTO srcChild : src.getChildList()) {
            AppCallTreeNode child = findOrCreateChild(srcChild.getName());
            child.addRecursive(srcChild);
        }
    }

    public void clear() {
        timeStats.clear();

        synchronized(childMapLock) {
            childMap = new LinkedHashMap<String, AppCallTreeNode>();
        }
    }

    public void clearAndCopyTo(AppCallTreeNode dest) {
        timeStats.clearAndCopyTo(dest.timeStats);

        for(AppCallTreeNode child : childMap.values()) {
            AppCallTreeNode destChild = dest.findOrCreateChild(child.childName);
            child.clearAndCopyTo(destChild);
        }

        synchronized(childMapLock) {
            this.childMap = new LinkedHashMap<String, AppCallTreeNode>();
        }
    }

    public void clearAndCopyTo(AppCallTreeNodeDTO dest) {
        timeStats.clearAndCopyTo(dest.getPerfTimeStats());

        for(AppCallTreeNode child : childMap.values()) {
            AppCallTreeNodeDTO destChild = dest.findOrCreateChild(child.childName);
            child.clearAndCopyTo(destChild);
        }

        synchronized(childMapLock) {
            this.childMap = new LinkedHashMap<String, AppCallTreeNode>();
        }
    }

    public void copyTo(AppCallTreeNodeDTO dest) {
        timeStats.copyTo(dest.getPerfTimeStats());

        for(AppCallTreeNode child : childMap.values()) {
            AppCallTreeNodeDTO destChild = dest.findOrCreateChild(child.childName);
            child.copyTo(destChild);
        }
    }

    // -------------------------------------------------------------------------

    @Override
    public String toString() {
        return "AppCallTreeNode[" + getPathString() + "]";
    }

}
