package cz.pulltorefresh;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import cz.pulltorefresh.callback.OnItemClickListener;
import cz.pulltorefresh.header.HeaderFollowActivity;
import cz.pulltorefresh.header.HeaderFrontActivity;
import cz.pulltorefresh.header.HeaderOverlapActivity;
import cz.pulltorefresh.header.HeaderScrollActivity;

/**
 * Created by Administrator on 2016/8/15.
 */
public class HeaderItemsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RecyclerView recyclerView= (RecyclerView) findViewById(R.id.list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        final SimpleAdapter adapter = SimpleAdapter.createFromResource(this, R.array.header_items);
        recyclerView.setAdapter(adapter);
        final Class<?>[] classes=new Class[]{HeaderFrontActivity.class,HeaderOverlapActivity.class, HeaderFollowActivity.class, HeaderScrollActivity.class};
        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Object item = adapter.getItem(position);
                Intent intent = new Intent(getBaseContext(), classes[position]);
                intent.putExtra("title",item.toString());
                startActivity(intent);
            }
        });
    }
}
