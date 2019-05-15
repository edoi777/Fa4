/**
 * 
 */
package nurgs.tool.batch.transformer;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import nurgs.domain.model.game.slot.SlotRound;
import nurgs.domain.model.game.slot.SlotScatterWin;

/**
 * @author pau.luna
 */
public class FafafaRoundTransformer extends AbstractSlotRoundTransformer {
    private static final Map<String, String> SYMBOL_MAPPING = new HashMap<>();

    static {
        SYMBOL_MAPPING.put("RED", "PIC1");
        SYMBOL_MAPPING.put("BLUE", "PIC2");
        SYMBOL_MAPPING.put("GREEN", "PIC3");
        SYMBOL_MAPPING.put("BLANK", "PIC4");
    }

    public FafafaRoundTransformer() {
        super("M4-0086", "M4-0099");
    }

    @Override
    protected String getSymbol(String symbol) {
        return SYMBOL_MAPPING.get(symbol);
    }

    @Override
    protected String getWinningSymbol(String winningSymbol) {
        for (Entry<String, String> entry : SYMBOL_MAPPING.entrySet()) {
            winningSymbol = winningSymbol.replace(entry.getKey(), entry.getValue());
        }
        return winningSymbol;
    }

    @Override
    protected JsonArray buildBonusData(SlotRound round) {
        return new JsonArray();
    }

    @Override
    protected JsonObject buildPerLineBets(SlotRound round) {
        return new JsonObject();
    }

    @Override
    protected JsonArray buildScatterWins(SlotScatterWin win) {
        return new JsonArray();
    }
}
