package nurgs.tool.batch.processor;

import java.util.List;

import org.springframework.batch.item.ItemProcessor;

import nurgs.domain.model.game.slot.SlotRound;

public class SlotRoundProcessor implements ItemProcessor<SlotRound, List<String>> {

    private RoundConverterProvider provider;

    @Override
    public List<String> process(SlotRound item) throws Exception {

        SlotRoundTransformer transformer = provider.getSlotRoundTransformer(item.getGameCode());
        
        return transformer.transform(item);
    }

}
