package cz.pulltorefresh;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import cz.library.anim.AnimatorCompat;
import cz.library.anim.AnimatorUpdateListener;
import cz.pulltorefresh.callback.OnItemClickListener;
import cz.pulltorefresh.header.HeaderFollowActivity;
import cz.pulltorefresh.header.HeaderFrontActivity;
import cz.pulltorefresh.header.HeaderOverlapActivity;
import cz.pulltorefresh.header.HeaderScrollActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RecyclerView recyclerView= (RecyclerView) findViewById(R.id.list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        final SimpleAdapter adapter = SimpleAdapter.createFromResource(this, R.array.header_items);
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
        recyclerView.setAdapter(adapter);

    }


}
