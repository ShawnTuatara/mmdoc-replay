package ca.tuatara.mmdoc.replay;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.tuatara.mmdoc.replay.data.Replay;
import ca.tuatara.mmdoc.replay.parser.ReplayParser;
import ca.tuatara.mmdoc.replay.ui.EloGraph;

public class DirectoryScanner {
    private static final Logger LOG = LoggerFactory.getLogger(DirectoryScanner.class);

    public static void main(String[] args) {
        File replayDirectory = new File(args[0]);
        File[] replayFiles = replayDirectory.listFiles();

        List<Replay> replays = new ArrayList<Replay>();
        ReplayParser replayParser = new ReplayParser();
        for (File file : replayFiles) {
            try {
                Replay replay = replayParser.parse(file);
                replays.add(replay);
            } catch (IOException e) {
                LOG.error("Unable to parse replay for {}", file, e);
            }
        }

        if ("graph".equals(args[1])) {
            EloGraph eloGraph = new EloGraph(replays);
            eloGraph.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            eloGraph.pack();
            eloGraph.setVisible(true);
        }

        if ("excel".equals(args[1])) {
            ReplaySpreadsheet replaySpreadsheet = new ReplaySpreadsheet(replays);
            try {
                OutputStream outputStream = new FileOutputStream("replays.xlsx");
                replaySpreadsheet.write(outputStream);
                outputStream.close();
            } catch (IOException e) {
                LOG.error("Problem writing excel workbook", e);
            }
        }
    }
}
