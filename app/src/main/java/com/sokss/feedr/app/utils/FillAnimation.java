package com.sokss.feedr.app.utils;

import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;

/**
 * Created by gary on 18/12/14.
 */
public class FillAnimation extends Animation {

    private static final float SPEED = 1f;

    private float mStart = 0f;
    private float mEnd = 1f;

    public FillAnimation() {
        setInterpolator(new LinearInterpolator());

        float duration = Math.abs(mEnd - mStart) / SPEED;
        setDuration((long) duration);
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        super.applyTransformation(interpolatedTime, t);

        Float offset = (mEnd - mStart) * interpolatedTime + mStart;
        Log.d("OFFSET", offset.toString());
//        postInvalidate();
    }

}
