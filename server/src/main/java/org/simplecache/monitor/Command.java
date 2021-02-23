package org.simplecache.monitor;

public final class Command {

    private final Instruction instruction;
    private final Node node;

    public Command(Instruction instruction, Node node) {
        this.instruction = instruction;
        this.node = node;
    }

    public Instruction getInstruction() {
        return instruction;
    }

    public Node getNode() {
        return node;
    }

    @Override
    public String toString() {
        return "Command{" +
                "instruction=" + instruction +
                ", instance=" + node +
                '}';
    }
}
