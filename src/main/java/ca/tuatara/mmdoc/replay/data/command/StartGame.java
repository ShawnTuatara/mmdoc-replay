package ca.tuatara.mmdoc.replay.data.command;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import ca.tuatara.mmdoc.replay.data.command.annotation.CommandAction;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@CommandAction
public class StartGame extends Command {

}
