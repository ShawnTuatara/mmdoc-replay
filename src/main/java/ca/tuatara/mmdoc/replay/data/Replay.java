package ca.tuatara.mmdoc.replay.data;

import java.util.Date;
import java.util.List;

import lombok.Data;
import ca.tuatara.mmdoc.replay.data.command.Command;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@JsonAutoDetect
@JsonIgnoreProperties(ignoreUnknown = true)
public class Replay {
    private Date datePlayed;

    private int randomSeed;

    private boolean enableLocalDraw;

    private boolean hotSeat;

    private short ownerPlayer;

    private String namePlayer1;

    private String namePlayer2;

    private short eloPlayer1;

    private short eloPlayer2;

    @JsonProperty("ReplayCommandList")
    private List<? extends Command> commands;

    public short getOwnerElo() {
        return ownerPlayer == 0 ? eloPlayer1 : eloPlayer2;
    }

    public <T extends Command> T getCommand(Class<T> commandClass) {
        for (Command command : commands) {
            if (command.getClass().equals(commandClass)) {
                return (T) command;
            }
        }

        return null;
    }
}
