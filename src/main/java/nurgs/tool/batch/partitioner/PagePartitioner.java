/**
 * 
 */
package nurgs.tool.batch.partitioner;

import java.util.HashMap;
import java.util.Map;

import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;

/**
 * @author pau.luna
 */
public class PagePartitioner implements Partitioner {

    /*
     * (non-Javadoc)
     * @see org.springframework.batch.core.partition.support.Partitioner#partition(int)
     */
    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        // get total records
        long totalRecords = 1;
        // records per page => totalRecords/gridSize
        long pageSize = totalRecords % gridSize > 0 ? (totalRecords / gridSize) + 1 : totalRecords / gridSize;
        Map<String, ExecutionContext> result = new HashMap<>();

        for (int i = 0; i < gridSize; i++) {
            ExecutionContext value = new ExecutionContext();
            value.putInt("page", i);
            value.putLong("pageSize", pageSize);
            result.put("partition" + i, value);
        }
        
        return result;
    }

}
