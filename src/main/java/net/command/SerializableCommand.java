package net.command;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class SerializableCommand implements Serializable {
    public Set<String> circles;

    public SerializableCommand() {
        this.circles = new HashSet<>();
    }

    public SerializableCommand(Set<String> circ) {
        this.circles = circ;
    }
}