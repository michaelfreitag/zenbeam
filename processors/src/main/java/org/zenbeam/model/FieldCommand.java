package org.zenbeam.model;

public class FieldCommand {

    String fieldKey;

    String command;

    boolean profileEnabled;

    String profile;

    public FieldCommand(String fieldKey, String command, boolean profileEnabled, String profile) {
        this.fieldKey = fieldKey;
        this.profile = profile;
        this.command = command;
        this.profileEnabled = profileEnabled;

    }

    public String getFieldKey() {
        return fieldKey;
    }

    public void setFieldKey(String fieldKey) {
        this.fieldKey = fieldKey;
    }

    public String getCommand() {

        String result = this.command;

        if (this.profileEnabled && this.profile != null && !this.profile.isEmpty()) {

            StringBuffer sb = new StringBuffer();

            String activeProfile = this.profile;
            boolean notActiveProfile = false;
            if (activeProfile != null && activeProfile.startsWith("!")) {
                activeProfile = activeProfile.replace("!", "");
                notActiveProfile = false;
            }
            if (notActiveProfile) {
                sb.append("if (profile != null && !profile.isEmpty() && !(\"" + activeProfile + "\").equalsIgnoreCase(profile)) { ").append("\r\n");
            } else {
                sb.append("if (profile != null && !profile.isEmpty() && (\"" + activeProfile + "\").equalsIgnoreCase(profile)) { ").append("\r\n");
            }

            sb.append(this.command).append("\r\n");
            sb.append("}").append("\r\n");

            result = sb.toString();
        }

        return result;
    }

    public void setCommand(String command) {
        this.command = command;
    }
}
