/**
 * 
 */
package nurgs.tool.batch.processor;

import java.util.List;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import nurgs.domain.model.game.slot.SlotRound;
import nurgs.domain.model.game.slot.SlotScatterWin;
import nurgs.domain.model.game.slot.feature.SlotFeature;
import nurgs.domain.model.game.slot.feature.SlotFreeSpinFeature;

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
        JsonArray array = new JsonArray();
        SlotFreeSpinFeature feature = getFreeSpins(round.getFeatures());
        if(feature != null) {
            buildFreeSpinData(feature, round);
        }
        
        return array;
    }

    @Override
    protected JsonObject buildPerLineBets(SlotRound round) {
        return new JsonObject();
    }

    @Override
    protected JsonArray buildScatterWins(SlotRound round) {
        SlotScatterWin win = round.getBaseSpinResult().getScatterWins();
        JsonObject scatterWinJson = new JsonObject();
        scatterWinJson.put("kind", win.getOfKind());
        scatterWinJson.put("symbol", win.getSymbol());
        scatterWinJson.put("prize", win.getPrize());
        scatterWinJson.put("line", 0);
        scatterWinJson.put("wild_multiplier", 1);
        return new JsonArray().add(scatterWinJson);
    }
    
    
    private SlotFreeSpinFeature getFreeSpins(List<SlotFeature> list) {
        if(list != null) {
            for(SlotFeature sf : list) {
                if("FREE_SPIN".equals(sf.getType())) {
                    return (SlotFreeSpinFeature)sf;
                }
            }
        }
        return null;
    }
    
    
    private JsonObject buildFreeSpinData(SlotFreeSpinFeature feature, SlotRound round) {
        if(feature.getFreeSpinsLeft() > 0) {
            
        } else {
            JsonObject spinData = new JsonObject();
            spinData.put("total_won", feature.getTotalFreeSpinWinAmount());
            spinData.put("bonus_name", "Spin Bonus");
            spinData.put("feature_number", 1);
            spinData.put("bonus_retrigger", feature.getAddedSpin() > 0);
            spinData.put("bonus_data", freeSpinBonusData(feature));
            spinData.put("extra_spins_data", value);
            
            return spinData;
        }
        
        return null;
    }
    
    
    private JsonArray freeSpinBonusData(SlotFreeSpinFeature feature) {
        
        return null;
    }

}
