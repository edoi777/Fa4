/**
 * 
 */
package nurgs.tool.batch.transformer;

import java.util.List;

import nurgs.domain.model.game.slot.SlotRound;

/**
 * @author pau.luna
 *
 */
public interface SlotRoundTransformer {
    
    /**
     * Check if this transformer support the passed game code
     * @param gameCodes
     * @return
     */
    boolean isSupported(String gameCode);
    
    
    /**
     * Transforms slot round into cqslh statement(either insert or update)
     * @param item
     * @return
     */
    List<String> transform(SlotRound item);
    
    
}
