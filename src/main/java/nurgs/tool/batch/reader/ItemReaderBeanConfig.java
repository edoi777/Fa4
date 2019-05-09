/**
 * 
 */
package nurgs.tool.batch.reader;

import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.data.builder.MongoItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.query.Query;

import nurgs.domain.model.game.slot.SlotRound;

import static org.springframework.data.mongodb.core.query.Criteria.*;

import java.time.Instant;

/**
 * @author pau.luna
 */
@Configuration
public class ItemReaderBeanConfig {

    private int itemsPerProcess = 50;

    @Bean
    public MongoItemReaderBuilder<SlotRound> mongoItemReaderBuilder(ExecutionContext ec) {

        int page = ec.getInt("page");

        Pageable pageable = PageRequest.of(page, itemsPerProcess, new Sort(Direction.ASC, "start"));
        //@formatter:off
        Query query = Query.query(
                    where("partnerCode").is("")
                        .andOperator(where("gameCode").is(""))
                        .andOperator(
                                where("start")
                                .gte(Instant.ofEpochMilli(0L))
                                .lte(Instant.ofEpochMilli(0L))))
                    .with(pageable);
        //@formatter:on
        return new MongoItemReaderBuilder<SlotRound>().query(query);
    }

    @Bean
    public ItemReader<SlotRound> reader(MongoItemReaderBuilder<SlotRound> builder) {
        return builder.build();
    }

}
