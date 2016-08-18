package cz.pulltorefresh;

import android.content.Context;
import android.support.annotation.ArrayRes;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cz.pulltorefresh.callback.OnItemClickListener;

/**
 * Created by cz on 16/1/23.
 */
public class SimpleAdapter<E> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private OnItemClickListener listener;
    private final LayoutInflater layoutInflater;
    private final ArrayList<E> items;
    private int layout;

    public static SimpleAdapter createFromResource(Context context, @ArrayRes int res) {
        return new SimpleAdapter(context, context.getResources().getStringArray(res));
    }


    public SimpleAdapter(Context context, E[] items) {
        this(context, android.R.layout.simple_list_item_1, Arrays.asList(items));
    }

    public SimpleAdapter(Context context, @LayoutRes int layout, E[] items) {
        this(context, layout, Arrays.asList(items));
    }

    public SimpleAdapter(Context context, List<E> items) {
        this(context,android.R.layout.simple_list_item_1,items);
    }

    public SimpleAdapter(Context context, @LayoutRes int layout, List<E> items) {
        this.layoutInflater=LayoutInflater.from(context);
        this.items=new ArrayList<>();
        this.layout = layout;
        if(null!=items){
            this.items.addAll(items);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(layout, parent, false);
        view.setBackgroundResource(R.drawable.item_selector);
        return new RecyclerView.ViewHolder(view){};
    }

    public E getItem(int position){
        return this.items.get(position);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder,final int position) {
        if(holder.itemView instanceof TextView){
            final TextView textView=((TextView)holder.itemView);
            textView.setText(getItem(position).toString());
            if(null!=listener){
                textView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        listener.onItemClick(textView,position);
                    }
                });
            }
        }
    }

    public void addItems(E[] items){
        this.items.addAll(0,Arrays.asList(items));
        notifyItemRangeInserted(0,items.length);
    }

    public void addItems(List<E> items){
        this.items.addAll(0,items);
        notifyItemRangeInserted(0,items.size());
    }


    @Override
    public int getItemCount() {
        return this.items.size();
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        this.listener=listener;
    }

}
