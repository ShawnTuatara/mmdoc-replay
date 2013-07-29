package ca.tuatara.mmdoc.replay.data.command;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import ca.tuatara.mmdoc.replay.data.command.annotation.CommandAction;
import ca.tuatara.mmdoc.replay.data.command.annotation.Offset;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@CommandAction("NEXTPHASE")
public class NextPhase extends Command {
    @Offset(0)
    private int phaseId;
}
