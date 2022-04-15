import grpc.modules.*;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.LongDeserializer;

import deserializers.DeserializerQ1;
import deserializers.DeserializerQ2;


public class IngestWorker implements Runnable{
    private String name;
    private ChallengerGrpc.ChallengerBlockingStub challengeClient;
    private Benchmark benchmark;
    private String ingestTopicName = "topic";
    private int mode;

    private static AtomicInteger batches_fetched = new AtomicInteger(0);
    private static AtomicBoolean last_bach_fetched = new AtomicBoolean(false);
    private static AtomicInteger reported_q1 = new AtomicInteger(0);
    private static AtomicInteger reported_q2 = new AtomicInteger(0);
    private Properties props;

    public static void propConfig(Properties props){
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "ProtoKafka");
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");   //Assign localhost id
        props.put(ProducerConfig.ACKS_CONFIG, "all");   //Set acknowledgements for producer requests.
        props.put(ProducerConfig.RETRIES_CONFIG, 0);    //If the request fails, the producer can automatically retry,
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384); //Specify buffer size in config
        props.put(ProducerConfig.LINGER_MS_CONFIG, 0);  //Reduce the no of requests less than 0
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 335544320);   //The buffer.memory controls the total amount of memory available to the producer for buffering.
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.LongSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.ByteArraySerializer");
        props.setProperty(ConsumerConfig.GROUP_ID_CONFIG,"grp_id");
        props.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,"earliest");
