package com.ankk.motiontracker;

public enum ActivityType {
    WALK(0),
    RUN(1),
    OTHER(2),
    NOTHING(3);

    private int val;

    private ActivityType(int val){
        this.val = val;
    }

    public int getValue(){
        return val;
    }
}
