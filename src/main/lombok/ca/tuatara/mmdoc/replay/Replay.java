package ca.tuatara.mmdoc.replay;

import java.util.Date;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

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

    public short getOwnerElo() {
        return ownerPlayer == 0 ? eloPlayer1 : eloPlayer2;
    }
}
