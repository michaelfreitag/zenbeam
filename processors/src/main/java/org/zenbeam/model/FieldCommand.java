package org.zenbeam.model;

public class FieldCommand {

    String fieldKey;

    String command;

    public FieldCommand(String fieldKey, String command) {
        this.fieldKey = fieldKey;
        this.command = command;
    }

    public String getFieldKey() {
        return fieldKey;
    }

    public void setFieldKey(String fieldKey) {
        this.fieldKey = fieldKey;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }
}
