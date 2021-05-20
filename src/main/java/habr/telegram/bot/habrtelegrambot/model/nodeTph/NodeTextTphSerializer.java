package habr.telegram.bot.habrtelegrambot.model.nodeTph;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class NodeTextTphSerializer extends StdSerializer<NodeTextTph> {

    public NodeTextTphSerializer() {
        this(null);
    }

    public NodeTextTphSerializer(Class<NodeTextTph> t) {
        super(t);
    }

    @Override
    public void serialize(NodeTextTph value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeString(value.getContent());
    }
}
