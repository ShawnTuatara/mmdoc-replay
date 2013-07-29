package ca.tuatara.mmdoc.replay.data.command;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import ca.tuatara.mmdoc.replay.data.CardGroup;
import ca.tuatara.mmdoc.replay.data.command.annotation.CommandAction;
import ca.tuatara.mmdoc.replay.data.command.annotation.Offset;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@CommandAction
public class RevealToOther extends Command {
    @Offset(0)
    private int previousObjectId;

    @Offset(1)
    private int cardId;

    @Offset(2)
    private int objectId;

    @Offset(3)
    private CardGroup cardGroup;
}
