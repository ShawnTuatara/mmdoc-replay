package ca.tuatara.mmdoc.replay.data.command;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import ca.tuatara.mmdoc.replay.data.command.annotation.CommandAction;
import ca.tuatara.mmdoc.replay.data.command.annotation.Offset;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@CommandAction("GENERIC")
public class Generic extends Command {
    @Offset(0)
    private String what;

    @Offset(1)
    private String who;

    @Offset(value = 2, includeRest = true)
    private List<String> whatType;
}
