package org.perflogstacktrace4j.dto.tree;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import org.perflogstacktrace4j.model.stats.LongStatsHistogram;
import org.perflogstacktrace4j.model.stats.PerfTimeStats;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for AppCallTreeNode
 *
 */
public class AppCallTreeNodeDTO implements Serializable {

    /** internal for java.io.Serializable */
    private static final long serialVersionUID = 1L;

    // ------------------------------------------------------------------------

    // implicit (not set on DTO)... 
    // private final AppCallTreeNodeDTO parent;

    private /*final*/ String name;

    private LinkedHashMap<String, AppCallTreeNodeDTO> childMap = new LinkedHashMap<String, AppCallTreeNodeDTO>();

    private PerfTimeStats perfTimeStats = new PerfTimeStats();

    // -------------------------------------------------------------------------

    public AppCallTreeNodeDTO(String name) {
        this.name = name;
    }

    @JsonCreator
    public AppCallTreeNodeDTO(
    		@JsonProperty("name") String name, 
    		@JsonProperty("childMap") LinkedHashMap<String, AppCallTreeNodeDTO> childMap, 
    		@JsonProperty("perfTimeStats") PerfTimeStats perfTimeStats) {
		super();
		this.name = name;
		this.childMap = childMap;
		this.perfTimeStats = perfTimeStats;
	}

	// -------------------------------------------------------------------------

    public String getName() {
        return name;
    }

    public Collection<AppCallTreeNodeDTO> getChildList() {
        return Collections.unmodifiableCollection(childMap.values());
    }

    public int getChildCount() {
        return childMap.size();
    }

    public PerfTimeStats getPerfTimeStats() {
        return perfTimeStats;
    }

    public void setPerfTimeStats(PerfTimeStats p) {
        this.perfTimeStats = p;
    }

    public AppCallTreeNodeDTO findOrCreateChild(String name) {
        AppCallTreeNodeDTO res = childMap.get(name);
        if (res == null) {
            res = new AppCallTreeNodeDTO(name);
            childMap.put(name, res);
        }
        return res;
    }

    public AppCallTreeNodeDTO findOrCreateChildPath(List<String> path) {
        AppCallTreeNodeDTO curr = this;
        for(String childName : path) {
            curr = curr.findOrCreateChild(childName);
        }
        return curr;
    }
    

    public void addRecursive(AppCallTreeNodeDTO src) {
        perfTimeStats.incr(src.perfTimeStats);

        for(AppCallTreeNodeDTO srcChild : src.childMap.values()) {
            AppCallTreeNodeDTO child = findOrCreateChild(srcChild.name);
            child.addRecursive(srcChild);
        }
    }

    // delegate getter ...
    // ------------------------------------------------------------------------
    
    public int getCount() {
        return perfTimeStats.getElapsedTimeStats().getCount();
    }

    public long getElapsedTime() {
        return perfTimeStats.getElapsedTimeStats().getSum();
    }

    public double getElapsedTimeAverage() {
        return perfTimeStats.getElapsedTimeStats().getAverage();
    }

    public double getElapsedCpuTimeAverage() {
        return perfTimeStats.getThreadCpuTimeStats().getAverage();
    }

    public double getElapsedUserTimeAverage() {
        return perfTimeStats.getThreadUserTimeStats().getAverage();
    }
    
    // -------------------------------------------------------------------------

    @Override
    public String toString() {
        return "AppCallTreeNodeDTO[" + name + "]";
    }

    public static void dumpHeaderLine(StringBuilder sb) {
        sb.append("Indent;Path;Name" 
                + ";count;SumTime" 
                + ";AvgTime;AvgThreadUserTime;AvgThreadCpuTime" 
                + ";Max;DateReachingMax;StackReachingMax"
                + "\n"
                );
    }
    
    public void recursiveToStringDump(StringBuilder sb, String parentPath, int indent, int maxRecurseLevel) {
    	LongStatsHistogram timeStats = perfTimeStats.getElapsedTimeStats();
    	LongStatsHistogram threadUserTimeStats = perfTimeStats.getThreadUserTimeStats();
    	LongStatsHistogram threadCpuTimeStats = perfTimeStats.getThreadCpuTimeStats();
        String currPath = ((parentPath != null && parentPath.length() != 0)? parentPath + "/" : "") + name; 

        long count = timeStats.getCount();
        if (count != 0) {
            printIndent(sb, indent);
            sb.append(";\"" + currPath + "\""
                    + ";\"" + name + "\""
                    
                    + ";" + count
                    + ";" + timeStats.getSum()
                    
                    + ";" + timeStats.getAverage()
                    + ";" + threadUserTimeStats.getAverage()
                    + ";" + threadCpuTimeStats.getAverage()
                    
                    + ";" + timeStats.getMaxValue()
                    + ";" + new Date(timeStats.getTimeReachingMaxValue())
                    + ";\"" + timeStats.getStackReachingMaxValue() + "\""
                    
                    + "\n");
        }
        
        if (maxRecurseLevel == -1 || maxRecurseLevel > 0) {
            int childMaxRecurseLevel = (maxRecurseLevel == -1)? -1 : (maxRecurseLevel-1);
            int childIndent = indent + 4;
            for(AppCallTreeNodeDTO child : childMap.values()) {
                child.recursiveToStringDump(sb, currPath, childIndent, childMaxRecurseLevel); // *** recurse ***
            }
        }
    }

    private static void printIndent(StringBuilder sb, int indent) {
        for(int i = 0; i < indent; i++) {
            sb.append(' ');
        }
    }

}
