package org.simplecache.monitor;

public final class Command {

    private final Instruction instruction;
    private final Instance instance;

    public Command(Instruction instruction, Instance instance) {
        this.instruction = instruction;
        this.instance = instance;
    }

    public Instruction getInstruction() {
        return instruction;
    }

    public Instance getInstance() {
        return instance;
    }

    @Override
    public String toString() {
        return "Command{" +
                "instruction=" + instruction +
                ", instance=" + instance +
                '}';
    }
}
