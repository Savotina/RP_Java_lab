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

    public int GetColor(String str) {
        String[] parts = str.split(", ");
        String color = parts[parts.length - 1];
        return Integer.parseInt(color);
    }
}





/*package net.command;

import java.io.Serializable;

public class SerializableCommand implements Serializable {

    public final int x;
    public final int y;
    public final int color; // 0 для черного, 1 для белого

    public SerializableCommand(int x, int y, int color) {
        this.x = x;
        this.y = y;
        this.color = color;
    }
}*/
