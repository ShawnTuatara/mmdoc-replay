package ca.tuatara.mmdoc.replay.data;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum CardGroup {
    Heros(100012), PlayerHand(100002), OpponentHand(100006), BoardEvents(100009);

    private int id;

    private CardGroup(int id) {
        this.id = id;
    }

    @JsonCreator
    public static CardGroup valueOf(int id) {
        CardGroup[] cardGroups = CardGroup.values();
        for (CardGroup cardGroup : cardGroups) {
            if (cardGroup.id == id) {
                return cardGroup;
            }
        }

        return null;
    }
}
