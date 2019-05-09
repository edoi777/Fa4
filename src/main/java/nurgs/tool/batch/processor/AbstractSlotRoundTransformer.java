/**
 * 
 */
package nurgs.tool.batch.processor;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author pau.luna
 */
public abstract class AbstractSlotRoundTransformer implements SlotRoundTransformer {

    private Set<String> supportedGames = new HashSet<>();

    public AbstractSlotRoundTransformer(String... supportedGameCodes) {
        supportedGames.addAll(Arrays.asList(supportedGameCodes));
    }

    @Override
    public boolean isSupported(String gameCode) {
        return supportedGames.contains(gameCode);
    }

}
