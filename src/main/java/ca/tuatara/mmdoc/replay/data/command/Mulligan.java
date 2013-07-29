package ca.tuatara.mmdoc.replay.data.command;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import ca.tuatara.mmdoc.replay.data.command.annotation.CommandAction;
import ca.tuatara.mmdoc.replay.data.command.annotation.Offset;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@CommandAction
public class Mulligan extends Command {
    @Offset(0)
    private int field1;

    @Offset(1)
    private int field2;

    @Offset(2)
    private int field3;

    @Offset(3)
    private int field4;
}
