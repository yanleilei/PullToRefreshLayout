package cz.library.header;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ProgressBar;
import android.widget.TextView;

import cz.library.R;
import cz.library.RefreshState;

/**
 * Created by czz on 2016/8/15.
 */
public class FlipHeader extends RefreshHeader {
    private static final String TAG = "FlipHeader";
    private final Animation rotateAnimation, resetRotateAnimation;
    private View indicatorView;
    private TextView refreshInfo;
    private TextView refreshTime;
    private ProgressBar progressBar;
    private RefreshState lastState;

    public FlipHeader(Context context,ViewGroup parent) {
        super(context,parent);
        View view=LayoutInflater.from(context).inflate(R.layout.header_flip_layout,parent,false);
        indicatorView= view.findViewById(R.id.iv_indicator);
        refreshInfo= (TextView) view.findViewById(R.id.refresh_text);
        refreshTime= (TextView) view.findViewById(R.id.refresh_time);
        progressBar= (ProgressBar) view.findViewById(R.id.progress);
        this.headerView=view;

        lastState=RefreshState.NONE;


        Interpolator ANIMATION_INTERPOLATOR=new LinearInterpolator();
        rotateAnimation = new RotateAnimation(0, 180, Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setInterpolator(ANIMATION_INTERPOLATOR);
        rotateAnimation.setDuration(200);
        rotateAnimation.setFillAfter(true);

        resetRotateAnimation = new RotateAnimation(180, 360, Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        resetRotateAnimation.setInterpolator(ANIMATION_INTERPOLATOR);
        resetRotateAnimation.setDuration(200);
        resetRotateAnimation.setFillAfter(true);
    }

    @Override
    public void onRefreshOffset(float fraction, int height,int totalHeight) {
        //nothing to do
    }

    @Override
    public void onRefreshStateChange(RefreshState state) {
        Log.e(TAG,"RefreshState:"+state+" lastState:"+lastState);
        switch (state){
            case RELEASE_START:
                refreshInfo.setText(R.string.pull_to_refresh_release);
                progressBar.setVisibility(View.GONE);
                indicatorView.setVisibility(View.VISIBLE);
                indicatorView.startAnimation(rotateAnimation);
                break;
            case START_REFRESHING:
            case RELEASE_REFRESHING_START:
                refreshInfo.setText(R.string.pull_to_refresh_refreshing);
                indicatorView.clearAnimation();
                indicatorView.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                break;
            case REFRESHING_START_COMPLETE:
                progressBar.setVisibility(View.GONE);
                indicatorView.setVisibility(View.GONE);
                refreshInfo.setText(R.string.pull_to_refresh_complete);
                break;
            case PULL_START:
                if(RefreshState.RELEASE_START==lastState){
                    indicatorView.startAnimation(resetRotateAnimation);
                }
            case NONE:
                progressBar.setVisibility(View.GONE);
                indicatorView.setVisibility(View.VISIBLE);
                refreshInfo.setText(R.string.pull_to_refresh_pull);
                break;
        }
        lastState=state;
    }

}
