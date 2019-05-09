/**
 * 
 */
package nurgs.tool.batch.processor;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import nurgs.domain.model.game.slot.SlotRound;

/**
 * @author pau.luna
 *
 */
public class ReindeerWildRoundTransformer extends AbstractSlotRoundTransformer{

    public ReindeerWildRoundTransformer() {
        super("REINDEER_WILDS_M4_RECORDER", "M4-0100");
    }

    @Override
    protected JsonArray buildBonusData(SlotRound round) {
        return null;
    }

    @Override
    protected JsonObject buildPerLineBets(SlotRound round) {
        return null;
    }

    @Override
    protected JsonArray buildScatterWins(SlotRound round) {
        return null;
    }

}
