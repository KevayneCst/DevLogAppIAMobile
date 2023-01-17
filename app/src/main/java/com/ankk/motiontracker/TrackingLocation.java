package com.ankk.motiontracker;

public enum TrackingLocation {
    LEFT_HAND(0),
    RIGHT_HAND(1),
    LEFT_TROUSER_POCKET(2),
    RIGHT_TROUSER_POCKET(3),
    LEFT_JACKET_POCKET(4),
    RIGHT_JACKET_POCKET(5);

    private int val;

    private TrackingLocation(int val){
        this.val = val;
    }

    public int getValue(){
        return val;
    }
}
