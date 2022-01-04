package bgu.spl.net.srv;

import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.api.bidi.*;
import bgu.spl.net.api.messages.Message;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;

public class BlockingConnectionHandler<T> implements Runnable, ConnectionHandler<T> {

    private final BidiMessagingProtocol<T> protocol;
    private final MessageEncoderDecoder<T> encdec;
    private final Socket sock;
    private BufferedInputStream in;
    private BufferedOutputStream out;
    private volatile boolean connected = true;
    private final Connections<Message> connections;
    private final int connId;

    public BlockingConnectionHandler(Socket sock, MessageEncoderDecoder<T> reader, BidiMessagingProtocol<T> protocol, Connections<Message> connections, int connId) {
        this.sock = sock;
        this.encdec = reader;
        this.protocol = protocol;
        this.connections = connections;
        this.connId = connId;
    }

    @Override
    public void run() {
        try (Socket sock = this.sock) { //just for automatic closing
            int read;

            in = new BufferedInputStream(sock.getInputStream());
            out = new BufferedOutputStream(sock.getOutputStream());

            ((BGSProtocol) protocol).setHandler((ConnectionHandler<Message>) this);
            protocol.start(connId, (Connections<T>) connections); // todo: casting??
            ((BGSEncoderDecoder) encdec).setConnId(connId); // todo: casting??

            while (!protocol.shouldTerminate() && connected && (read = in.read()) >= 0) {
                System.out.println("reading from client");
                T nextMessage = encdec.decodeNextByte((byte) read);
                if (nextMessage != null) protocol.process(nextMessage);
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    @Override
    public void send(T msg) {
        try {
            if (msg != null) {
                out.write(encdec.encode(msg));
                out.flush();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void close() throws IOException {
        connected = false;
        sock.close();
    }
}
