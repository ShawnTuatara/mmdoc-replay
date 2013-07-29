package ca.tuatara.mmdoc.replay.data;

import java.util.Date;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import ca.tuatara.mmdoc.card.Deck;
import ca.tuatara.mmdoc.card.DeckSummary;
import ca.tuatara.mmdoc.replay.data.command.Command;

@Data
@EqualsAndHashCode(of = { "datePlayed" })
public class Replay {
    private Date datePlayed;

    private int randomSeed;

    private boolean localDrawEnabled;

    private boolean hotSeat;

    private short playerSeat;

    private String playerName;

    private String opponentName;

    private short playerElo;

    private short opponentElo;

    private Deck playerDeck;

    private DeckSummary playerDeckSummary;

    private DeckSummary opponentDeckSummary;

    private List<? extends Command> commands;

    @SuppressWarnings("unchecked")
    public <T extends Command> T getCommand(Class<T> commandClass) {
        for (Command command : commands) {
            if (command.getClass().equals(commandClass)) {
                return (T) command;
            }
        }

        return null;
    }
}
