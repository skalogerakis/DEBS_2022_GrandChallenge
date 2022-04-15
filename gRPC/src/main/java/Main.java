import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import com.google.protobuf.Timestamp;
import deserializers.DeserializerQ1;
import deserializers.DeserializerQ2;
import grpc.modules.Benchmark;
import grpc.modules.BenchmarkConfiguration;
import grpc.modules.ChallengerGrpc;
import grpc.modules.Query;
import grpc.modules.ResultQ1;
import grpc.modules.ResultQ2;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;


public class Main {

    public static void main(String[] args) {
        System.out.println("Data ingestion driver v0.7.5");

        int queryPicker = queryMode(args);

        ManagedChannel channel = ManagedChannelBuilder
//                 .forAddress("challenge.msrg.in.tum.de", 5023)
                .forAddress("192.168.1.4", 5023) //in case it is used internally
                .usePlaintext()
                .build();

        ChallengerGrpc.ChallengerBlockingStub challengeClient= ChallengerGrpc.newBlockingStub(channel)
                .withMaxInboundMessageSize(100 * 1024 * 1024)
                .withMaxOutboundMessageSize(100 * 1024 * 1024);

        BenchmarkConfiguration bc = configBuilder(queryPicker);

        //Create a new Benchmark
        Benchmark benchmark = challengeClient.createNewBenchmark(bc);
        System.out.println("Benchmark ID: " + benchmark.getId());

        //Start the benchmark
        challengeClient.startBenchmark(benchmark);
        System.out.println("Benchmark Started");

        Thread object  = new Thread(new IngestWorker("Thread_1",  challengeClient, benchmark, 1));
        Thread object2  = new Thread(new IngestWorker("Thread_2",  challengeClient, benchmark, 2 + queryPicker));

        object.start();
        object2.start();
    }

    public static int queryMode(String[] args){
        try{
            String inputParam = args[0];
            if (inputParam.compareTo("1") == 0 || inputParam.compareTo("2") == 0){
                System.out.println("Execute Benchmark for Query"+args[0]);
                return Integer.parseInt(inputParam);
            } else {
                System.out.println("Execute Both Queries");
                return 0;
            }
        }catch (ArrayIndexOutOfBoundsException AE){
            System.out.println("Executing Both Queries");
            return 0;
        }

    }

    public static BenchmarkConfiguration configBuilder(int mode){

        BenchmarkConfiguration bc = BenchmarkConfiguration.newBuilder()
                .setBenchmarkName("group-14")
                .setToken("qluoyseijckkfdnezbwsqxpynmxxesyj") //go to: https://challenge.msrg.in.tum.de/profile/
                    .setBenchmarkType("evaluation") //Benchmark Type for evaluation
//                .setBenchmarkType("test") //Benchmark Type for testing
                .build();
        if (mode == 0){
            return bc.toBuilder().addQueries(Query.Q1).addQueries(Query.Q2).build();
        }else if(mode == 1) {
            return bc.toBuilder().addQueries(Query.Q1).build();
        }else {
            return bc.toBuilder().addQueries(Query.Q2).build();
        }

    }


}