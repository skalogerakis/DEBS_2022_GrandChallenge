package deserializers;

import com.google.protobuf.InvalidProtocolBufferException;
import grpc.modules.ResultQ2;
import org.apache.kafka.common.serialization.Deserializer;

public class DeserializerQ2 implements Deserializer<ResultQ2> {

    @Override
    public ResultQ2 deserialize(String s, byte[] bytes) {
        try {
            return ResultQ2.parseFrom(bytes);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        return null;
    }
}
