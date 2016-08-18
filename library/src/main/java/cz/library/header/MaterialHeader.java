package cz.library.header;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.ViewGroup;

import cz.library.RefreshState;
import cz.library.widget.MaterialProgressDrawable;
import cz.library.widget.MaterialProgressView;

/**
 * Created by Administrator on 2016/8/16.
 */
public class MaterialHeader extends RefreshHeader {
    private MaterialProgressView materialProgressView;

    public MaterialHeader(Context context, ViewGroup parent) {
        super(context, parent);
        materialProgressView=new MaterialProgressView(context);
        materialProgressView.setPadding(0,60,0,60);
        materialProgressView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        headerView=materialProgressView;
    }

    @Override
    public void onRefreshOffset(float fraction, int height, int totalHeight) {
        MaterialProgressDrawable drawable = materialProgressView.getMaterialDrawable();
        drawable.setAlpha((int) (255 * fraction));
        float strokeStart = ((fraction) * .8f);
        drawable.setStartEndTrim(0f, Math.min(0.8f, strokeStart));
        drawable.setArrowScale(Math.min(1f, fraction));

        float rotation = (-0.25f + .4f * fraction + fraction * 2) * .5f;
        drawable.setProgressRotation(rotation);
    }

    @Override
    public void onRefreshStateChange(RefreshState state) {
        MaterialProgressDrawable drawable = materialProgressView.getMaterialDrawable();
        switch (state){
            case RELEASE_START:
                break;
            case START_REFRESHING:
            case RELEASE_REFRESHING_START:
                if(!drawable.isRunning()){
                    drawable.showArrow(false);
                    drawable.setAlpha(0xFF);
                    drawable.start();
                }
                break;
            case PULL_START:
            case NONE:
                default:
                    drawable.stop();
                    drawable.setArrowScale(1f);
                    drawable.showArrow(true);
                break;
        }
    }
}
