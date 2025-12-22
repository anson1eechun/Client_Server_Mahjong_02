package com.mahjong.model;

import java.io.Serializable;
import java.util.Map;

/**
 * Data packet exchanged between Client and Server.
 * Designed for JSON serialization (Jackson).
 */
public class Packet implements Serializable {
    private Command command;
    private Map<String, Object> data;
    
    // Jackson needs default constructor
    public Packet() {}
    
    public Packet(Command command, Map<String, Object> data) {
        this.command = command;
        this.data = data;
    }

    public Command getCommand() {
        return command;
    }

    public void setCommand(Command command) {
        this.command = command;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }
    
    @Override
    public String toString() {
        return "Packet{cmd=" + command + ", data=" + data + "}";
    }
}
