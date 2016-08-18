package cz.library.headerstrategy;

import android.support.v4.view.ViewCompat;
import android.util.Log;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;

import cz.library.PullToRefreshLayout;
import cz.library.RefreshState;
import cz.library.anim.AnimatorCompat;
import cz.library.anim.AnimatorUpdateListener;
import cz.library.header.RefreshHeader;

/**
 * Created by czz on 2016/8/16.
 */
public class HeaderFrontStrategy extends HeaderStrategy {

    private static final String TAG = "PullToRefreshBase";

    public HeaderFrontStrategy(PullToRefreshLayout pullToRefreshLayout) {
        super(pullToRefreshLayout);
    }

    @Override
    public void onInitRefreshHeader(RefreshHeader newHeader) {
        View newHeaderView = newHeader.getRefreshHeaderView();
        if(null==newHeader||null==newHeaderView) {
            throw  new NullPointerException("the new header is null ref!");
        }
        swapRefreshHeaderView(newHeaderView);
        newHeaderView.bringToFront();
    }

    @Override
    public void onLayout(boolean b, int left, int top, int right, int bottom) {
        PullToRefreshLayout pullToRefreshLayout = getPullToRefreshLayout();
        RefreshState refreshState = pullToRefreshLayout.getRefreshState();
        if(RefreshState.NONE==refreshState){
            RefreshHeader refreshHeader = pullToRefreshLayout.getRefreshHeader();
            View headerView = refreshHeader.getRefreshHeaderView();
            if(null != headerView){
                headerView.layout(0,-headerView.getMeasuredHeight(),headerView.getMeasuredWidth(),0);
            }
            View refreshView = pullToRefreshLayout.getRefreshView();
            refreshView.layout(0,0,right,bottom);
        }
    }

    @Override
    public void onMoveOffset(float distanceY) {
        PullToRefreshLayout pullToRefreshLayout = getPullToRefreshLayout();
        RefreshHeader refreshHeader = pullToRefreshLayout.getRefreshHeader();
        View refreshHeaderView = refreshHeader.getRefreshHeaderView();
        int headerHeight = refreshHeader.getHeaderHeight();
        float pullMaxHeight = pullToRefreshLayout.getPullMaxHeight();

        float resistance = pullToRefreshLayout.getResistance();
        int scrollY=Math.abs(refreshHeaderView.getBottom());
        int moveDistanceY = (int) (distanceY / resistance);
        if (distanceY>0&&pullMaxHeight<=scrollY) moveDistanceY=0;

        float fraction = scrollY * 1.0f / headerHeight;
        refreshHeaderView.offsetTopAndBottom(moveDistanceY);

        if(1.0f<=fraction) fraction=1.0f;
        pullToRefreshLayout.refreshStateChanged(fraction);
        refreshHeader.onRefreshOffset(fraction,scrollY,headerHeight);
    }

    @Override
    public void onResetRefresh(RefreshState refreshState) {
        PullToRefreshLayout pullToRefreshLayout = getPullToRefreshLayout();
        RefreshHeader refreshHeader = pullToRefreshLayout.getRefreshHeader();
        View refreshHeaderView = refreshHeader.getRefreshHeaderView();
        int measuredHeight = refreshHeaderView.getMeasuredHeight();
        int top = refreshHeaderView.getBottom();
        if(RefreshState.NONE==refreshState){
            Log.e(TAG,"none:"+top);
            refreshHeaderView.layout(0,-measuredHeight,refreshHeaderView.getMeasuredWidth(),0);
        } else if(RefreshState.PULL_START==refreshState){
            pullToRefreshLayout.setReleasing(true);
            Log.e(TAG,"pull start:"+top);
            offsetOverlapHeader(top);
        } else if(RefreshState.RELEASE_REFRESHING_START==refreshState){
            Log.e(TAG,"release refreshing start start");
            VelocityTracker velocityTracker = pullToRefreshLayout.getVelocityTracker();
            int scaledMinimumFlingVelocity = pullToRefreshLayout.getScaledMinimumFlingVelocity();
            float yVelocity = velocityTracker.getYVelocity();
            if(yVelocity>scaledMinimumFlingVelocity){
                //scroll
                offsetOverlapHeader(top-measuredHeight);
                pullToRefreshLayout.setRefreshState(RefreshState.START_REFRESHING);
                refreshHeader.onRefreshStateChange(RefreshState.START_REFRESHING);
            } else {
                offsetOverlapHeader(top);
            }
        } else if(RefreshState.RELEASE_START==refreshState||RefreshState.START_REFRESHING==refreshState){
            Log.e(TAG,"release start:"+top);
            offsetOverlapHeader(top-measuredHeight);
            pullToRefreshLayout.setRefreshState(RefreshState.START_REFRESHING);
            refreshHeader.onRefreshStateChange(RefreshState.START_REFRESHING);
        }
    }

