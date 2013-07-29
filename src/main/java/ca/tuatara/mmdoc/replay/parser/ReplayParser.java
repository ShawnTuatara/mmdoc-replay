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
import ca.tuatara.mmdoc.card.CardManager;
import ca.tuatara.mmdoc.card.Deck;
import ca.tuatara.mmdoc.card.DeckSummary;
import ca.tuatara.mmdoc.card.DirectoryScanner;
import ca.tuatara.mmdoc.card.data.Card;
import ca.tuatara.mmdoc.card.data.CardType;
import ca.tuatara.mmdoc.replay.data.Replay;
import ca.tuatara.mmdoc.replay.data.command.Command;
import ca.tuatara.mmdoc.replay.data.command.GameOver;
import ca.tuatara.mmdoc.replay.data.command.annotation.CommandAction;
import ca.tuatara.mmdoc.replay.data.command.annotation.Offset;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

public class ReplayParser {
    private static final Logger LOG = LoggerFactory.getLogger(ReplayParser.class);

    private DateFormat replayDateFormat = new SimpleDateFormat("yyyy-MM-dd-hh'h'mm'm'ss's'");

    private XmlMapper xmlMapper;

    private HashMap<String, Class<? extends Command>> commandActionMap;

    private CardManager cardManager;

    public ReplayParser(CardManager cardManager) {
        this.cardManager = cardManager;

        JacksonXmlModule module = new JacksonXmlModule();
        module.addDeserializer(Boolean.TYPE, new CapitalizeBooleanDeserializer());
        xmlMapper = new XmlMapper(module);
        xmlMapper.setPropertyNamingStrategy(new CapitalizeNamingStrategy());

        buildCommandActionMap();
    }

    public static void main(String[] args) {
        try {
            URL replayLocation = new URL(args[0]);

            DirectoryScanner directoryScanner = new DirectoryScanner(args[1]);
            CardManager manager = new CardManager();
            manager.addCards(directoryScanner.loadCards());

            Replay replay = new ReplayParser(manager).parse(replayLocation);

            List<? extends Command> commands = replay.getCommands();
            for (Command command : commands) {
                LOG.debug("{}", command);
            }

            LOG.debug("{}", replay);
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

        String playerDeck = replayXml.getOwnerPlayer() == 0 ? replayXml.getDeckPlayer1() : replayXml.getDeckPlayer2();
        if (playerDeck.contains(CardType.Creature.name())) {
            replay.setPlayerDeckSummary(parseDeckSummary(playerDeck));
        } else {
            replay.setPlayerDeck(parseDeck(playerDeck));
        }
        replay.setOpponentDeckSummary(parseDeckSummary(replayXml.getOwnerPlayer() == 0 ? replayXml.getDeckPlayer2() : replayXml.getDeckPlayer1()));

        replay.setCommands(parseCommands(replayXml.getCommands()));
        LOG.trace("{}", replay);
        return replay;
    }

    private DeckSummary parseDeckSummary(String deckSummaryInput) {
        DeckSummary deckSummary = new DeckSummary();
        String[] summaryElements = deckSummaryInput.split("\n");
        String cardId = summaryElements[0].split("|")[1];
        deckSummary.setHero(cardManager.getCardById(Integer.parseInt(cardId)));
        for (int elementIndex = 1; elementIndex < summaryElements.length; elementIndex++) {
            String[] elementDetails = StringUtils.trimToEmpty(summaryElements[elementIndex]).split(",");

            String cardTypeString = StringUtils.removeEnd(elementDetails[1], "Card");
            CardType cardType = CardType.valueOf(cardTypeString);
            switch (cardType) {
            case Creature:
                deckSummary.setCreatureCount(Integer.parseInt(elementDetails[0]));
                break;
            case Event:
                deckSummary.setEventCount(Integer.parseInt(elementDetails[0]));
                break;
            case Spell:
                deckSummary.setSpellCount(Integer.parseInt(elementDetails[0]));
                break;
            case Fortune:
                deckSummary.setFortuneCount(Integer.parseInt(elementDetails[0]));
                break;
            case Hero:
            }

        }
        return deckSummary;
    }

    private Deck parseDeck(String deckInput) {
        Deck deck = new Deck();

        String[] cards = deckInput.split("\n");
        deck.setHero(cardManager.getCardById(Integer.parseInt(StringUtils.trimToEmpty(cards[0]).split(",")[1])));
        cards = ArrayUtils.remove(cards, 0);
        for (String card : cards) {
            String[] cardDetails = StringUtils.trimToEmpty(card).split(",");
            Card cardById = cardManager.getCardById(Integer.parseInt(cardDetails[1]));
            int numberOfCardsById = Integer.parseInt(cardDetails[0]);
            for (int cardCount = 0; cardCount < numberOfCardsById; cardCount++) {
                deck.addCard(cardById);
            }
        }

        return deck;
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
                    String value = values.get(offset.value());
                    if (fieldType == Integer.TYPE) {
                        method.invoke(command, Integer.parseInt(value));
                    } else if (fieldType == String.class) {
                        method.invoke(command, value);
                    } else if (fieldType.isEnum()) {
                        Method[] enumMethods = fieldType.getMethods();
                        for (Method enumMethod : enumMethods) {
                            if (enumMethod.getAnnotation(JsonCreator.class) != null) {
                                Object enumObject = enumMethod.invoke(null, Integer.parseInt(value));
                                method.invoke(command, enumObject);
                            }
                        }
                    } else if (offset.includeRest() && fieldType.isAssignableFrom(List.class)) {
                        ArrayList<String> remainingValues = new ArrayList<String>();
                        for (int valueIndex = offset.value(); valueIndex < values.size(); valueIndex++) {
                            remainingValues.add(values.get(valueIndex));
                        }
                        method.invoke(command, remainingValues);
                    }
                    hasCustomValues = true;
                } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                    LOG.error("Unable to assign value to field", e);
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
                Class<?> beanClass = Class.forName(commandBean.getBeanClassName());
                if (Command.class.isAssignableFrom(beanClass)) {
                    Class<? extends Command> commandClass = (Class<? extends Command>) beanClass;
                    CommandAction commandAction = commandClass.getAnnotation(CommandAction.class);
                    if (commandAction != null) {
                        String[] actions = commandAction.value();
                        if (StringUtils.isEmpty(actions[0])) {
                            commandActionMap.put(commandClass.getSimpleName(), commandClass);
                        } else {
                            for (String action : actions) {
                                commandActionMap.put(action, commandClass);
                            }
                        }
                    }
                } else {
                    LOG.warn("Found @CommandAction class that doesn't extend Command. [{}]", beanClass.getName());
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
