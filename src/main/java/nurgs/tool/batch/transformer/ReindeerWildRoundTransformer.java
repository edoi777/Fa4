/**
 * 
 */
package nurgs.tool.batch.transformer;

import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import nurgs.domain.exception.ValidationException;
import nurgs.domain.model.game.factory.Game;
import nurgs.domain.model.game.slot.SlotRound;
import nurgs.domain.model.game.slot.SlotScatterWin;
import nurgs.domain.model.game.slot.SlotSpinResult;
import nurgs.domain.model.game.slot.feature.SlotFeature;
import nurgs.domain.model.game.slot.feature.SlotFreeSpinFeature;
import nurgs.domain.model.game.slot.savannahking.SavannahKingWildFeature;
import nurgs.domain.repo.slot.GameRepository;
import nurgs.domain.rng.RandomNumberGenerator;
import nurgs.tool.batch.kafka.KafkaSender;

/**
 * @author pau.luna
 */
public class ReindeerWildRoundTransformer extends AbstractSlotRoundTransformer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReindeerWildRoundTransformer.class);
    private static final String USERSPINJSONTEMPLATE = "{" + "\"game_history\" : null," + "\"balance\" : %s,"
            + "\"causality\" : \"%s\"," + "\"channel\" : \"%s\"," + "\"currency\" : \"%s\"," + "\"game_id\" : \"%s\","
            + "\"merchant_code\" : \"%s\"," + "\"partner_id\" : \"%s\"," + "\"round_id\" : \"%s\","
            + "\"timestamp\" : %s," + "\"total_bet\" : %s," + "\"bonus_bet\" : %s," + "\"total_won\" : %s,"
            + "\"user_id\" : \"%s\"," + "\"user_type\" : \"%s\"," + "\"type\" : \"%s\"}";

    private static final String M4_0100 = "M4-0100";
    private static final String REINDEER_WILDS_M4_RECORDER = "REINDEER_WILDS_M4_RECORDER";
    private static final String VALUE = "value";
    private static final String KEY = "key";

    private static final Map<String, Game> GAMES = new HashMap<>();

    private RandomNumberGenerator randomNumberGenerator;
    
    private KafkaSender kafkaSender;

    public ReindeerWildRoundTransformer(GameRepository gameRepo, RandomNumberGenerator randomNumberGenerator, KafkaSender kafkaSender) {
        super(REINDEER_WILDS_M4_RECORDER, M4_0100);
        GAMES.put(REINDEER_WILDS_M4_RECORDER, gameRepo.getGame(REINDEER_WILDS_M4_RECORDER));
        GAMES.put(M4_0100, gameRepo.getGame(M4_0100));
        this.randomNumberGenerator = randomNumberGenerator;
        this.kafkaSender = kafkaSender;
    }

    @Override
    protected JsonArray buildBonusData(List<String> list, SlotRound round) {
        JsonArray bonusDataArray = new JsonArray();
        SlotFreeSpinFeature feature = getFreeSpins(round.getFeatures());
        if (feature != null) {
            buildFreeSpinData(list, bonusDataArray, feature, round);
        }

        return bonusDataArray;
    }

    @Override
    protected JsonObject buildPerLineBets(SlotRound round) {
        return new JsonObject();
    }

    private SlotFreeSpinFeature getFreeSpins(List<SlotFeature> list) {
        if (list != null) {
            for (SlotFeature sf : list) {
                if ("FREE_SPIN".equals(sf.getType())) {
                    return (SlotFreeSpinFeature) sf;
                }
            }
        }
        return null;
    }

    private void buildFreeSpinData(List<String> list, JsonArray bonusDataArray, SlotFreeSpinFeature feature, SlotRound round) {
        boolean hanPending = true;
        while (hanPending) {
            try {
                Game game = GAMES.get(round.getGameCode());
                round.executePendingFeature(game, game.getGameConfig(round.getRtp()), randomNumberGenerator, null);
                SlotSpinResult spr = round.getLastSpinResult();
                String payload = String.format(USERSPINJSONTEMPLATE, 
                        round.getBalance(),
                        spr.getCausality(),
                        round.getChannel(),
                        round.getCurrency(),
                        round.getGameCode() + ";V:1",
                        round.getMerchantCode(),
                        round.getPartnerToken(),
                        round.getRoundId(),
                        spr.getTransactionTime().toInstant(ZoneOffset.UTC).toEpochMilli(),
                        spr.getBetAmount(),
                        spr.getBonusBet(),
                        spr.getWinAmount(),
                        round.getPlayerId(),
                        round.getUserType(),
                        round.getType());
                kafkaSender.send(payload);
                list.add(payload);
            } catch (ValidationException ve) {
                hanPending = false;
            } catch (Exception e) {
                LOGGER.error("Unexpected error occured while executing pending feature for round {}",
                        round.getRoundId(), e);
                return;
            }
        }
        JsonObject spinData = new JsonObject();
        spinData.put("total_won", feature.getTotalFreeSpinWinAmount());
        spinData.put("bonus_name", "Spin Bonus");
        spinData.put("feature_number", 1);
        spinData.put("bonus_retrigger", feature.getAddedSpin() > 0);
        spinData.put("bonus_data", freeSpinBonusData(feature));
        spinData.put("extra_spins_data", buildExtraSpinData(bonusDataArray, round));
        bonusDataArray.add(spinData);

    }

    private JsonArray buildExtraSpinData(JsonArray bonusDataArray, SlotRound round) {
        int spinCounter = 1;
        JsonArray array = new JsonArray();
        for (SlotSpinResult ssr : round.getSpins()) {
            if ("FREE".equalsIgnoreCase(ssr.getType())) {
                JsonObject freeSpinResult = new JsonObject();
                int multiplier = 1;
                for (SlotFeature featureTriggered : ssr.getFeatureTriggered()) {
                    if ("WILD_MULTIPLIER".equalsIgnoreCase(featureTriggered.getType())) {
                        multiplier = ((SavannahKingWildFeature) featureTriggered).getWildMultiplierValue();
                        bonusDataArray.add(buildRandomWildMultiplierBonus(spinCounter, multiplier));
                    }
                }

                freeSpinResult.put("multiplier", multiplier);
                freeSpinResult.put("spin_number", spinCounter);
                freeSpinResult.put("line_wins", buildLineWins(ssr.getReelWins()));

                SlotScatterWin scaterWin = ssr.getScatterWins();
                freeSpinResult.put("scatters", buildScatterWins(scaterWin));
                freeSpinResult.put("reels", buildReels(ssr.getReels()));
                freeSpinResult.put("retrigger", scaterWin != null && (scaterWin.getOfKind() > 0));
                array.add(freeSpinResult);
                spinCounter++;
            }
        }
        return array;
    }

    private JsonObject buildRandomWildMultiplierBonus(int spinNumber, int multiplier) {
        JsonObject json = new JsonObject();
        json.put("total_won", 0);
        json.put("bonus_name", "RandomWildMultiplier");
        json.put("feature_number", 0);
        json.put("bonus_retrigger", false);
        JsonArray bonusData = new JsonArray();
        JsonObject bonusDataEntry = new JsonObject();
        bonusDataEntry.put(KEY, "spin_number");
        bonusDataEntry.put(VALUE, spinNumber);
        bonusData.add(bonusDataEntry);

        bonusDataEntry = new JsonObject();
        bonusDataEntry.put(KEY, "multiplier0");
        bonusDataEntry.put(VALUE, multiplier);
        bonusData.add(bonusDataEntry);

        json.put("bonus_data", bonusData);
        json.put("extra_spins_data", new JsonArray());
        return json;
    }

    private JsonArray freeSpinBonusData(SlotFreeSpinFeature feature) {
        JsonArray array = new JsonArray();

        JsonObject bonusDataEntry = new JsonObject();
        bonusDataEntry.put(KEY, "total_spins");
        bonusDataEntry.put(VALUE, feature.getTotalFreeSpins());
        array.add(bonusDataEntry);

        bonusDataEntry = new JsonObject();
        bonusDataEntry.put(KEY, "re_trigger_amount");
        bonusDataEntry.put(VALUE, feature.getAddedSpin());
        array.add(bonusDataEntry);

        bonusDataEntry = new JsonObject();
        bonusDataEntry.put(KEY, "multiplier");
        bonusDataEntry.put(VALUE, 1);
        array.add(bonusDataEntry);

        bonusDataEntry = new JsonObject();
        bonusDataEntry.put(KEY, "feat_multiplier");
        bonusDataEntry.put(VALUE, 1);
        array.add(bonusDataEntry);

        return array;
    }

}
