package ca.tuatara.mmdoc.replay.jackson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import ca.tuatara.mmdoc.replay.data.command.Command;
import ca.tuatara.mmdoc.replay.data.command.CommandAction;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.std.ContainerDeserializerBase;

public class CommandCollectionDeserializer extends ContainerDeserializerBase<Collection<Command>> {
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(CommandCollectionDeserializer.class);

    private Map<String, Class<? extends Command>> commandActionMap;

    public CommandCollectionDeserializer() {
        super(List.class);
        buildCommandActionMap();
    }

    @Override
    public JavaType getContentType() {
        return null;
    }

    @Override
    public JsonDeserializer<Object> getContentDeserializer() {
        return null;
    }

    @Override
    public Collection<Command> deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        if (jp.getCurrentToken() != JsonToken.VALUE_STRING) {
            LOG.warn("Unable to deserialize as current token was not a string");
            return null;
        }
        Collection<Command> commands = new ArrayList<Command>();

        String[] replayCommands = StringUtils.split(jp.getValueAsString(), "\n");
        for (String replayCommand : replayCommands) {
            commands.add(parseCommand(replayCommand));
        }

        return commands;
    }

    private Command parseCommand(String replayCommand) {
        String[] splitOnId = StringUtils.split(replayCommand, "|");
        String[] commandValues = StringUtils.split(splitOnId[1], " ");

        Class<? extends Command> commandClass = commandActionMap.get(commandValues[0]);
        if (commandClass == null) {
            commandClass = Command.class;
        }

        Command command = null;
        try {
            command = commandClass.newInstance();
            command.setId(Integer.parseInt(splitOnId[0]));
            command.setAction(commandValues[0]);
            command.setValues(Arrays.asList(ArrayUtils.remove(commandValues, 0)));
        } catch (InstantiationException | IllegalAccessException e) {
            LOG.error("Unable to instantiate command class", e);
        }

        return command;
    }

    private void buildCommandActionMap() {
        commandActionMap = new HashMap<String, Class<? extends Command>>();
        ClassPathScanningCandidateComponentProvider classPathScanning = new ClassPathScanningCandidateComponentProvider(false);
        classPathScanning.addIncludeFilter(new AnnotationTypeFilter(CommandAction.class));
        Set<BeanDefinition> commandBeans = classPathScanning.findCandidateComponents("ca/tuatara/mmdoc/replay/data/command");
        for (BeanDefinition commandBean : commandBeans) {
            try {
                Class<?> commandClass = Class.forName(commandBean.getBeanClassName());
                CommandAction commandAction = commandClass.getAnnotation(CommandAction.class);
                if (commandAction != null) {
                    String[] actions = commandAction.value();
                    for (String action : actions) {
                        commandActionMap.put(action, (Class<? extends Command>) commandClass);
                    }
                }
            } catch (ClassNotFoundException e) {
                LOG.error("Unable to find command class", e);
            }
        }
    }
}
