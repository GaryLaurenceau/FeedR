//package sokss.fadeactionbar;
//
//import android.app.Activity;
//import android.content.Context;
//import android.graphics.Color;
//import android.graphics.drawable.ColorDrawable;
//import android.graphics.drawable.Drawable;
//import android.os.Build;
//import android.widget.ScrollView;
//import android.widget.TextView;
//
///**
// * Created by gary on 26/11/14.
// */
//public class FadeActionBar {
//
//    private Activity mActivity;
//    private ColorDrawable mColorDrawable;
//
//    public FadeActionBar(Activity activity, NotifyingScrollView notifyingScrollView, int color) {
//        mActivity = activity;
//        mColorDrawable = new ColorDrawable(color);
//        mColorDrawable.setAlpha(0);
//        mActivity.getActionBar().setBackgroundDrawable(mColorDrawable);
//
//        notifyingScrollView.setOnScrollChangedListener(mOnScrollChangedListener);
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
//            mColorDrawable.setCallback(mDrawableCallback);
//        }
//    }
//
//    private NotifyingScrollView.OnScrollChangedListener mOnScrollChangedListener = new NotifyingScrollView.OnScrollChangedListener() {
//        public void onScrollChanged(ScrollView who, int l, int t, int oldl, int oldt) {
//            final int headerHeight = mImageHeader.getHeight() - mNewsActivity.getActionBarFromFragment().getHeight();
//            final float ratio = (float) Math.min(Math.max(t, 0), headerHeight) / headerHeight;
//            final Integer newAlpha = (int) (ratio * 255);
//            mNewsActivity.getActionBarBackgroundDrawable().setAlpha(newAlpha);
//
////            int titleId = getResources().getIdentifier("action_bar_title", "id", "android");
////            TextView abTitle = (TextView) mNewsActivity.findViewById(titleId);
////            abTitle.setTextColor(Color.argb(newAlpha, 255, 255, 255));
//        }
//    };
//
//    private Drawable.Callback mDrawableCallback = new Drawable.Callback() {
//        @Override
//        public void invalidateDrawable(Drawable who) {
//            mNewsActivity.getActionBarFromFragment().setBackgroundDrawable(who);
//        }
//
//        @Override
//        public void scheduleDrawable(Drawable who, Runnable what, long when) {
//        }
//
//        @Override
//        public void unscheduleDrawable(Drawable who, Runnable what) {
//        }
//    };
//
//    public Activity getActivity() {
//        return mActivity;
//    }
//
//    public void setActivity(Activity activity) {
//        mActivity = activity;
//    }
//
//    public ColorDrawable getColorDrawable() {
//        return mColorDrawable;
//    }
//
//    public void setColorDrawable(ColorDrawable colorDrawable) {
//        mColorDrawable = colorDrawable;
//    }
//}
