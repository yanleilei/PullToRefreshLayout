package cz.pulltorefresh.header;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import cz.library.PullToRefreshLayout;
import cz.library.header.FlipHeader;
import cz.library.header.MaterialHeader;
import cz.library.headerstrategy.HeaderFollowStrategy;
import cz.library.headerstrategy.HeaderFrontStrategy;
import cz.pulltorefresh.R;
import cz.pulltorefresh.SimpleAdapter;
import cz.pulltorefresh.StringItems;

/**
 * Created by Administrator on 2016/8/15.
 */
public class HeaderFrontActivity extends AppCompatActivity {
    public static final String TAG = "HeaderFrontActivity";
    private int startIndex;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_refresh_recycler);
        setTitle(getIntent().getStringExtra("title"));

        final PullToRefreshLayout layout= (PullToRefreshLayout) findViewById(R.id.layout);
        layout.setRefreshHeader(new MaterialHeader(this,layout));
        layout.setHeaderStrategy(new HeaderFrontStrategy(layout));
        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.list);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        final SimpleAdapter adapter=new SimpleAdapter(this, StringItems.ITEMS);
        recyclerView.setAdapter(adapter);

        layout.setOnPullToRefreshListener(new PullToRefreshLayout.OnPullToRefreshListener() {
            @Override
            public void onRefresh() {
                layout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        adapter.addItems(StringItems.getNewItems(startIndex,5));
                        layout.onRefreshComplete();
                        startIndex+=5;
                    }
                },3*1000);
            }
        });
    }
}
