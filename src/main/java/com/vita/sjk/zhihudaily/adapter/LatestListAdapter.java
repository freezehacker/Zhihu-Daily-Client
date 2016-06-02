package com.vita.sjk.zhihudaily.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vita.sjk.zhihudaily.R;
import com.vita.sjk.zhihudaily.bean.Story;
import com.vita.sjk.zhihudaily.ui.fragment.LatestFragment;
import com.vita.sjk.zhihudaily.utils.CacheUtils;
import com.vita.sjk.zhihudaily.utils.LogUtils;

import java.net.URL;
import java.util.List;

/**
 * Created by sjk on 2016/6/2.
 */
public class LatestListAdapter extends RecyclerView.Adapter<LatestListAdapter.LatestListViewHolder> {

    private OnItemClickListener mOnItemClickListener;

    private List<Story> storyList;

    private int resourceId;

    private Context mContext;

    private RecyclerView mRecyclerView;

    /**
     * 构造器，目的是传入需要的参数（引用）
     *
     * @param context
     * @param rId     资源文件，确定要使用哪个item布局（其实我觉得已经是确定了的……）
     * @param inList
     */
    public LatestListAdapter(Context context, int rId, List<Story> inList, RecyclerView recyclerView) {
        mContext = context;
        resourceId = rId;
        storyList = inList;
        mRecyclerView = recyclerView;
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
    public LatestListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //LogUtils.log("create");
        View view = LayoutInflater.from(mContext).inflate(resourceId, parent, false);
        return new LatestListViewHolder(view);
    }

    /**
     * 该方法会一直调用，具体地说，哪个item被显示在屏幕上，就调用哪个item的这个回调
     * 要设置文字的颜色，等等，比如已经浏览过了就得设置成灰色文字
     * 而且要考虑异步回调的问题
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(LatestListViewHolder holder, final int position) {
        //LogUtils.log("bind " + position);
        Story story = storyList.get(position);
        holder.item_title.setText(story.getTitle());
        /**
         * 无论如何，刚进入屏幕的瞬间都先置为“空图片”
         */
        holder.item_thumbnail.setImageResource(R.drawable.no_pic);
        /**
         * 标记tag为url
         * 这里有3个url可以选择：图片的url，新闻的url，新闻的id
         * 这3个url都可以唯一标记一个新闻item对象，而新闻的id是新闻url的简化版本
         * 而鉴于有的新闻是没有图片url的
         * 所以，拿新闻的id作为显示封面的ImageView的TAG就最合适了
         *
         * 更新：setTag巨坑..
         * Tag最好是String类型，如果是long类型，后续的findViewWithTag会找不到T_T
         */
        holder.item_thumbnail.setTag(String.valueOf(story.getId()));

        List<String> imageStrs = story.getImages();
        if (imageStrs != null) {
            String imageStr = imageStrs.get(0); // 根据json字符串的格式说明，一般取第一个就可以了
            Bitmap bitmap = CacheUtils.load(imageStr);
            if (bitmap != null) {
                holder.item_thumbnail.setImageBitmap(bitmap);
            } else {
                /**
                 * 没有封面，就执行下载任务
                 */
                new ImageListTask(story.getId()).execute(imageStr);
            }
        }

        /**
         * 设置点击监听器，如果用户设置了的话
         */
        if (mOnItemClickListener != null) {
            holder.item_click_block.setOnClickListener(new View.OnClickListener() {
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
     * RecyclerView的item点击，需要自己定义点击的回调来实现
     */
    public interface OnItemClickListener {
        void onItemClick(View v, int position);
    }

    /**
     * RecyclerView实现上拉加载更多
     */
    public interface OnLoadMoreListener {
        void onLoadMore();
    }

    /**
     * RecyclerView中的列表项的图片下载任务
     */
    class ImageListTask extends AsyncTask<String, Integer, Bitmap> {

        long news_thumbnail_id;
        String urlString;

        /**
         * 传入一个新闻的id，来作为某张封面的tag部分
         * @param id
         */
        public ImageListTask(long id) {
            news_thumbnail_id = id;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            urlString = params[0];
            Bitmap ret = null;
            try {
                LogUtils.log("正在下载图片，url = " + urlString);
                ret = BitmapFactory.decodeStream(new URL(urlString).openStream());
                //LogUtils.log("下载完一个了");
            } catch (Exception e) {

            }
            return ret;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            /**
             * recyclerView.findViewWithTag找到ImageView来显示新闻封面
             * 这样可以解决异步加载图片过程中的图片乱序的问题
             */
            ImageView iv =(ImageView) mRecyclerView.findViewWithTag(String.valueOf(news_thumbnail_id));
            if (iv != null && bitmap != null) {
                //LogUtils.log("found!");
                iv.setImageBitmap(bitmap);

                /**
                 * 下载完后记得缓存
                 */
                CacheUtils.dumpToMemory(urlString, bitmap);
                CacheUtils.dumpToDisk(urlString, bitmap);
            } else {
                //LogUtils.log("not found: " + news_thumbnail_id);
            }
        }
    }


    /**
     * ViewHolder类
     */
    public static class LatestListViewHolder extends RecyclerView.ViewHolder {

        /**
         * 该linearLayout是用户点击到的区域，故以此命名
         */
        LinearLayout item_click_block;

        TextView item_title;

        ImageView item_thumbnail;

        public LatestListViewHolder(View itemView) {
            super(itemView);
            item_title = (TextView) itemView.findViewById(R.id.f_item_title);
            item_thumbnail = (ImageView) itemView.findViewById(R.id.f_item_thumbnail);
            item_click_block=(LinearLayout)itemView.findViewById(R.id.f_item_body);
        }
    }
}
