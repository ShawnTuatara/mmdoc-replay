package ca.tuatara.mmdoc.replay;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.tuatara.mmdoc.replay.ui.EloGraph;

public class DirectoryScanner {
    private static final Logger LOG = LoggerFactory.getLogger(DirectoryScanner.class);

    public static void main(String[] args) {
        File replayDirectory = new File(args[0]);
        File[] replayFiles = replayDirectory.listFiles();

        DateFormat replayDateFormat = new SimpleDateFormat("yyyy-MM-dd-hh'h'mm'm'ss's'");

        List<Replay> replays = new ArrayList<Replay>();
        ReplayParser replayParser = new ReplayParser();
        for (File file : replayFiles) {
            try {
                Replay replay = replayParser.parse(file);
                replay.setDatePlayed(replayDateFormat.parse(file.getName()));
                replays.add(replay);
            } catch (IOException e) {
                LOG.error("Unable to parse replay for {}", file, e);
            } catch (ParseException e) {
                LOG.warn("Unable to parse date from file name {}", file.getName(), e);
            }
        }

        EloGraph eloGraph = new EloGraph(replays);
        eloGraph.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        eloGraph.pack();
        eloGraph.setVisible(true);
    }
}