    @Override
    public void onRefreshComplete() {
        final PullToRefreshLayout pullToRefreshLayout = getPullToRefreshLayout();
        RefreshState refreshState = pullToRefreshLayout.getRefreshState();
        Log.e(TAG,"onRefreshComplete");
        if(RefreshState.START_REFRESHING==refreshState){
            RefreshHeader refreshHeader = pullToRefreshLayout.getRefreshHeader();
            final View refreshHeaderView = refreshHeader.getRefreshHeaderView();
            int headerHeight = refreshHeader.getHeaderHeight();

            AnimatorCompat.Animator animator = AnimatorCompat.ofInt(headerHeight);
            animator.setDuration(pullToRefreshLayout.getScrollDuration()/2);
            animator.addUpdateListener(new AnimatorUpdateListener() {
                private int lastValue;
                @Override
                public void onAnimationUpdate(AnimatorCompat.Animator animation,float fraction) {
                    Integer value = animation.getAnimatedIntValue();
                    refreshHeaderView.offsetTopAndBottom(lastValue-value);
                    ViewCompat.setScaleX(refreshHeaderView,(1f-fraction)*1f);
                    ViewCompat.setScaleY(refreshHeaderView,(1f-fraction)*1f);
                    lastValue=value;
                }
            });
            animator.addListener(new cz.library.anim.AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(AnimatorCompat.Animator animation) {
                    super.onAnimationEnd(animation);
                    ViewCompat.setScaleX(refreshHeaderView,1f);
                    ViewCompat.setScaleY(refreshHeaderView,1f);
                    pullToRefreshLayout.setRefreshState(RefreshState.NONE);
                    pullToRefreshLayout.requestLayout();
                }
            });
            animator.start();
        } else if(RefreshState.RELEASE_REFRESHING_START==refreshState){
            pullToRefreshLayout.setRefreshState(RefreshState.NONE);
        }
    }

    @Override
    public void autoRefreshing(boolean anim) {
        PullToRefreshLayout pullToRefreshLayout = getPullToRefreshLayout();
        RefreshHeader refreshHeader = pullToRefreshLayout.getRefreshHeader();
        RefreshState refreshState = pullToRefreshLayout.getRefreshState();
        int headerHeight = refreshHeader.getHeaderHeight();
        if(RefreshState.NONE==refreshState){
            if(anim){
                offsetOverlapHeader(headerHeight);
            } else {
                View refreshHeaderView = refreshHeader.getRefreshHeaderView();
                refreshHeaderView.offsetTopAndBottom(headerHeight);
            }
        }
        pullToRefreshLayout.setRefreshState(RefreshState.START_REFRESHING);
    }

    private void offsetOverlapHeader(int top) {
        PullToRefreshLayout pullToRefreshLayout = getPullToRefreshLayout();
        final View refreshHeaderView = pullToRefreshLayout.getRefreshHeader().getRefreshHeaderView();

        AnimatorCompat.Animator animator = AnimatorCompat.ofInt(top);
        animator.setDuration(pullToRefreshLayout.getScrollDuration()/2);
        animator.addUpdateListener(new AnimatorUpdateListener() {
            private int lastValue;
            @Override
            public void onAnimationUpdate(AnimatorCompat.Animator animation, float fraction) {
                Integer value = animation.getAnimatedIntValue();
                refreshHeaderView.offsetTopAndBottom(lastValue-value);
                lastValue=value;
            }
        });
        animator.start();
    }

    @Override
    public boolean isMoveToTop() {
        PullToRefreshLayout pullToRefreshLayout = getPullToRefreshLayout();
        View refreshHeaderView = pullToRefreshLayout.getRefreshHeader().getRefreshHeaderView();
        boolean result=false;
        if(null!=refreshHeaderView){
            result=0>=refreshHeaderView.getBottom();
        }
        return result;
    }

    @Override
    public boolean isIntercept(float distanceY) {
        PullToRefreshLayout pullToRefreshLayout = getPullToRefreshLayout();
        RefreshState refreshState = pullToRefreshLayout.getRefreshState();

        View refreshHeaderView = pullToRefreshLayout.getRefreshHeader().getRefreshHeaderView();
        boolean isTop = pullToRefreshLayout.isChildScrollToTop();

        Log.e(TAG,"isTop:"+isTop+" bottom:"+refreshHeaderView.getBottom()+" refreshState:"+refreshState);
        return isTop && (distanceY > 0 || 0 <= refreshHeaderView.getBottom());
    }
}
