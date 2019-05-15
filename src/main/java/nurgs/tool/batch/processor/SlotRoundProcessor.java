package nurgs.tool.batch.processor;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;

import nurgs.domain.model.game.slot.SlotRound;
import nurgs.tool.batch.transformer.RoundConverterProvider;
import nurgs.tool.batch.transformer.SlotRoundTransformer;

public class SlotRoundProcessor implements ItemProcessor<SlotRound, List<String>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SlotRoundProcessor.class);
    
    private RoundConverterProvider provider;
    
    public SlotRoundProcessor(RoundConverterProvider provider) {
        this.provider = provider;
    }
    
    @Value("#{stepExecutionContext[name]}")
    private String threadName;
    

    @Override
    public List<String> process(SlotRound item) throws Exception {
        try {
            SlotRoundTransformer transformer = provider.getSlotRoundTransformer(item.getGameCode());
            return transformer.transform(item); 
        } catch(Exception e) {
            LOGGER.error("Error occured while processing {}", item, e);
            throw e;
        }
    }

}
