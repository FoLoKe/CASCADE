package com.foloke.cascade.utils;

public class SimpleCumulativeEMWA {
    private float dxt_1 = 0; // previous EMWA xt
    private float et_1 = 0; // previous error t-1
    private float et_2 = 0; // previous error t-2
    private float St_1 = 0; // previous cumulative sum

    // params
    private final float a;
    private final float emwaThreshold;
    private final float m;
    private final float cumulativeThreshold;

    public SimpleCumulativeEMWA() {
        emwaThreshold = 1.f;
        a = 0.5f;
        m = 10;
        cumulativeThreshold = 10;
    }

    public SimpleCumulativeEMWA(float a, float emwaThreshold, float m, float cumulativeThreshold) {
        this.a = a;
        this.emwaThreshold = emwaThreshold;
        this.m = m;
        this.cumulativeThreshold = cumulativeThreshold;
    }

    public boolean put(float xt) {
        float dxt = a * xt + (1 - a) * dxt_1;
        dxt_1 = dxt;

        float et = Math.abs(xt - dxt);

        // Standard deviation (actually should contain more than 2 data entries)
        float mean = (et_2 + et_1) / 2;
        float sqMean = (float)(Math.pow(et_2 - 2, 2) + Math.pow(et_1 - 2, 2));
        float variance = sqMean / mean;
        float deviation = (float)Math.sqrt(variance);
        //

        float upperLimit = dxt + Math.max(emwaThreshold * deviation, m);

        // if(xt > upperLimit) could be false alarm so using Cumulative Sum

        float St = Math.max(St_1 + xt - upperLimit, 0);
        float sumLimit = cumulativeThreshold * deviation;

        System.out.println(St + " limit: " + sumLimit);
        if (St > sumLimit) {
            System.out.println("LIMIT! by Cumul");
        }
        St_1 = St;
        et_2 = et_1;
        et_1 = et;
        return St > sumLimit;
    }
}
