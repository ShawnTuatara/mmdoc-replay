package ca.tuatara.mmdoc.replay;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.tuatara.jackson.CapitalizeBooleanDeserializer;
import ca.tuatara.jackson.CapitalizeNamingStrategy;

import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

public class ReplayParser {
    private static final Logger LOG = LoggerFactory.getLogger(ReplayParser.class);

    private XmlMapper xmlMapper;

    public ReplayParser() {
        JacksonXmlModule model = new JacksonXmlModule();
        model.addDeserializer(Boolean.TYPE, new CapitalizeBooleanDeserializer(Boolean.TYPE));
        model.addDeserializer(Boolean.class, new CapitalizeBooleanDeserializer(Boolean.class));
        xmlMapper = new XmlMapper(model);
        xmlMapper.setPropertyNamingStrategy(new CapitalizeNamingStrategy());
    }

    public static void main(String[] args) {
        try {
            URL replayLocation = new URL(args[0]);

            new ReplayParser().parse(replayLocation);
        } catch (IOException e) {
            LOG.error("Unable parse replay", e);
        }
    }

    public Replay parse(URL replayLocation) throws IOException {
        return parseXml(fixMalformedXml(replayLocation.openStream()));
    }

    public Replay parse(File file) throws IOException {
        return parseXml(fixMalformedXml(new FileInputStream(file)));
    }

    private Replay parseXml(String replayXml) throws IOException {
        Replay replay = null;
        try {
            replay = xmlMapper.readValue(replayXml, Replay.class);
            LOG.trace("{}", replay);
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
