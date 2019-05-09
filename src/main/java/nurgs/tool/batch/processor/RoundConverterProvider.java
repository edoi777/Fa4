/**
 * 
 */
package nurgs.tool.batch.processor;

import java.util.HashSet;
import java.util.Set;

/**
 * @author pau.luna
 */
public class RoundConverterProvider {

    private Set<SlotRoundTransformer> transformers = new HashSet<>();

    public SlotRoundTransformer getSlotRoundTransformer(String gameCode) {
        for (SlotRoundTransformer transformer : transformers) {
            if (transformer.isSupported(gameCode)) {
                return transformer;
            }
        }

        throw new RuntimeException();
    }
    
    
    public RoundConverterProvider add(SlotRoundTransformer transformer) {
        transformers.add(transformer);
        return this;
    }
    
    
}
