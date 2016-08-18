package cz.pulltorefresh.header;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import cz.library.PullToRefreshLayout;
import cz.library.header.FlipHeader;
import cz.library.headerstrategy.HeaderOverlapStrategy;
import cz.pulltorefresh.R;
import cz.pulltorefresh.SimpleAdapter;
import cz.pulltorefresh.StringItems;

/**
 * Created by Administrator on 2016/8/15.
 */
public class HeaderScrollActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scroll_recycler);
        setTitle(getIntent().getStringExtra("title"));

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new SimpleAdapter(this, StringItems.ITEMS));
    }
}
