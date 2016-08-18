package cz.library.header;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.IntDef;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import cz.library.R;
import cz.library.RefreshState;

/**
 * Created by cz on 2016/8/13.
 */
public abstract class RefreshHeader {
    private static final String TAG = "RefreshHeader";
    protected  View headerView;

    public RefreshHeader(Context context, ViewGroup parent){
        if(null==context||null==parent){
            throw new NullPointerException("context or parent is null!");
        }
    }
    /**
     * return view refresh height,sometime you can return view height,but if you want other special effects,you can return other value
     * @return
     */
    public int getRefreshHeight(int height) {
        return height;
    }

    /**
     * return refresh header view
     * @return
     */
    public View getRefreshHeaderView(){
        return headerView;
    }

    public int getHeaderWidth(){
        return headerView.getMeasuredWidth();
    }

    public int getHeaderHeight(){
        return headerView.getMeasuredHeight();
    }


    /**
     * when user scroll return refresh offset value
     * @param fraction
     * @param height
     */
    public abstract void onRefreshOffset(float fraction,int height,int totalHeight);

    /**
     * when user refresh state changed callback
     * @param state
     */
    public abstract void onRefreshStateChange(RefreshState state);
}
