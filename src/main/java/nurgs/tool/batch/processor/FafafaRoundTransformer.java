/**
 * 
 */
package nurgs.tool.batch.processor;

import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.QueryBuilder;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import nurgs.domain.model.game.slot.SlotRound;
import nurgs.domain.model.game.slot.spinner.SpinnedReel;

/**
 * @author pau.luna
 *
 */
public class FafafaRoundTransformer extends AbstractSlotRoundTransformer {

    private static final Map<String, String> SYMBOL_MAPPING = new HashMap<>();
    
    static {
        SYMBOL_MAPPING.put("RED", "PIC1");
        SYMBOL_MAPPING.put("BLUE", "PIC2");
        SYMBOL_MAPPING.put("GREEN", "PIC3");
        SYMBOL_MAPPING.put("BLANK", "PIC4");
    }
    
    /* (non-Javadoc)
     * @see nurgs.tool.batch.processor.SlotRoundTransformer#transform(nurgs.domain.model.game.slot.SlotRound)
     */
    @Override
    public List<String> transform(SlotRound item) {
        Insert insert = QueryBuilder.insertInto("userspinhistory");
        insert.value("userid", item.getPlayerId());
        insert.value("createdate", item.getStart().toInstant(ZoneOffset.UTC).toEpochMilli());
        insert.value("balance", item.getBalance());
        insert.value("bonusbet", item.getBaseSpinResult().getBonusBet());
        insert.value("causality", item.getBaseSpinResult().getCausality());
        insert.value("channel", item.getChannel());
        insert.value("currency", item.getCurrency());
        if(item.getSlotFreeRound() != null) {   
            insert.value("freeroundproviderref", item.getSlotFreeRound().getProvider());
        }
        insert.value("gamehistory", buildGameHistory(item));
        insert.value("gameinfo", item.getGameCode() + ";V:1");
        insert.value("jpcontrib", 0.0);
        insert.value("merchantcode", item.getMerchantCode());
        insert.value("partnerid", item.getPartnerToken());
        if(item.getSlotFreeRound() != null) {
            insert.value("refid", item.getSlotFreeRound().getRefId());
        }
        insert.value("roundid", item.getRoundId());
        insert.value("totalbet", BigDecimal.valueOf(item.getSlotBet().getBetPerLine()).movePointRight(2).longValue());
        insert.value("totalwon", item.getTotalWin());
        insert.value("type", item.getType());
        insert.value("usertype", item.getUserType());
        
        return Arrays.asList(insert.getQueryString());
    }
    
    
    private String buildGameHistory(SlotRound item) {
        JsonObject json = new JsonObject();
        json.put("causality", item.getBaseSpinResult().getCausality());
        json.put("round_id", item.getRoundId());
        json.put("reels", buildReels(item.getBaseSpinResult().getReels()));
        json.putNull("tx_id");
        json.put("total_won", "");
        json.put("total_bet", "");
        json.put("lines", "");
        json.put("per_line_bets", "");
        json.put("line_wins", "");
        json.put("scatters", "");
        json.put("bonuses", "");
        json.put("is_final_pick", false);
        json.put("is_separate", false);
        json.putNull("last_bet_transaction_id");
        json.putNull("raw_result");
        return null;
    }

    private JsonArray buildReels(List<SpinnedReel> reels) {
        JsonArray array = new JsonArray();
        int position = 1;
        for(SpinnedReel sr : reels) {
            JsonObject reelJson = new JsonObject();
            reelJson.put("position", position);
            
            JsonArray symbols = new JsonArray();
            int index = 1;
            for(String sym : sr.getSymbols()) {
                JsonObject symbol = new JsonObject();
                symbol.put("index", index);
                symbol.put("symbol", SYMBOL_MAPPING.get(sym));
                symbol.put("reel_position", 0);
                symbols.add(symbol);
                index++;
            }
            reelJson.put("symbols", symbols);
            reelJson.putNull("key");
            array.add(reelJson);
            position++;
        }
        return array;
    }
    
    
}
