package com.ankk.motiontracker.debug;

public class DebugArray {

    private float x[][];

    public DebugArray(float x[][]){
        this.x = x;
    }
    public void Print2DArray(){
        for(int i =0;i<this.x.length;i++){
            for (int j=0;j<this.x[i].length;j++){
                System.out.println("["+i+"]"+this.x[i][j]);
            }
        }
    }
}
