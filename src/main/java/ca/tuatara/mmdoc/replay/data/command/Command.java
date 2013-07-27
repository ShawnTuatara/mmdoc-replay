package ca.tuatara.mmdoc.replay.data.command;

import java.util.List;

import lombok.Data;

@Data
public class Command {
    private int id;

    private String action;

    private List<String> values;
}
