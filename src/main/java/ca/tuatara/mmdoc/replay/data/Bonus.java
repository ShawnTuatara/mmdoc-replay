package ca.tuatara.mmdoc.replay.data;

import lombok.Data;

@Data
public class Bonus {
    private BonusType type;
    
    private int value;
}