//        props.setProperty(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true"); 
    }

    public IngestWorker(String name, 
                        ChallengerGrpc.ChallengerBlockingStub challengeClient, 
                        Benchmark benchmark, 
                        int mode){
        this.name = name;
        this.challengeClient = challengeClient;
        this.benchmark = benchmark;
        this.mode = mode;

        props = new Properties();
        propConfig(props);
    }
    

    public void IngestProducer() {
        // Create Kafka producer that will place data fetched from rpc server to a Kafka topic
        Producer<Long, byte[]> producer = new KafkaProducer<Long,byte[]>(props);

        while(true) {
            // throttle data ingestion until the reporter catches up
            // data ingestion should be at max 10 batches ahead in order to reduce latency
            if ( (batches_fetched.get() - reported_q2.get()) > 15) {
                try {
                    Thread.sleep(10);
                } catch (Exception ex) {
                    System.err.println("Exception " + ex.toString());
                }
                continue;
            }
            
            Batch batch = challengeClient.nextBatch(benchmark);
            producer.send(new ProducerRecord<Long, byte[]>(ingestTopicName, batch.getSeqId() ,batch.toByteArray()));
            int curr_count = batches_fetched.incrementAndGet();
            //  System.out.println(new java.util.Date() + " Processed batch ID " + batch.getSeqId() + " (cuur count: " + curr_count + ")");
            
            if (batch.getLast()){
                last_bach_fetched.set(true);
                producer.flush();
                producer.close();
                System.out.println(new java.util.Date() + " Received lastbatch, finished!");
                break;
            }
            
        }
        System.out.println(new java.util.Date() + " Ingest complete @ " + batches_fetched.get() + " with " + last_bach_fetched.get());
    }

    /**
     * Result Consumer that expects results from both queries
     */
    public void ResultConsumer() {
        // Create Kafka consumers that subscribe to topics for Q1 / Q2 results and report back to rpc server
        KafkaConsumer<Long, ResultQ1> consumerQ1 = new KafkaConsumer<Long, ResultQ1>(props, new LongDeserializer(), new DeserializerQ1());
        KafkaConsumer<Long, ResultQ2> consumerQ2 = new KafkaConsumer<Long, ResultQ2>(props, new LongDeserializer(), new DeserializerQ2());

        String topic = "topic";
        consumerQ1.subscribe(Arrays.asList(topic+"Q1"));
        consumerQ2.subscribe(Arrays.asList(topic+"Q2"));

        while(true) {
            // if (last_bach_fetched.get() && (reported_batches.get() >= batches_fetched.get()) ) {
            //     System.out.println(new java.util.Date() + " Reporter complete @ " + reported_batches.get() + " batches");
            //     break;
            // }
            if (last_bach_fetched.get() && 
                (reported_q1.get() >= batches_fetched.get()) && 
                (reported_q2.get() >= batches_fetched.get()) ) {
                
                System.out.println(new java.util.Date() + " Reporter complete @ " + reported_q1.get() + " / " + reported_q2.get() + " batches");
                break;
            }


            ConsumerRecords<Long, ResultQ1> recordsQ1 = consumerQ1.poll(Duration.ofMillis(10));
            ConsumerRecords<Long,ResultQ2> recordsQ2 = consumerQ2.poll(Duration.ofMillis(10));

            for(ConsumerRecord<Long,ResultQ1> record1: recordsQ1){
            //    System.out.println("Key1: "+ record1.key() + ", Value1:" +record1.value().toString());
                ResultQ1 res1Send = record1.value().toBuilder().setBenchmarkId(benchmark.getId()).build();
                challengeClient.resultQ1(res1Send);
                // reported_batches.incrementAndGet();
                reported_q1.incrementAndGet();
                // System.out.println(new java.util.Date() + " reported Q1 for " + record1.value().getBatchSeqId() + " reported count: " + reported_batches.get());
            }

            for(ConsumerRecord<Long,ResultQ2> record2: recordsQ2){
            //    System.out.println("Key2: "+ record2.key() + ", Value2:" +record2.value().toString());
                ResultQ2 res2Send = record2.value().toBuilder().setBenchmarkId(benchmark.getId()).build();
                challengeClient.resultQ2(res2Send);
                reported_q2.incrementAndGet();
            }

            // reported_batches.incrementAndGet();
            // System.out.println("---> reported " + reported_batches.get() + " / " + batches_fetched.get() + " (" + last_bach_fetched.get() + ")");
        }

        challengeClient.endBenchmark(benchmark);
        System.out.println(new java.util.Date() + " Benchmark terminated");
    }

    /**
     * Result Consumer that expects results only from query 1
     */
    public void ResultConsumerQ1() {
        // Create Kafka consumers that subscribe to topics for Q1 / Q2 results and report back to rpc server
        KafkaConsumer<Long, ResultQ1> consumerQ1 = new KafkaConsumer<Long, ResultQ1>(props, new LongDeserializer(), new DeserializerQ1());

        String topic = "topic";
        consumerQ1.subscribe(Arrays.asList(topic+"Q1"));

        while(true) {

            if (last_bach_fetched.get() &&
                    (reported_q2.get() >= batches_fetched.get()) ) {
                System.out.println(new java.util.Date() + " Reporter complete @ " + reported_q2.get() + " batches");
                break;
            }


            ConsumerRecords<Long, ResultQ1> recordsQ1 = consumerQ1.poll(Duration.ofMillis(10));

            for(ConsumerRecord<Long,ResultQ1> record1: recordsQ1){
                //    System.out.println("Key1: "+ record1.key() + ", Value1:" +record1.value().toString());
                ResultQ1 res1Send = record1.value().toBuilder().setBenchmarkId(benchmark.getId()).build();
                challengeClient.resultQ1(res1Send);
                // reported_batches.incrementAndGet();
                reported_q2.incrementAndGet();
                // System.out.println(new java.util.Date() + " reported Q1 for " + record1.value().getBatchSeqId() + " reported count: " + reported_batches.get());
            }

            // System.out.println("---> reported " + reported_batches.get() + " / " + batches_fetched.get() + " (" + last_bach_fetched.get() + ")");
        }

        challengeClient.endBenchmark(benchmark);
        System.out.println(new java.util.Date() + " Benchmark terminated");
    }

    /**
     * Result Consumer2 that expects results only from query 2
     */
    public void ResultConsumerQ2() {
        // Create Kafka consumers that subscribe to topics for Q1 / Q2 results and report back to rpc server
        KafkaConsumer<Long, ResultQ2> consumerQ2 = new KafkaConsumer<Long, ResultQ2>(props, new LongDeserializer(), new DeserializerQ2());

        String topic = "topic";
        consumerQ2.subscribe(Arrays.asList(topic+"Q2"));

        while(true) {
            if (last_bach_fetched.get() &&
                    (reported_q2.get() >= batches_fetched.get()) ) {

                System.out.println(new java.util.Date() + " Reporter complete @ " + reported_q2.get() + " batches");
                break;
            }

            ConsumerRecords<Long,ResultQ2> recordsQ2 = consumerQ2.poll(Duration.ofMillis(10));

            for(ConsumerRecord<Long,ResultQ2> record2: recordsQ2){
                //    System.out.println("Key2: "+ record2.key() + ", Value2:" +record2.value().toString());
                ResultQ2 res2Send = record2.value().toBuilder().setBenchmarkId(benchmark.getId()).build();
                challengeClient.resultQ2(res2Send);
                reported_q2.incrementAndGet();
            }

            // System.out.println("---> reported " + reported_batches.get() + " / " + batches_fetched.get() + " (" + last_bach_fetched.get() + ")");
        }

        challengeClient.endBenchmark(benchmark);
        System.out.println(new java.util.Date() + " Benchmark terminated");
    }

    @Override
    public void run() {
        //Choose the correct mode for the corresponding producer/consumer
        if (mode == 1){
            IngestProducer();
        } else if (mode == 2){
            ResultConsumer();
        } else if (mode == 3){
            ResultConsumerQ1();
        }else if (mode == 4){
            ResultConsumerQ2();
        }
    }


}

