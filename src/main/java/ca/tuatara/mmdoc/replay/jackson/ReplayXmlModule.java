package ca.tuatara.mmdoc.replay.jackson;

import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;

public class ReplayXmlModule extends JacksonXmlModule {
    private static final long serialVersionUID = 1L;

    @Override
    public void setupModule(SetupContext context) {
        context.addDeserializers(new ReplayDeserializers());
        super.setupModule(context);
    }
}
