package ca.tuatara.mmdoc.replay.parser;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@JsonAutoDetect
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReplayXml {
    private int randomSeed;

    private boolean enableLocalDraw;

    private boolean hotSeat;

    private short ownerPlayer;

    private String namePlayer1;

    private String namePlayer2;

    private short eloPlayer1;

    private short eloPlayer2;

    private String deckPlayer1;

    private String deckPlayer2;

    @JsonProperty("ReplayCommandList")
    private String commands;
}
