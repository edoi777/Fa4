package nurgs.tool.config;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemStreamWriter;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.MongoItemReader;
import org.springframework.batch.item.data.builder.MongoItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.support.CompositeItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.datastax.driver.core.Session;

import nurgs.domain.model.game.slot.SlotRound;
import nurgs.tool.batch.partitioner.PagePartitioner;
import nurgs.tool.batch.processor.SlotRoundProcessor;
import nurgs.tool.batch.transformer.RoundConverterProvider;

@Configuration
@Import({GameConfig.class})
@EnableBatchProcessing
public class BatchConfig extends DefaultBatchConfigurer {

    private static final Logger LOGGER = LoggerFactory.getLogger(BatchConfig.class);
    
    @Autowired
    private StepBuilderFactory steps;

    @Value("${partnerCode}")
    private String partnerCode;

    @Value("${gameCode}")
    private String gameCode;

    @Value("${outputFolder:output}")
    private String outputFolder;

    @Value("${pageSize:50}")
    private int pageSize;

    @Value("${enableWriteToCassandra:false}")
    private boolean writeToCassandra;
    
    @Bean
    public JobExecutionListener jobExecutionListener() {
        return new JobExecutionListener() {
            @Override
            public void beforeJob(JobExecution jobExecution) {
                LOGGER.info("Batch application has started at {}" , jobExecution.getStartTime());
            }

            @Override
            public void afterJob(JobExecution jobExecution) {
                jobExecution.stop();
                LOGGER.info("Batch Status : {}", jobExecution.getStatus());
                LOGGER.info("Batch Exit Status : {}", jobExecution.getExitStatus());
                LOGGER.info("Batch stopping : {}", jobExecution.isStopping());
                LOGGER.info("Batch application has ended at {}" , jobExecution.getEndTime());
                System.exit(-1);
            }
        };
    }

    @Bean(name = "partitionerJob")
    public Job partitionerJob(JobBuilderFactory jobs, Step partitionStep) {
        return jobs.get("partitionerJob")
                .listener(jobExecutionListener())
                .start(partitionStep).build();
    }

    @Bean
    public Step partitionStep(@Value("${threadCount:10}") Integer gridSize, PagePartitioner partitioner) {
        return steps.get("partitionStep")
                .partitioner("slaveStep", partitioner)
                .gridSize(gridSize)
                .step(slaveStep())
                .taskExecutor(taskExecutor()).build();
    }

    @Bean
    public Step slaveStep() {
        //@formatter:off
        return steps.get("slaveStep")
                .<SlotRound, List<String>> chunk(1)
                .reader(itemReader(null, null, null))
                .processor(slotRoundProcessor(null))
                .writer(itemWriter(null, null))
                .build();
        //@formatter:on
    }

    @Bean
    public PagePartitioner partitioner(@Value("${from:0L}") Long fromTime,  @Value("${until:0L}") Long untilTime) {
        return new PagePartitioner(fromTime, untilTime);
    }

    @Bean
    public TaskExecutor taskExecutor() {
        return new SimpleAsyncTaskExecutor();
    }

    @Bean
    @StepScope
    public MongoItemReader<SlotRound> itemReader(
            @Value("#{stepExecutionContext[start]}") Long start,
            @Value("#{stepExecutionContext[end]}") Long end,
            MongoTemplate mongoTemplate) {
        MongoItemReaderBuilder<SlotRound> itemReaderBuilder = new MongoItemReaderBuilder<>();
        
        Map<String,Direction> sorts = new HashMap<>();
        sorts.put("start", Direction.ASC);
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("{\"partnerCode\":\"");
        queryBuilder.append(partnerCode);
        queryBuilder.append("\",");
        queryBuilder.append("\"gameCode\":\"");
        queryBuilder.append(gameCode);
        queryBuilder.append("\",");
        queryBuilder.append("\"start\":{\"$gte\":ISODate(\"");
        queryBuilder.append(Instant.ofEpochMilli(start));
        queryBuilder.append("\"),");
        queryBuilder.append("\"$lte\":ISODate(\"");
        queryBuilder.append(Instant.ofEpochMilli(end));
        queryBuilder.append("\")}}");
        return itemReaderBuilder
                .jsonQuery(queryBuilder.toString())
                .pageSize(pageSize)
                .saveState(false)
                .targetType(SlotRound.class)
                .sorts(sorts)
                .template(mongoTemplate)
                .build();
    }

    @Bean
    @StepScope
    public SlotRoundProcessor slotRoundProcessor(RoundConverterProvider provider) {
        return new SlotRoundProcessor(provider);
    }
    
    
    @Bean
    @StepScope
    public ItemStreamWriter<List<String>> itemWriter(@Qualifier("logWriter") ItemWriter<List<String>> logWriter,
            @Qualifier("cassandraWriter") ItemWriter<List<String>> cassandraWriter) {
        CompositeItemWriter<List<String>> compositeWriter = new CompositeItemWriter<>();
        compositeWriter.setDelegates(Arrays.asList(logWriter, cassandraWriter));
        return compositeWriter;
    }

    @Bean("logWriter")
    @StepScope
    public FlatFileItemWriter<List<String>> logWriter(@Value("#{stepExecutionContext[name]}") String filename) {
        char endingChar = outputFolder.charAt(outputFolder.length() - 1);
        String output = outputFolder;
        if(endingChar == '/' || endingChar == '\\') {
            output = output.substring(0, output.length() - 1);
        }
        //Create writer instance
        FlatFileItemWriter<List<String>> writer = new FlatFileItemWriter<>();
        //Set output file location
        writer.setResource(new FileSystemResource(output + "/" + partnerCode + "/" + gameCode + "/" +filename));
        writer.setSaveState(false);
        writer.setLineAggregator(items -> {
            StringBuilder sb = new StringBuilder();
            boolean isFirst = true;
            for(String item : items) {
                if(!isFirst) {
                    sb.append("\n");
                }
                isFirst = false;
                sb.append(item);
            }
            return sb.toString();
        });
        return writer;
    }
    
    
    @Bean("cassandraWriter")
    @StepScope
    public ItemWriter<List<String>> cassandraWriter(Session session) {
        return items -> {
            if(writeToCassandra) {
                for(List<String> strItems : items) {
                    write(strItems, session);
                }
            }
        };
    }
    
    private void write(List<String> strItems, Session session) {
        for(String item : strItems) {
            try {
                if(item.startsWith("INSERT")) {
                    session.execute(item);   
                }
            }catch(Exception e) {
                LOGGER.error("Failed to insert : {}", item);
            }
        }
    }

}
