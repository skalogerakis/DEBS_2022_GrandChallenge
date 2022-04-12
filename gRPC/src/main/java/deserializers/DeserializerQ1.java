package deserializers;

import com.google.protobuf.InvalidProtocolBufferException;
import grpc.modules.Batch;
import grpc.modules.ResultQ1;
import org.apache.kafka.common.serialization.Deserializer;

public class DeserializerQ1 implements Deserializer<ResultQ1> {
    @Override
    public ResultQ1 deserialize(String s, byte[] bytes) {
        try {
            return ResultQ1.parseFrom(bytes);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        return null;
    }
}
