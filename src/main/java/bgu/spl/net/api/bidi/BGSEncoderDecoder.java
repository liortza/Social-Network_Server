package bgu.spl.net.api.bidi;

import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.api.messages.*;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedList;

public class BGSEncoderDecoder implements MessageEncoderDecoder<Message> {

    private byte[] bytes = new byte[1 << 10]; //start with 1k
    private LinkedList<String> msgInput;
    private int len = 0, byteCounter = 0;
    private short opCode = -1;
    private int connId;

    @Override
    public Message decodeNextByte(byte nextByte) {
        //notice that the top 128 ascii characters have the same representation as their utf-8 counterparts
        //this allow us to do the following comparison
        if (nextByte == ';') { // end of message
            msgInput.clear();
            byteCounter = 0;
            opCode = -1;
            return buildMsg(opCode, msgInput);
        } else { // continue read message
            byteCounter++;
            if (nextByte == '\0') { // next word
                msgInput.add(popString());
            } else {
                pushByte(nextByte);
                if (byteCounter == 2) { // opcode
                    opCode = bytesToShort(bytes);
                    len = 0;
                }
            }
            return null; // not a full message yet
        }
    }

    public short bytesToShort(byte[] byteArr) {
        short result = (short)((byteArr[0] & 0xff) << 8);
        result += (short)(byteArr[1] & 0xff);
        return result;
    }

    @Override
    public byte[] encode(Message message) {
        return message.toBytes(); // only for ACK, ERROR, NOTIFICATION
    }

    private void pushByte(byte nextByte) {
        if (len >= bytes.length) {
            bytes = Arrays.copyOf(bytes, len * 2);
        }

        bytes[len++] = nextByte;
    }

    private String popString() {
        //notice that we explicitly requesting that the string will be decoded from UTF-8
        //this is not actually required as it is the default encoding in java.
        String result = new String(bytes, 0, len, StandardCharsets.UTF_8);
        len = 0;
        return result;
    }

    public BGSEncoderDecoder(int id) {
        this.connId = id;
    }

    // region DECODE
    private Message buildMsg(int opCode, LinkedList<String> msgInput) {
        switch (opCode) {
            case 1: return buildRegister(msgInput);
            case 2: return buildLogin(msgInput);
            case 3: return buildLogout();
            case 4: return buildFollow(msgInput);
            case 5: return buildPost(msgInput);
            case 6: return buildPM(msgInput);
            case 7: return buildLogStat();
            case 8: return buildStat(msgInput);
            default: return buildBlock(msgInput);
        }
    }

    private Message buildRegister(LinkedList<String> msgInput) {
        return new Register(connId, msgInput.get(0), msgInput.get(1), msgInput.get(2));
    }

    private Message buildLogin(LinkedList<String> msgInput) {
        return new Login(connId, msgInput.get(0), msgInput.get(1), msgInput.get(2));
    }

    private Message buildLogout() {
        return new Logout(connId);
    }

    private Message buildFollow(LinkedList<String> msgInput) {
        return new Follow(connId, Integer.parseInt(msgInput.get(0)), msgInput.get(1));
    }

    private Message buildPost(LinkedList<String> msgInput) {
        String content = msgInput.get(0); // TODO: check original content stays ok
        LinkedList<String> taggedUsers = new LinkedList<>();
        int startIndex = content.indexOf('@'), endIndex;
        while (startIndex != -1) {
            content = content.substring(startIndex + 1);
            endIndex = content.indexOf(' ');
            taggedUsers.add(content.substring(0, endIndex));
            content = content.substring(endIndex + 1);
            startIndex = content.indexOf('@');
        }
        return new Post(connId, msgInput.get(0), taggedUsers);
    }

    private Message buildPM(LinkedList<String> msgInput) {
        return new PM(connId, msgInput.get(0), msgInput.get(1), msgInput.get(2));
    }

    private Message buildLogStat() {
        return new LogStat(connId);
    }

    private Message buildStat(LinkedList<String> msgInput) {
        LinkedList<String> usernames = new LinkedList<>();
        String input = msgInput.get(0);
        int endIndex;
        while (!input.isEmpty()) {
            endIndex = input.indexOf('|');
            usernames.add(input.substring(0, endIndex));
            input = input.substring(endIndex + 1);
        }
        return new Stat(connId, usernames);
    }

    private Message buildBlock(LinkedList<String> msgInput) {
        return new Block(connId, msgInput.get(0));
    }
    // endregion
}
