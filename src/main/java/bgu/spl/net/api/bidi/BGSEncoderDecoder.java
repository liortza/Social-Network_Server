package bgu.spl.net.api.bidi;

import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.api.messages.*;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedList;

public class BGSEncoderDecoder implements MessageEncoderDecoder<Message> {

    private byte[] bytes = new byte[1 << 10]; //start with 1k
    private String[] msgInput;
    private int len = 0;
    private short opCode;
    private int connId;

    @Override
    public Message decodeNextByte(byte nextByte) {
        //notice that the top 128 ascii characters have the same representation as their utf-8 counterparts
        //this allow us to do the following comparison
        if (nextByte == ';') { // end of message
            String popString = popString();
            processPopString(popString);
            return buildMsg();
        } // continue read message
        pushByte(nextByte);
        if (len == 2) opCode = bytesToShort(bytes); // opcode
        return null; // not a full message yet
    }

    private void processPopString(String popString) {
        popString = popString.substring(2);
        msgInput = popString.split("\0");
    }

    public short bytesToShort(byte[] byteArr) {
        short result = (short) ((byteArr[0] & 0xff) << 8);
        result += (short) (byteArr[1] & 0xff);
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

    public void setConnId(int connId) {
        this.connId = connId;
    }

    // region DECODE
    private Message buildMsg() {
        switch (opCode) {
            case 1:
                return buildRegister();
            case 2:
                return buildLogin();
            case 3:
                return buildLogout();
            case 4:
                return buildFollow();
            case 5:
                return buildPost();
            case 6:
                return buildPM();
            case 7:
                return buildLogStat();
            case 8:
                return buildStat();
            default:
                return buildBlock();
        }
    }

    private Message buildRegister() {
        if (msgInput.length != 3) return null;
        return new Register(connId, msgInput[0], msgInput[1], msgInput[2]);
    }

    private Message buildLogin() {
        if (msgInput.length != 3) return null;
        return new Login(connId, msgInput[0], msgInput[1], msgInput[2]);
    }

    private Message buildLogout() {
        return new Logout(connId);
    }

    private Message buildFollow() {
        if (msgInput.length != 2) return null;
        return new Follow(connId, Integer.parseInt(msgInput[0]), msgInput[1]);
    }

    private Message buildPost() {
        if (msgInput.length != 1) return null;
        String content = msgInput[0];
        LinkedList<String> taggedUsers = new LinkedList<>();
        int startIndex, endIndex;
        while (content.contains("@")) {
            startIndex = content.indexOf('@');
            content = content.substring(startIndex + 1);
            if (content.contains(" ")) {
                endIndex = content.indexOf(' ');
                taggedUsers.add(content.substring(0, endIndex));
                content = content.substring(endIndex + 1);
            } else {
                taggedUsers.add(content);
                break;
            }
        }
        return new Post(connId, msgInput[0], taggedUsers);
    }

    private Message buildPM() {
        if (msgInput.length != 3) return null;
        return new PM(connId, msgInput[0], msgInput[1], msgInput[2]);
    }

    private Message buildLogStat() {
        return new LogStat(connId);
    }

    private Message buildStat() {
        if (msgInput.length != 1) return null;
        LinkedList<String> usernames = new LinkedList<>();
        String input = msgInput[0];
        int endIndex;
        while (input.contains("|")) {
            endIndex = input.indexOf('|');
            usernames.add(input.substring(0, endIndex));
            input = input.substring(endIndex + 1);
        }
        usernames.add(input);
        return new Stat(connId, usernames);
    }

    private Message buildBlock() {
        if (msgInput.length != 1) return null;
        return new Block(connId, msgInput[0]);
    }
    // endregion
}
