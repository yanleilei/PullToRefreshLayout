package cz.library.headerstrategy;

import android.util.Log;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;

import cz.library.PullToRefreshLayout;
import cz.library.RefreshState;
import cz.library.header.RefreshHeader;

/**
 * Created by czz on 2016/8/16.
 */
public class HeaderFollowStrategy extends HeaderStrategy {


    public HeaderFollowStrategy(PullToRefreshLayout pullToRefreshLayout) {
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
        if(null != headerView){
            headerView.layout(0,-headerView.getMeasuredHeight(),headerView.getMeasuredWidth(),0);
        }
        View refreshView = pullToRefreshLayout.getRefreshView();
        refreshView.layout(0,0,right,bottom);
    }

    @Override
    public void onMoveOffset(float distanceY) {
        PullToRefreshLayout pullToRefreshLayout = getPullToRefreshLayout();
        RefreshHeader refreshHeader = pullToRefreshLayout.getRefreshHeader();
        int headerHeight = refreshHeader.getHeaderHeight();
        float resistance = pullToRefreshLayout.getResistance();
        float pullMaxHeight = pullToRefreshLayout.getPullMaxHeight();

        int scrollY = Math.abs(pullToRefreshLayout.getScrollY());
        int moveDistanceY = (int) (distanceY / resistance);
        if (distanceY>0&&pullMaxHeight<=scrollY) moveDistanceY=0;
        float fraction = scrollY * 1.0f / headerHeight;
        pullToRefreshLayout.scrollBy(0, -moveDistanceY);

        if(1.0f<=fraction) fraction=1.0f;
        pullToRefreshLayout.refreshStateChanged(fraction);
        refreshHeader.onRefreshOffset(fraction,scrollY,headerHeight);
    }

    @Override
    public void onResetRefresh(RefreshState refreshState) {
        PullToRefreshLayout pullToRefreshLayout = getPullToRefreshLayout();
        RefreshHeader refreshHeader = pullToRefreshLayout.getRefreshHeader();
        int scrollDuration = pullToRefreshLayout.getScrollDuration();

        int scrollY = pullToRefreshLayout.getScrollY();
        if(RefreshState.NONE==refreshState){
            pullToRefreshLayout.scrollTo(0,0);
        } else if(RefreshState.PULL_START==refreshState){
            pullToRefreshLayout.setReleasing(true);
            pullToRefreshLayout.startScroll(0, scrollY, 0, -scrollY,scrollDuration);
            pullToRefreshLayout.postInvalidate();
        } else if(RefreshState.RELEASE_REFRESHING_START==refreshState){
            VelocityTracker velocityTracker = pullToRefreshLayout.getVelocityTracker();
            int scaledMinimumFlingVelocity = pullToRefreshLayout.getScaledMinimumFlingVelocity();
            float yVelocity = velocityTracker.getYVelocity();
            if(yVelocity>scaledMinimumFlingVelocity){
                //scroll
                View refreshHeaderView = refreshHeader.getRefreshHeaderView();
                int measuredHeight = refreshHeaderView.getMeasuredHeight();
                pullToRefreshLayout.startScroll(0, scrollY, 0, -scrollY-measuredHeight,scrollDuration);
                pullToRefreshLayout.setRefreshState(RefreshState.START_REFRESHING);
                refreshHeader.onRefreshStateChange(RefreshState.START_REFRESHING);
            } else {
                pullToRefreshLayout.startScroll(0, scrollY, 0, -scrollY,scrollDuration);
            }
        } else if(RefreshState.RELEASE_START==refreshState||RefreshState.START_REFRESHING==refreshState){
            View refreshHeaderView = refreshHeader.getRefreshHeaderView();
            int measuredHeight = refreshHeaderView.getMeasuredHeight();
            pullToRefreshLayout.startScroll(0, scrollY, 0, -scrollY-measuredHeight,scrollDuration);
            pullToRefreshLayout.setRefreshState(RefreshState.START_REFRESHING);
            refreshHeader.onRefreshStateChange(RefreshState.START_REFRESHING);
        }
    }

    @Override
    public void onRefreshComplete() {
        PullToRefreshLayout pullToRefreshLayout = getPullToRefreshLayout();
        RefreshState refreshState = pullToRefreshLayout.getRefreshState();
        if(RefreshState.START_REFRESHING==refreshState){
            int scrollX = pullToRefreshLayout.getScrollX();
            int scrollY = pullToRefreshLayout.getScrollY();
            //orientation
            int scrollDuration = pullToRefreshLayout.getScrollDuration();
            pullToRefreshLayout.startScroll(0,scrollY,0,-scrollY,scrollDuration);
            pullToRefreshLayout.requestLayout();
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
                pullToRefreshLayout.startScroll(0,0,0,headerHeight,pullToRefreshLayout.getScrollDuration());
            } else {
                pullToRefreshLayout.scrollTo(0,-headerHeight);
            }
        }
        pullToRefreshLayout.setRefreshState(RefreshState.START_REFRESHING);
    }

    @Override
    public boolean isMoveToTop() {
        PullToRefreshLayout pullToRefreshLayout = getPullToRefreshLayout();
        int scrollY = pullToRefreshLayout.getScrollY();
        return Math.abs(scrollY)<30;
    }

    @Override
    public boolean isIntercept(float distanceY) {
        PullToRefreshLayout pullToRefreshLayout = getPullToRefreshLayout();
        boolean isTop = pullToRefreshLayout.isChildScrollToTop();
        return (isTop && distanceY > 0 || pullToRefreshLayout.getScrollY() < -30);
    }
}
