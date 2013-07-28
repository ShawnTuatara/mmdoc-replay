package ca.tuatara.mmdoc.replay.data;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum BonusType {
    CRITICAL_STRIKE(1), HEAVY_ONSLAUGHT(2), OVERWHELMING_FORCES(3), BONUS_4(4);

    private int type;

    private BonusType(int type) {
        this.type = type;
    }

    @JsonCreator
    public static BonusType forValue(int type) {
        BonusType[] values = BonusType.values();
        for (BonusType bonusType : values) {
            if (bonusType.type == type) {
                return bonusType;
            }
        }

        return null;
    }
}
