package ca.tuatara.mmdoc.replay.jackson;

import java.util.List;

import ca.tuatara.jackson.CapitalizeBooleanDeserializer;
import ca.tuatara.mmdoc.replay.data.command.Command;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.Deserializers;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.type.CollectionType;

public class ReplayDeserializers extends Deserializers.Base {

    @Override
    public JsonDeserializer<?> findCollectionDeserializer(CollectionType type, DeserializationConfig config, BeanDescription beanDesc, TypeDeserializer elementTypeDeserializer,
            JsonDeserializer<?> elementDeserializer) throws JsonMappingException {
        if (type.hasRawClass(List.class) && type.getContentType().hasRawClass(Command.class)) {
            return new CommandCollectionDeserializer();
        }

        return null;
    }

    @Override
    public JsonDeserializer<?> findBeanDeserializer(JavaType type, DeserializationConfig config, BeanDescription beanDesc) throws JsonMappingException {
        if (type.isPrimitive() && type.getRawClass().equals(Boolean.TYPE)) {
            return new CapitalizeBooleanDeserializer();
        }

        return null;
    }
}
