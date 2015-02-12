package com.sokss.feedr.app.utils;

import android.app.Fragment;
import android.content.Context;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.view.View;

import com.sokss.feedr.app.R;

import java.util.Random;

/**
 * Created by gary on 26/09/14.
 */
public class ColorManager {

    // colors
    private int[] mColors;
    private Integer mColorId = 0;

    public ColorManager(Context context) {
        mColors = context.getResources().getIntArray(R.array.color_array);
    }

    public ColorManager(Fragment fragment) {
        mColors = fragment.getResources().getIntArray(R.array.color_array);
    }

    public void applyColorBackgroundRoundCorner(Context context, View view, int color) {
        ShapeDrawable footerBackground = new ShapeDrawable();

        float[] radii = new float[8];
//        radii[0] = context.getResources().getDimension(R.dimen.footer_corners);
//        radii[1] = context.getResources().getDimension(R.dimen.footer_corners);
//
//        radii[2] = context.getResources().getDimension(R.dimen.footer_corners);
//        radii[3] = context.getResources().getDimension(R.dimen.footer_corners);
//
//        radii[4] = context.getResources().getDimension(R.dimen.footer_corners);
//        radii[5] = context.getResources().getDimension(R.dimen.footer_corners);
//
//        radii[6] = context.getResources().getDimension(R.dimen.footer_corners);
//        radii[7] = context.getResources().getDimension(R.dimen.footer_corners);
        footerBackground.setShape(new RoundRectShape(radii, null, null));

        footerBackground.getPaint().setColor(color);

        view.setBackgroundDrawable(footerBackground);
    }

    public int getRandomColor() {
        mColorId = new Random().nextInt(mColors.length);
        return mColorId;
    }

    public int getPrevColor() {
        mColorId--;
        if (mColorId < 0)
            mColorId = mColors.length - 1;
        return mColorId;
    }

    public int getNextColor() {
        mColorId++;
        if (mColorId >= mColors.length)
            mColorId = 0;
        return mColorId;
    }

    public int[] getColors() {
        return mColors;
    }

    public void setColors(int[] colors) {
        mColors = colors;
    }

    public Integer getColorId() {
        return mColorId;
    }

    public void setColorId(Integer colorId) {
        mColorId = colorId;
    }
}
