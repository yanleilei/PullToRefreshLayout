package cz.library.header;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import cz.library.RefreshState;
import cz.library.widget.MaterialProgressDrawable;
import cz.library.widget.MaterialProgressView;

/**
 * Created by Administrator on 2016/8/16.
 */
public class DisplayHeader extends RefreshHeader {
    private static final String TAG = "MaterialHeader";

    public DisplayHeader(Context context, ViewGroup parent) {
        super(context, parent);
        headerView=new FrameLayout(context);
    }

    public void setHeaderView(View view){
        FrameLayout layout = (FrameLayout) headerView;
        layout.removeAllViews();
        layout.addView(view,FrameLayout.LayoutParams.MATCH_PARENT,FrameLayout.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onRefreshOffset(float fraction, int height, int totalHeight) {
        //nothing to do
    }

    @Override
    public void onRefreshStateChange(RefreshState state) {
       //nothing to do
    }
}
