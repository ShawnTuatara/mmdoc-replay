package ca.tuatara.mmdoc.replay.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.tuatara.jackson.CapitalizeNamingStrategy;
import ca.tuatara.mmdoc.replay.data.Replay;
import ca.tuatara.mmdoc.replay.data.command.Command;
import ca.tuatara.mmdoc.replay.jackson.ReplayXmlModule;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;

public class ReplayParser {
    private static final Logger LOG = LoggerFactory.getLogger(ReplayParser.class);

    private DateFormat replayDateFormat = new SimpleDateFormat("yyyy-MM-dd-hh'h'mm'm'ss's'");

    private XmlMapper xmlMapper;

    public ReplayParser() {
        xmlMapper = new XmlMapper(new ReplayXmlModule());
        xmlMapper.setPropertyNamingStrategy(new CapitalizeNamingStrategy());
    }

    public static void main(String[] args) {
        try {
            URL replayLocation = new URL(args[0]);

            Replay replay = new ReplayParser().parse(replayLocation);
            for (Command command : replay.getCommands()) {
                LOG.trace("{}", command);
            }
        } catch (IOException e) {
            LOG.error("Unable parse replay", e);
        }
    }

    public Replay parse(URL replayLocation) throws IOException {
        Replay replay = parseXml(fixMalformedXml(replayLocation.openStream()));
        String replayDate = StringUtils.removeEnd(StringUtils.substringAfterLast(replayLocation.getPath(), "/"), ".replay");
        assignReplayDate(replay, replayDate);
        LOG.trace("{}", replay);
        return replay;
    }

    public Replay parse(File file) throws IOException {
        Replay replay = parseXml(fixMalformedXml(new FileInputStream(file)));
        String replayDate = StringUtils.removeEnd(file.getName(), ".replay");
        assignReplayDate(replay, replayDate);
        LOG.trace("{}", replay);
        return replay;
    }

    private void assignReplayDate(Replay replay, String replayDate) {
        try {
            replay.setDatePlayed(replayDateFormat.parse(replayDate));
        } catch (ParseException e) {
            LOG.warn("Unable to parse date from name {}", replayDate, e);
        }
    }

    private Replay parseXml(String replayXml) throws IOException {
        Replay replay = null;
        try {
            replay = xmlMapper.readValue(replayXml, Replay.class);
        } catch (IOException e) {
            LOG.error("Unable to unmarshal XML", e);
            throw e;
        }

        return replay;
    }

    private static String fixMalformedXml(InputStream inputStream) {
        StringBuilder correctedXml = new StringBuilder();

        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));

            correctedXml.append(in.readLine());
            correctedXml.append("<replay>");
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                correctedXml.append(inputLine);
            }
            correctedXml.append("</replay>");
        } catch (IOException e) {
            LOG.error("Unable to read XML from location", e);
        }
        return correctedXml.toString();
    }
}
