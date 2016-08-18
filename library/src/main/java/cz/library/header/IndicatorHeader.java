package cz.library.header;

import android.content.Context;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.TextView;

import cz.library.R;
import cz.library.RefreshState;

/**
 * Created by czz on 2016/8/15.
 */
public class IndicatorHeader extends RefreshHeader {
    private static final String TAG = "PullToRefreshBase";
    private RotateAnimation rotateAnimation;
    private View indicatorView;
    private TextView refreshInfo;

    public IndicatorHeader(Context context,ViewGroup parent) {
        super(context,parent);
        View view=LayoutInflater.from(context).inflate(R.layout.header_indicator_layout,parent,false);
        indicatorView=view.findViewById(R.id.iv_indicator);
        refreshInfo= (TextView) view.findViewById(R.id.text);
        this.headerView=view;

        rotateAnimation = new RotateAnimation(0, 720, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);
        rotateAnimation.setInterpolator(new LinearInterpolator());
        rotateAnimation.setDuration(1000);
        rotateAnimation.setRepeatCount(Animation.INFINITE);
        rotateAnimation.setRepeatMode(Animation.RESTART);
    }

    @Override
    public View getRefreshHeaderView() {
        return headerView;
    }

    @Override
    public void onRefreshOffset(float fraction, int height,int totalHeight) {
        //Log.e(TAG,"fraction:"+fraction+" height:"+height+" totalHeight:"+totalHeight);
        if(Build.VERSION.SDK_INT>Build.VERSION_CODES.HONEYCOMB){
            indicatorView.setRotation(360*fraction);
        }
    }

    @Override
    public void onRefreshStateChange(RefreshState state) {
        Log.e(TAG,"RefreshState:"+state);
        switch (state){
            case RELEASE_START:
                refreshInfo.setText(R.string.pull_to_refresh_release);
                break;
            case START_REFRESHING:
            case RELEASE_REFRESHING_START:
                refreshInfo.setText(R.string.pull_to_refresh_refreshing);
                indicatorView.startAnimation(rotateAnimation);
                break;
            case PULL_START:
                default:
                    indicatorView.clearAnimation();
                    refreshInfo.setText(R.string.pull_to_refresh_pull);
                break;

        }
    }

}
