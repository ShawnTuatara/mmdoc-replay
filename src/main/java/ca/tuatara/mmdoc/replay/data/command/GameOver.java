package ca.tuatara.mmdoc.replay.data.command;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@CommandAction({ "GameWon", "GameLost" })
public class GameOver extends Command {
    private static final Logger LOG = LoggerFactory.getLogger(GameOver.class);

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

    @Offset(10)
    private int goldBoost;

    @Offset(15)
    private int player1Elo;

    @Offset(16)
    private int player2Elo;

    @Override
    public void setValues(List<String> values) {
        Class<? extends GameOver> clazz = this.getClass();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            Offset offset = field.getAnnotation(Offset.class);
            if (offset != null) {
                try {
                    Method method = clazz.getMethod("set" + StringUtils.capitalize(field.getName()), field.getType());
                    if (field.getType() == Integer.TYPE) {
                        method.invoke(this, Integer.parseInt(values.get(offset.value())));
                    }
                } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                    LOG.error("Unable to assign game over value to field", e);
                }
            }
        }
    }
}
