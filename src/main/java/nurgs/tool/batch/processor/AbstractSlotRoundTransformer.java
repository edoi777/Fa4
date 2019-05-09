/**
 * 
 */
package nurgs.tool.batch.processor;

import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.QueryBuilder;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import nurgs.domain.model.game.slot.SlotPaylineData;
import nurgs.domain.model.game.slot.SlotRound;
import nurgs.domain.model.game.slot.spinner.SpinnedReel;

/**
 * @author pau.luna
 */
public abstract class AbstractSlotRoundTransformer implements SlotRoundTransformer {
    private Set<String> supportedGames = new HashSet<>();

    protected AbstractSlotRoundTransformer(String... supportedGameCodes) {
        supportedGames.addAll(Arrays.asList(supportedGameCodes));
    }

    @Override
    public boolean isSupported(String gameCode) {
        return supportedGames.contains(gameCode);
    }

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
        if (item.getSlotFreeRound() != null) {
            insert.value("freeroundproviderref", item.getSlotFreeRound().getProvider());
        }
        insert.value("gamehistory", buildGameHistory(item));
        insert.value("gameinfo", item.getGameCode() + ";V:1");
        insert.value("jpcontrib", 0.0);
        insert.value("merchantcode", item.getMerchantCode());
        insert.value("partnerid", item.getPartnerToken());
        if (item.getSlotFreeRound() != null) {
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
        json.put("total_won", item.getTotalWin());
        json.put("total_bet", item.getBaseSpinResult().getBetAmount());
        json.put("lines", 1);
        json.put("line_wins", buildLineWins(item.getBaseSpinResult().getReelWins()));
        json.put("per_line_bets", buildPerLineBets(item));
        json.put("scatters", buildScatterWins(item));
        json.put("bonuses", buildBonusData(item));
        json.put("is_final_pick", false);
        json.put("is_separate", false);
        json.putNull("tx_id");
        json.putNull("last_bet_transaction_id");
        json.putNull("raw_result");
        return json.encode();
    }

    private JsonArray buildReels(List<SpinnedReel> reels) {
        JsonArray array = new JsonArray();
        int position = 1;
        for (SpinnedReel sr : reels) {
            JsonObject reelJson = new JsonObject();
            reelJson.put("position", position);

            JsonArray symbols = new JsonArray();
            int index = 1;
            for (String sym : sr.getSymbols()) {
                JsonObject symbol = new JsonObject();
                symbol.put("index", index);
                symbol.put("symbol", getSymbol(sym));
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

    private JsonArray buildLineWins(List<SlotPaylineData> list) {
        JsonArray array = new JsonArray();
        for (SlotPaylineData spld : list) {
            JsonObject lineWin = new JsonObject();
            lineWin.put("kind", spld.getOfKind());
            lineWin.put("symbol", getWinningSymbol(spld.getSymbol()));
            lineWin.put("prize", spld.getPrize());
            lineWin.put("line", spld.getLineNumber());
            lineWin.put("wild_multiplier", spld.getWildMultiplier());
        }
        return array;
    }

    
    protected String getSymbol(String symbol) {
        return symbol;
    }

    protected String getWinningSymbol(String winningSymbol) {
        return winningSymbol;
    }
    
    /**
     * 
     * @param round
     * @return
     */
    protected abstract JsonArray buildBonusData(SlotRound round);
    
    /**
     * 
     * @param round
     * @return
     */
    protected abstract JsonObject buildPerLineBets(SlotRound round);
    
    /**
     * 
     * @param round
     * @return
     */
    protected abstract JsonArray buildScatterWins(SlotRound round);
    
}
