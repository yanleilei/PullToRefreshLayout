package cz.library.headerstrategy;

import android.view.View;
import android.view.ViewGroup;

import cz.library.PullToRefreshLayout;
import cz.library.RefreshState;
import cz.library.header.RefreshHeader;

/**
 * Created by czz on 2016/8/16.
 */
public abstract class HeaderStrategy {
    private PullToRefreshLayout pullToRefreshLayout;
    public HeaderStrategy(PullToRefreshLayout pullToRefreshLayout){
        this.pullToRefreshLayout = pullToRefreshLayout;
    }

    protected PullToRefreshLayout getPullToRefreshLayout(){
        return pullToRefreshLayout;
    }

    /**
     * when set new header clear all refresh view
     */
    protected void swapRefreshHeaderView(View newHeaderView){
        PullToRefreshLayout pullToRefreshLayout = getPullToRefreshLayout();
        RefreshHeader refreshHeader = pullToRefreshLayout.getRefreshHeader();
        View refreshHeaderView = refreshHeader.getRefreshHeaderView();
        //remove header view
        if(null!=refreshHeaderView) {
            pullToRefreshLayout.removeView(refreshHeaderView);
        }
        pullToRefreshLayout.addView(newHeaderView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

    }

    public abstract void onInitRefreshHeader(RefreshHeader newHeader);

    public abstract void onLayout(boolean b, int left, int top, int right, int bottom);

    public abstract void onMoveOffset(float distanceY);

    public abstract void onResetRefresh(RefreshState refreshState);

    public abstract void onRefreshComplete();

    public abstract void autoRefreshing(boolean anim);

    public abstract boolean isMoveToTop();

    public abstract boolean isIntercept(float distanceY);


}
