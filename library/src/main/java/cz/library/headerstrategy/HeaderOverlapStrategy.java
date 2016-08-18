package cz.library.headerstrategy;

import android.util.Log;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;

import cz.library.PullToRefreshLayout;
import cz.library.RefreshState;
import cz.library.anim.AnimatorCompat;
import cz.library.anim.AnimatorListenerAdapter;
import cz.library.anim.AnimatorUpdateListener;
import cz.library.header.RefreshHeader;

/**
 * Created by czz on 2016/8/16.
 */
public class HeaderOverlapStrategy extends HeaderStrategy {

    private static final String TAG = "HeaderOverlapStrategy";

    public HeaderOverlapStrategy(PullToRefreshLayout pullToRefreshLayout) {
        super(pullToRefreshLayout);
    }

    @Override
    public void onInitRefreshHeader(RefreshHeader newHeader) {
        View refreshHeaderView = newHeader.getRefreshHeaderView();
        if(null==newHeader||null==refreshHeaderView) {
            throw  new NullPointerException("the new header is null ref!");
        }
        swapRefreshHeaderView(refreshHeaderView);
        View refreshView = getPullToRefreshLayout().getRefreshView();
        if(null!=refreshView){
            refreshView.bringToFront();
        }
    }

    @Override
    public void onLayout(boolean b, int left, int top, int right, int bottom) {
        PullToRefreshLayout pullToRefreshLayout = getPullToRefreshLayout();
        RefreshHeader refreshHeader = pullToRefreshLayout.getRefreshHeader();
        View headerView = refreshHeader.getRefreshHeaderView();

        RefreshState refreshState = pullToRefreshLayout.getRefreshState();
        if(RefreshState.NONE==refreshState){
            if(null != headerView){
                headerView.layout(0,0,headerView.getMeasuredWidth(),headerView.getMeasuredHeight());
            }
            View refreshView = pullToRefreshLayout.getRefreshView();
            refreshView.layout(0,0,refreshView.getMeasuredWidth(),refreshView.getMeasuredHeight());
        }
    }

    @Override
    public void onMoveOffset(float distanceY) {
        PullToRefreshLayout pullToRefreshLayout = getPullToRefreshLayout();
        View refreshView = pullToRefreshLayout.getRefreshView();
        RefreshHeader refreshHeader = pullToRefreshLayout.getRefreshHeader();
        int headerHeight = refreshHeader.getHeaderHeight();
        float pullMaxHeight = pullToRefreshLayout.getPullMaxHeight();

        float resistance = pullToRefreshLayout.getResistance();
        int scrollY=refreshView.getTop();
        int moveDistanceY = (int) (distanceY / resistance);
        if (distanceY>0&&pullMaxHeight<=scrollY) moveDistanceY=0;

        float fraction = scrollY * 1.0f / headerHeight;
        refreshView.offsetTopAndBottom(moveDistanceY);

        if(1.0f<=fraction) fraction=1.0f;
        pullToRefreshLayout.refreshStateChanged(fraction);
        refreshHeader.onRefreshOffset(fraction,scrollY,headerHeight);
    }

    @Override
    public void onResetRefresh(RefreshState refreshState) {
        PullToRefreshLayout pullToRefreshLayout = getPullToRefreshLayout();
        int width = pullToRefreshLayout.getMeasuredWidth();
        int height = pullToRefreshLayout.getMeasuredHeight();
        RefreshHeader refreshHeader = pullToRefreshLayout.getRefreshHeader();
        View refreshView = pullToRefreshLayout.getRefreshView();

        int top = refreshView.getTop();
        if(RefreshState.NONE==refreshState){
            refreshView.layout(0,0,width,height);
        } else if(RefreshState.PULL_START==refreshState){
            pullToRefreshLayout.setReleasing(true);
            offsetOverlapHeader(top);
        } else if(RefreshState.RELEASE_REFRESHING_START==refreshState){
            VelocityTracker velocityTracker = pullToRefreshLayout.getVelocityTracker();
            int scaledMinimumFlingVelocity = pullToRefreshLayout.getScaledMinimumFlingVelocity();
            float yVelocity = velocityTracker.getYVelocity();
            if(yVelocity>scaledMinimumFlingVelocity){
                //scroll
                View refreshHeaderView = refreshHeader.getRefreshHeaderView();
                int measuredHeight = refreshHeaderView.getMeasuredHeight();
                offsetOverlapHeader(top-measuredHeight);
                pullToRefreshLayout.setRefreshState(RefreshState.START_REFRESHING);
                refreshHeader.onRefreshStateChange(RefreshState.START_REFRESHING);
            } else {
                offsetOverlapHeader(top);
            }
        } else if(RefreshState.RELEASE_START==refreshState||RefreshState.START_REFRESHING==refreshState){
            View refreshHeaderView = refreshHeader.getRefreshHeaderView();
            int measuredHeight = refreshHeaderView.getMeasuredHeight();
            offsetOverlapHeader(top-measuredHeight);
            pullToRefreshLayout.setRefreshState(RefreshState.START_REFRESHING);
            refreshHeader.onRefreshStateChange(RefreshState.START_REFRESHING);
        }
    }

