package ca.tuatara.mmdoc.replay.data.command;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import ca.tuatara.mmdoc.replay.data.BonusType;
import ca.tuatara.mmdoc.replay.data.command.annotation.CommandAction;
import ca.tuatara.mmdoc.replay.data.command.annotation.Offset;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@CommandAction({ "GameWon", "GameLost" })
public class GameOver extends Command {
    @Offset(0)
    private int xp;

    @Offset(1)
    private int gold;

    @Offset(2)
    private int victoryBonus;

    @Offset(3)
    private int enduranceBonus;

    @Offset(4)
    private int xpBoost;

    @Offset(5)
    private int dmgInflictedBonus;

    @Offset(6)
    private BonusType bonusType;

    @Offset(7)
    private int bonusGold;

    @Offset(10)
    private int goldBoost;

    @Offset(15)
    private int playerElo;

    @Offset(16)
    private int opponentElo;

    private boolean won;
}
