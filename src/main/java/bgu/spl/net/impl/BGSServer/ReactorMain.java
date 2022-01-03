package bgu.spl.net.impl.BGSServer;

import bgu.spl.net.api.bidi.*;
import bgu.spl.net.api.messages.Message;
import bgu.spl.net.srv.Server;

public class ReactorMain {
    public static void main(String[] args) {
        Connections<Message> connections = new ConnectionsImpl();
        Control control = new Control(connections);

        Server.reactor(
                Runtime.getRuntime().availableProcessors(),
                7777, //port
                () -> new BGSProtocol(control), //protocol factory
                BGSEncoderDecoder::new, //message encoder decoder factory
                connections
        ).serve();

    }
}
