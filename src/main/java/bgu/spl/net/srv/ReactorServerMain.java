package bgu.spl.net.srv;

import bgu.spl.net.api.bidi.*;
import bgu.spl.net.api.messages.Message;

public class ReactorServerMain {
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
