package com.theta360.pluginapplication.webrtc.sample.network.model.commands;

/**
 * CommandsName
 */
public enum CommandsName {
    TAKE_PICTURE("camera.takePicture"),
    GET_SHOOTING_STATUS("camera.getShootingStatus"),
    COMMANDS_EXECUTE("camera.commands.execute"),
    SET_OPTIONS("camera.setOptions"),
    GET_OPTIONS("camera.getOptions"),
    START_LIVE_PREVIEW("camera.startLivePreview"),
    STOP_LIVE_PREVIEW("camera.stopLivePreview"),
    SET_SETTINGS("camera.setSettings"),
    GET_SETTINGS("camera.getSettings"),
    GET_STATUS("camera.getStatus"),
    UNKNOWN("unknown"),;

    private final String mCommands;

    CommandsName(final String commands) {
        this.mCommands = commands;
    }

    public static CommandsName getValue(final String name) {
        for (CommandsName commandsName : CommandsName.values()) {
            if (commandsName.toString().equals(name)) {
                return commandsName;
            }
        }

        return null;
    }

    @Override
    public String toString() {
        return this.mCommands;
    }
}
