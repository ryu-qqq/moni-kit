package com.monikit.core;

public enum LogLevel {
    INFO, WARN, ERROR, DEBUG, TRACE;

    public boolean isEmergency(){
        return this.equals(ERROR);
    }
}
