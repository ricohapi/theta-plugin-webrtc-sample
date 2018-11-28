package com.theta360.pluginapplication.webrtc.sample.network.model.values;

public enum Status {
    SHOOTING("shooting"),
    IDLE("idle"),;

    private final String mStatus;

    Status(String status) {
        this.mStatus = status;
    }

    @Override
    public String toString() {
        return this.mStatus;
    }
}
