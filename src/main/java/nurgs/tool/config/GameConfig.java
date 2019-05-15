package nurgs.tool.config;

import java.io.InputStream;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

import nurgs.domain.model.game.factory.GameConfigCreator;
import nurgs.domain.model.game.slot.evaluator.AllPaysEvaluator;
import nurgs.domain.model.game.slot.evaluator.LinePaysEvaluator;
import nurgs.domain.model.game.slot.evaluator.LinePaysWithAnySymbolEvaluator;
import nurgs.domain.model.game.slot.fafafa.FafafaGame;
import nurgs.domain.model.game.slot.fafafaXL.FafafaXLGame;
import nurgs.domain.model.game.slot.filter.SlotFilter;
import nurgs.domain.model.game.slot.ganeshblessing.GaneshEvaluator;
import nurgs.domain.model.game.slot.reindeerwildwins.ReindeerWildWinsGame;
import nurgs.domain.model.game.slot.reindeerwildwinsxl.ReindeerWildWinsXLGame;
import nurgs.domain.model.game.slot.savannahking.SavannahKingResultFilter;
import nurgs.domain.model.game.slot.spinner.SlotGameSpinner;
import nurgs.domain.model.game.slot.spinner.SlotGameSpinnerForFaFaFa;
import nurgs.domain.model.game.slot.spinner.SlotSpinner;
import nurgs.domain.repo.slot.GameRepository;
import nurgs.domain.repo.slot.GameRepositoryImpl;
import nurgs.domain.rng.RandomNumberGenerator;
import nurgs.tool.batch.kafka.KafkaSender;
import nurgs.tool.batch.transformer.FafafaRoundTransformer;
import nurgs.tool.batch.transformer.ReindeerWildRoundTransformer;
import nurgs.tool.batch.transformer.RoundConverterProvider;

@Configuration
public class GameConfig {

    @Bean
    public GameConfigCreator gameConfigCreator() {
        return new GameConfigCreator()
        {
            @Override
            protected InputStream getConfigAsInputStream(String rtp, String config, String gameCode) {
                return GameConfigCreator.class.getResourceAsStream("/games/standard" + "/" + gameCode + "/" + rtp + "/" + config);
            }
        };
    }
    
    @Bean
    public GameRepository gameFactoryRepository() {
        return new GameRepositoryImpl();
    }
    
    @Bean
    public FafafaGame fafafaGame(SlotSpinner slotGameSpinnerForFaFaFa, LinePaysEvaluator linePaysWithAnySymbolEvaluator,
            GameConfigCreator gameConfigCreator) {
        FafafaGame fafafaGame = new FafafaGame();
        fafafaGame.setSlotSpinner(slotGameSpinnerForFaFaFa);
        fafafaGame.setSlotEvaluator(linePaysWithAnySymbolEvaluator);
        fafafaGame.setGameConfigCreator(gameConfigCreator);
        return fafafaGame;
    }
    
    @Bean
    public FafafaXLGame fafafaXLGame(SlotSpinner slotGameSpinnerForFaFaFa, LinePaysEvaluator linePaysWithAnySymbolEvaluator,
            GameConfigCreator gameConfigCreator) {
        FafafaXLGame fafafaXLGame = new FafafaXLGame();
        fafafaXLGame.setSlotSpinner(slotGameSpinnerForFaFaFa);
        fafafaXLGame.setSlotEvaluator(linePaysWithAnySymbolEvaluator);
        fafafaXLGame.setGameConfigCreator(gameConfigCreator);
        return fafafaXLGame;
    }
    
    @Bean
    public ReindeerWildWinsGame reindeerWildWinsGame(SlotSpinner slotGameSpinner,
            AllPaysEvaluator allPaysEvaluator,
            SlotFilter savannahKingResultFilter,
            GameConfigCreator gameConfigCreator) {
        ReindeerWildWinsGame reindeerWildWinsGame = new ReindeerWildWinsGame();
        reindeerWildWinsGame.setSlotSpinner(slotGameSpinner);
        reindeerWildWinsGame.setSlotEvaluator(allPaysEvaluator);
        reindeerWildWinsGame.setResultFilter(savannahKingResultFilter);
        reindeerWildWinsGame.setGameConfigCreator(gameConfigCreator);
        return reindeerWildWinsGame;
    }
    
    @Bean
    public ReindeerWildWinsXLGame reindeerWildWinsXLGame(SlotSpinner slotGameSpinner,
            AllPaysEvaluator allPaysEvaluator,
            SlotFilter savannahKingResultFilter,
            GameConfigCreator gameConfigCreator) {
        ReindeerWildWinsXLGame reindeerWildWinsXLGame = new ReindeerWildWinsXLGame();
        reindeerWildWinsXLGame.setSlotSpinner(slotGameSpinner);
        reindeerWildWinsXLGame.setSlotEvaluator(allPaysEvaluator);
        reindeerWildWinsXLGame.setResultFilter(savannahKingResultFilter);
        reindeerWildWinsXLGame.setGameConfigCreator(gameConfigCreator);
        return reindeerWildWinsXLGame;
    }


    @Bean
    public RandomNumberGenerator randomNumberGenerator() {
        return new RandomNumberGenerator();
    }
    

    @Bean
    public AllPaysEvaluator allPaysEvaluator() {
        return new AllPaysEvaluator();
    }

    @Bean
    public LinePaysEvaluator linePaysEvaluator() {
        return new LinePaysEvaluator();
    }

    @Bean
    public LinePaysWithAnySymbolEvaluator linePaysWithAnySymbolEvaluator() {
        return new LinePaysWithAnySymbolEvaluator();
    }

    @Bean
    public GaneshEvaluator ganeshEvaluator() {
        return new GaneshEvaluator();
    }
    
    @Bean
    public SlotGameSpinner slotGameSpinner() {
        return new SlotGameSpinner();
    }

    @Bean
    public SlotGameSpinnerForFaFaFa slotGameSpinnerForFaFaFa() {
        return new SlotGameSpinnerForFaFaFa();
    }

    @Bean
    public SavannahKingResultFilter savannahKingResultFilter() {
        return new SavannahKingResultFilter();
    }
    
    
    
    @Bean
    public RoundConverterProvider provider(FafafaRoundTransformer fafafaRoundTransformer, ReindeerWildRoundTransformer reindeerWildRoundTransformer) {
        RoundConverterProvider provider = new RoundConverterProvider();
        provider.add(fafafaRoundTransformer);
        provider.add(reindeerWildRoundTransformer);
        return provider;
    }
    
    @Bean
    public FafafaRoundTransformer fafafaRoundTransformer() {
        return new FafafaRoundTransformer();
    }
    
    @Bean
    public ReindeerWildRoundTransformer reindeerWildRoundTransformer(GameRepository gameRepo, RandomNumberGenerator randomNumberGenerator, KafkaSender kafkaSender) {
        return new ReindeerWildRoundTransformer(gameRepo, randomNumberGenerator, kafkaSender);
    }
    
    @Bean
    public KafkaSender kafkaSender(KafkaTemplate<String, String> kafkaTemplate) {
        return new KafkaSender(kafkaTemplate);
    }
}
