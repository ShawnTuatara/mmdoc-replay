package ca.tuatara.mmdoc.replay.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import ca.tuatara.jackson.CapitalizeBooleanDeserializer;
import ca.tuatara.jackson.CapitalizeNamingStrategy;
import ca.tuatara.mmdoc.replay.data.Replay;
import ca.tuatara.mmdoc.replay.data.command.Command;
import ca.tuatara.mmdoc.replay.data.command.CommandAction;
import ca.tuatara.mmdoc.replay.data.command.GameOver;
import ca.tuatara.mmdoc.replay.data.command.Offset;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

public class ReplayParser {
    private static final Logger LOG = LoggerFactory.getLogger(ReplayParser.class);

    private DateFormat replayDateFormat = new SimpleDateFormat("yyyy-MM-dd-hh'h'mm'm'ss's'");

    private XmlMapper xmlMapper;

    private HashMap<String, Class<? extends Command>> commandActionMap;

    public ReplayParser() {
        JacksonXmlModule module = new JacksonXmlModule();
        module.addDeserializer(Boolean.TYPE, new CapitalizeBooleanDeserializer());
        xmlMapper = new XmlMapper(module);
        xmlMapper.setPropertyNamingStrategy(new CapitalizeNamingStrategy());

        buildCommandActionMap();
    }

    public static void main(String[] args) {
        try {
            URL replayLocation = new URL(args[0]);

            Replay replay = new ReplayParser().parse(replayLocation);
        } catch (IOException e) {
            LOG.error("Unable parse replay", e);
        }
    }

    public Replay parse(URL replayLocation) throws IOException {
        String replayDate = StringUtils.removeEnd(StringUtils.substringAfterLast(replayLocation.getPath(), "/"), ".replay");
        String xml = fixMalformedXml(replayLocation.openStream());
        return createReplay(replayDate, xml);
    }

    public Replay parse(File file) throws IOException {
        String replayDate = StringUtils.removeEnd(file.getName(), ".replay");
        String xml = fixMalformedXml(new FileInputStream(file));
        return createReplay(replayDate, xml);
    }

    private Replay createReplay(String replayDate, String xml) throws IOException {
        ReplayXml replayXml = parseXml(xml);

        Replay replay = new Replay();
        assignReplayDate(replay, replayDate);
        replay.setRandomSeed(replayXml.getRandomSeed());
        replay.setLocalDrawEnabled(replayXml.isEnableLocalDraw());
        replay.setHotSeat(replayXml.isHotSeat());
        replay.setPlayerSeat(replayXml.getOwnerPlayer());
        replay.setPlayerName(replayXml.getOwnerPlayer() == 0 ? replayXml.getNamePlayer1() : replayXml.getNamePlayer2());
        replay.setPlayerElo(replayXml.getOwnerPlayer() == 0 ? replayXml.getEloPlayer1() : replayXml.getEloPlayer2());
        replay.setOpponentName(replayXml.getOwnerPlayer() == 0 ? replayXml.getNamePlayer2() : replayXml.getNamePlayer1());
        replay.setOpponentElo(replayXml.getOwnerPlayer() == 0 ? replayXml.getEloPlayer2() : replayXml.getEloPlayer1());
        replay.setCommands(parseCommands(replayXml.getCommands()));
        LOG.trace("{}", replay);
        return replay;
    }

    private List<Command> parseCommands(String replayCommandsAsString) {
        List<Command> commands = new ArrayList<Command>();

        String[] replayCommands = StringUtils.split(replayCommandsAsString, "\n");
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

        int commandId = Integer.parseInt(splitOnId[0]);
        String commandAction = commandValues[0];
        List<String> values = Arrays.asList(ArrayUtils.remove(commandValues, 0));

        Command command = null;
        try {
            command = commandClass.newInstance();
            command.setId(commandId);
            command.setAction(commandAction);
            if (commandClass.equals(GameOver.class)) {
                GameOver gameOver = (GameOver) command;
                if ("GameWon".equals(commandAction)) {
                    gameOver.setWon(true);
                }
            }
            setValues(command, values, commandClass);
        } catch (InstantiationException | IllegalAccessException e) {
            LOG.error("Unable to instantiate command class", e);
        }

        return command;
    }

    private void setValues(Command command, List<String> values, Class<? extends Command> commandClass) {
        boolean hasCustomValues = false;
        Field[] fields = commandClass.getDeclaredFields();
        for (Field field : fields) {
            Offset offset = field.getAnnotation(Offset.class);
            if (offset != null) {
                try {
                    Class<?> fieldType = field.getType();
                    Method method = commandClass.getMethod("set" + StringUtils.capitalize(field.getName()), fieldType);
                    int value = Integer.parseInt(values.get(offset.value()));
                    if (fieldType == Integer.TYPE) {
                        method.invoke(command, value);
                        hasCustomValues = true;
                    } else if (fieldType.isEnum()) {
                        Method[] enumMethods = fieldType.getMethods();
                        for (Method enumMethod : enumMethods) {
                            if (enumMethod.getAnnotation(JsonCreator.class) != null) {
                                Object enumObject = enumMethod.invoke(null, value);
                                method.invoke(command, enumObject);
                            }
                        }
                    }
                } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                    LOG.error("Unable to assign game over value to field", e);
                }
            }
        }

        if (!hasCustomValues) {
            command.setValues(values);
        }
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

    private void assignReplayDate(Replay replay, String replayDate) {
        try {
            replay.setDatePlayed(replayDateFormat.parse(replayDate));
        } catch (ParseException e) {
            LOG.warn("Unable to parse date from name {}", replayDate, e);
        }
    }

    private ReplayXml parseXml(String replayXml) throws IOException {
        ReplayXml replay = null;
        try {
            replay = xmlMapper.readValue(replayXml, ReplayXml.class);
        } catch (IOException e) {
            LOG.error("Unable to unmarshal XML", e);
            throw e;
        }

        return replay;
    }

    private static String fixMalformedXml(InputStream inputStream) {
        StringBuilder correctedXml = new StringBuilder();

        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));

            correctedXml.append(in.readLine());
            correctedXml.append("<replay>");
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                correctedXml.append(inputLine);
            }
            correctedXml.append("</replay>");
        } catch (IOException e) {
            LOG.error("Unable to read XML from location", e);
        }
        return correctedXml.toString();
    }
}
