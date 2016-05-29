package com.vita.sjk.zhihudaily.adapter;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.vita.sjk.zhihudaily.R;
import com.vita.sjk.zhihudaily.bean.Story;
import com.vita.sjk.zhihudaily.utils.LogUtils;

import java.util.List;
import java.util.zip.Inflater;

/**
 * Created by sjk on 2016/5/27.
 */
public class FirstAdapter extends RecyclerView.Adapter<FirstAdapter.FirstViewHolder> {

    /**
     * RecyclerView的item点击，需要自己定义点击的回调来实现
     */
    public interface OnItemClickListener {
        void onItemClick(View v, int position);
    }

    OnItemClickListener mOnItemClickListener;
    List<Story> storyList;
    int resourceId;
    Context mContext;

    /**
     * 构造器，目的是传入需要的参数（引用）
     *
     * @param context
     * @param rId     资源文件，确定要使用哪个item布局（其实我觉得已经是确定了的……）
     * @param inList
     */
    public FirstAdapter(Context context, int rId, List<Story> inList) {
        mContext = context;
        resourceId = rId;
        storyList = inList;
    }

    /**
     * 设置监听器
     * @param listener
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    /**
     * 该方法是重用View，所以不会一直调用
     * 当有n个item时，该函数只会回调m（m << n）次，比如测试n=20，那大概m=8
     * @param parent
     * @param viewType
     * @return
     */
    @Override
    public FirstViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //LogUtils.log("create");
        View view = LayoutInflater.from(mContext).inflate(resourceId, parent, false);
        return new FirstViewHolder(view);
    }

    /**
     * 该方法会一直调用，具体地说，哪个item被显示在屏幕上，就调用哪个item的这个回调
     * 要设置文字的颜色，等等，比如已经浏览过了就得设置成灰色文字
     * 而且要考虑异步回调的问题
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(FirstViewHolder holder, final int position) {
        //LogUtils.log("bind " + position);
        Story story = storyList.get(position);
        holder.item_title.setText(story.getTitle());
        //holder.item_thumbnail.setImageBitmap(bitmap);

        /**
         * 设置点击监听器，如果用户设置了的话
         */
        if (mOnItemClickListener != null) {
            holder.item_card_view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickListener.onItemClick(v, position);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return storyList.size();
    }

    /**
     * ViewHolder类
     */
    public static class FirstViewHolder extends RecyclerView.ViewHolder {

        CardView item_card_view;

        TextView item_title;

        ImageView item_thumbnail;

        public FirstViewHolder(View itemView) {
            super(itemView);
            item_title = (TextView) itemView.findViewById(R.id.item_title);
            item_thumbnail = (ImageView) itemView.findViewById(R.id.item_thumbnail);
            item_card_view=(CardView)itemView.findViewById(R.id.item_card_view);
        }
    }
}
