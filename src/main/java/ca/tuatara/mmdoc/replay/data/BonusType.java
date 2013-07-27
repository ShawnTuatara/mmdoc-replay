package ca.tuatara.mmdoc.replay.data;

public enum BonusType {
    CRITICAL_STRIKE(1), HEAVY_ONSLAUGHT(2);

    private int type;

    private BonusType(int type) {
        this.type = type;
    }

    public static BonusType valueOf(int type) {
        BonusType[] values = BonusType.values();
        for (BonusType bonusType : values) {
            if (bonusType.type == type) {
                return bonusType;
            }
        }

        return null;
    }
}
