package cz.library.headerstrategy;

import android.util.Log;
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
public class HeaderScrollStrategy extends HeaderStrategy {

    private static final String TAG = "HeaderScrollStrategy";

    public HeaderScrollStrategy(PullToRefreshLayout pullToRefreshLayout) {
        super(pullToRefreshLayout);
    }

    @Override
    public void onInitRefreshHeader(RefreshHeader newHeader) {
        View newHeaderView = newHeader.getRefreshHeaderView();
        if(null==newHeader||null==newHeaderView) {
            throw  new NullPointerException("the new header is null ref!");
        }
        swapRefreshHeaderView(newHeaderView);
        getPullToRefreshLayout().getRefreshView().bringToFront();
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
        float pullMaxHeight = pullToRefreshLayout.getPullMaxHeight();
        float resistance = pullToRefreshLayout.getResistance();

        int scrollY=refreshView.getTop();
        int moveDistanceY = (int) (distanceY / resistance);
        if (distanceY>0&&pullMaxHeight<=scrollY) moveDistanceY=0;
        refreshView.offsetTopAndBottom(moveDistanceY);
    }

    @Override
    public void onResetRefresh(RefreshState refreshState) {
        PullToRefreshLayout pullToRefreshLayout = getPullToRefreshLayout();
        View refreshView = pullToRefreshLayout.getRefreshView();
        if(RefreshState.NONE==refreshState){
            refreshView.layout(0,0,pullToRefreshLayout.getMeasuredWidth(), pullToRefreshLayout.getMeasuredHeight());
        } else {
            Log.e(TAG,"top:"+refreshView.getTop());
            pullToRefreshLayout.setReleasing(true);
            offsetOverlapHeader(refreshView.getTop());
        }
    }

    @Override
    public void onRefreshComplete() {
        PullToRefreshLayout pullToRefreshLayout = getPullToRefreshLayout();
        int headerHeight = pullToRefreshLayout.getRefreshHeader().getHeaderHeight();
        RefreshState refreshState = pullToRefreshLayout.getRefreshState();
        if(RefreshState.START_REFRESHING==refreshState){
            offsetOverlapHeader(-headerHeight);
        }
        pullToRefreshLayout.setRefreshState(RefreshState.NONE);
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
            result=Math.abs(refreshView.getTop()) <40;
        }
        return result;
    }

    @Override
    public boolean isIntercept(float distanceY) {
        PullToRefreshLayout pullToRefreshLayout = getPullToRefreshLayout();
        View refreshView = pullToRefreshLayout.getRefreshView();
        boolean isTop = pullToRefreshLayout.isChildScrollToTop();
        return isTop && distanceY > 0 || refreshView.getTop() >  20;
    }
}
