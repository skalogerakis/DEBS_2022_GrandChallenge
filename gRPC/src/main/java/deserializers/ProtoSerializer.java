package deserializers;

import grpc.modules.Batch;
import org.apache.kafka.common.serialization.Serializer;


public class ProtoSerializer implements Serializer<Batch> {

    @Override
    public byte[] serialize(String s, Batch batch) {
        return new byte[0];
    }
}
