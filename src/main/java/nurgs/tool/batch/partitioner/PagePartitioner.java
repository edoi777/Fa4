/**
 * 
 */
package nurgs.tool.batch.partitioner;

import static org.springframework.data.mongodb.core.query.Criteria.where;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import nurgs.domain.model.game.slot.SlotRound;

/**
 * @author pau.luna
 */
public class PagePartitioner implements Partitioner {

    private static final Logger LOGGER = LoggerFactory.getLogger(PagePartitioner.class);

    private long startTimeInMilli;
    private long endTimeInMilli;
    
    public PagePartitioner(long startTimeInMilli, long endTimeInMilli) {
        this.startTimeInMilli = startTimeInMilli;
        this.endTimeInMilli = endTimeInMilli;
    }
    
    /*
     * (non-Javadoc)
     * @see org.springframework.batch.core.partition.support.Partitioner#partition(int)
     */
    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        LOGGER.info("grid size was {}", gridSize);
        // records per page => totalRecords/gridSize
        Map<String, ExecutionContext> result = new HashMap<>();
        long difference = endTimeInMilli - startTimeInMilli;
        long bufferTime = difference/gridSize;
        if(difference % gridSize > 0 ) {
            bufferTime++;
        }
            
        for (int i = 0; i < gridSize; i++) {
            ExecutionContext execContext = new ExecutionContext();
            execContext.putInt("page", i);
            long startTime = startTimeInMilli + (i * bufferTime);
            long endTime = startTime + bufferTime;
            if(endTime > endTimeInMilli) {
                endTime = endTimeInMilli;
            }
            execContext.putLong("start", startTime);
            execContext.putLong("end", endTime);
            execContext.putString("name", "Thread" + i);
            result.put("partition" + i, execContext);
        }
        return result;
    }
    

}