    @Override
    public void onRefreshComplete() {
        final PullToRefreshLayout pullToRefreshLayout = getPullToRefreshLayout();
        final View refreshView = pullToRefreshLayout.getRefreshView();
        final RefreshState refreshState = pullToRefreshLayout.getRefreshState();
        if(RefreshState.START_REFRESHING==refreshState){
            AnimatorCompat.Animator animator = AnimatorCompat.ofInt(refreshView.getTop());
            animator.setDuration(pullToRefreshLayout.getScrollDuration()/3);
            animator.addUpdateListener(new AnimatorUpdateListener() {
                private int lastValue;
                @Override
                public void onAnimationUpdate(AnimatorCompat.Animator animation, float fraction) {
                    Integer value = animation.getAnimatedIntValue();
                    refreshView.offsetTopAndBottom(lastValue-value);
                    lastValue=value;
                }
            });
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(AnimatorCompat.Animator animation) {
                    super.onAnimationEnd(animation);
                    pullToRefreshLayout.setRefreshState(RefreshState.NONE);
                    pullToRefreshLayout.requestLayout();
                }
            });
            animator.start();
        }
    }

    @Override
    public void autoRefreshing(boolean anim) {
        PullToRefreshLayout pullToRefreshLayout = getPullToRefreshLayout();
        int headerHeight = pullToRefreshLayout.getRefreshHeader().getHeaderHeight();
        RefreshState refreshState = pullToRefreshLayout.getRefreshState();
        if(RefreshState.NONE==refreshState){
            if(anim){
                offsetOverlapHeader(headerHeight);
            } else {
                View refreshView = pullToRefreshLayout.getRefreshView();
                refreshView.offsetTopAndBottom(headerHeight);
            }
        }
        pullToRefreshLayout.setRefreshState(RefreshState.START_REFRESHING);
    }

    private void offsetOverlapHeader(int top) {
        PullToRefreshLayout pullToRefreshLayout = getPullToRefreshLayout();
        final View refreshView = pullToRefreshLayout.getRefreshView();

        AnimatorCompat.Animator animator = AnimatorCompat.ofInt(top);
        animator.setDuration(pullToRefreshLayout.getScrollDuration()/3);
        animator.addUpdateListener(new AnimatorUpdateListener() {
            private int lastValue;
            @Override
            public void onAnimationUpdate(AnimatorCompat.Animator animation, float fraction) {
                Integer value = animation.getAnimatedIntValue();
                refreshView.offsetTopAndBottom(lastValue-value);
                lastValue=value;
            }
        });
        animator.start();
    }

    @Override
    public boolean isMoveToTop() {
        PullToRefreshLayout pullToRefreshLayout = getPullToRefreshLayout();
        View refreshView = pullToRefreshLayout.getRefreshView();
        boolean result=false;
        if(null!=refreshView){
            result=pullToRefreshLayout.isChildScrollToTop()&&Math.abs(refreshView.getTop()) <40;
        }
        Log.e(TAG,"result:"+result);
        return result;
    }

    @Override
    public boolean isIntercept(float distanceY) {
        PullToRefreshLayout pullToRefreshLayout = getPullToRefreshLayout();
        View refreshView = pullToRefreshLayout.getRefreshView();
        boolean isTop = pullToRefreshLayout.isChildScrollToTop();
        Log.e(TAG,"isTop:"+isTop+" top:"+refreshView.getTop());
        return isTop && (distanceY > 0 || refreshView.getTop() >  20);
    }
}
