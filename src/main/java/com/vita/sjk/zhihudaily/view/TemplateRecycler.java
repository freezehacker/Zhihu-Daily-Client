package com.vita.sjk.zhihudaily.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vita.sjk.zhihudaily.R;
import com.vita.sjk.zhihudaily.bean.Story;
import com.vita.sjk.zhihudaily.utils.BitmapUtils;
import com.vita.sjk.zhihudaily.utils.CacheUtils;
import com.vita.sjk.zhihudaily.utils.LogUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

/**
 * Created by sjk on 2016/6/3.
 * <p>
 * 继承View或者Fragment，这里选择Fragment试试
 * 关键：根据定制（抽象）的程度来编写
 */
public class TemplateRecycler extends Fragment {

    public static final int RESOURCE_LAYOUT_ID = R.layout.fragment_list_item;   // 该fragment使用的资源文件

    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private TemplateAdapter adapter;

    private int lastVisibleItemPos = 0;

    private List<Story> mStories;

    /**
     * 3种监听器，对应用户对RecyclerView的3种操作
     * "定制"
     */
    private OnLoadMoreListener mOnLoadMoreListener = null;
    private OnDataRefreshListener mOnDataRefreshListener = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.template_recycler, container, false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setColorSchemeResources(
                R.color.material_red,
                R.color.material_orange,
                R.color.material_yellow,
                R.color.material_green,
                R.color.material_cyan,
                R.color.material_blue,
                R.color.material_purple,
                R.color.material_light_blue,
                R.color.material_blue_gray,
                R.color.material_gray,
                R.color.material_brown
        );

        /**
         * 为RecyclerView默认样式，不用每次都要去定义
         */
        mLinearLayoutManager = new LinearLayoutManager(getActivity());
        mLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        //mRecyclerView.setHasFixedSize(true);  //提高性能
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        /**
         * "下拉刷新"监听器
         * （BTW，绑定用户要求的监听器之前，最好先判断一下用户有没有绑定，如果没有就不用设置了）
         */
        if (mOnDataRefreshListener != null) {
            mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    /**
                     * 回调用户定义的"刷新"操作（接口的定义：此处定义回调时机，具体回调什么内容由用户定义）
                     */
                    mOnDataRefreshListener.onDataRefresh();
                }
            });
        }

        /**
         * "上拉加载"监听器
         */
        if (mOnLoadMoreListener != null) {
            mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                /**
                 * 只有当状态改变的时候才回调
                 * @param recyclerView
                 * @param newState
                 */
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    if (newState == RecyclerView.SCROLL_STATE_IDLE && adapter.getItemCount() == lastVisibleItemPos + 1) {
                        /**
                         * 回调用户定义的"加载更多"操作（接口的定义：此处定义回调时机，具体回调什么内容由用户定义）
                         */
                        mOnLoadMoreListener.onLoadMore();
                    }
                }

                /**
                 * 在滚动中不停回调
                 * @param recyclerView
                 * @param dx
                 * @param dy
                 */
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    lastVisibleItemPos = mLinearLayoutManager.findLastVisibleItemPosition();
                }
            });
        }

        /**
         * 本来应该是第一次进入就刷新界面的
         * 但是刷新界面的操作应该由用户定义
         * 这里不写
         * 定义2种操作接口（具体是3个函数，见下面）就好，一个增加，一个刷新，参数都是dataSource
         */
    }

    /**
     * 给用户调用，绑定一个"上拉加载"操作
     *
     * @param listener
     */
    public void setOnLoadMoreListener(OnLoadMoreListener listener) {
        mOnLoadMoreListener = listener;
    }

    /**
     * 给用户调用，绑定一个"下拉刷新"操作
     *
     * @param listener
     */
    public void setOnDataRefreshListener(OnDataRefreshListener listener) {
        mOnDataRefreshListener = listener;
    }

    /**
     * 给用户调用，绑定一个item点击的操作（一般是跳转到显示新闻的activity）
     * 这里有点特殊，因为onItemClickListener是定义在adapter里面的
     * 所以在函数里是给adapter绑定而不是recyclerView
     * 不过封装起来了，用户也看不到，只能看到一大'垢'recyclerView
     * 唯觉聪明如我(>_<)
     *
     * @param listener
     */
    public void setOnItemClickListener(TemplateAdapter.OnItemClickListener listener) {
        adapter.setOnItemClickListener(listener);
    }

    /**
     * 定义"上拉加载更多"接口
     */
    public interface OnLoadMoreListener {
        void onLoadMore();
    }

    /**
     * 定义"下拉刷新数据"接口
     */
    public interface OnDataRefreshListener {
        void onDataRefresh();
    }

    /**
     * 给用户调用的
     * 第一次载入数据，需要根据数据源，更新整个列表。adapter需要被创建
     * @param newStoryList
     */
    public void loadDataFirstly(List<Story> newStoryList) {
        mStories = newStoryList;
        adapter = new TemplateAdapter(getActivity(), RESOURCE_LAYOUT_ID, newStoryList, mRecyclerView);
        mRecyclerView.setAdapter(adapter);
    }

    /**
     * 给用户调用的
     * 后续的刷新数据。根据数据源，更新整个列表
     * @param newStoryList
     */
    public void loadData(List<Story> newStoryList) {
        mStories.clear();
        mStories.addAll(newStoryList);
        adapter.notifyDataSetChanged();
    }

    /**
     * 给用户调用的
     * 根据数据源，增加（后接）一个列表
     * @param moreStoryList
     */
    public void addData(List<Story> moreStoryList) {
        int originSize = mStories.size();
        mStories.addAll(moreStoryList);
        adapter.notifyItemRangeInserted(originSize, moreStoryList.size());
    }

    /**
     * 自定义Adapter
     */
    static class TemplateAdapter extends RecyclerView.Adapter<TemplateAdapter.TemplateViewHolder> {

        OnItemClickListener mOnItemClickListener = null;
        List<Story> mStoryList;
        Context mContext;
        int mResourceId;

        /**
         * 保留一个recyclerView的引用，是为了到时候用findViewWithTag()
         */
        RecyclerView mRecyclerView;

        /**
         * 构造器，传入数据源等
         */
        public TemplateAdapter(Context context, int resourceId, List<Story> inList, RecyclerView recyclerView) {
            mContext = context;
            mResourceId = resourceId;
            mStoryList = inList;
            mRecyclerView = recyclerView;
        }

        /**
         * 该方法是重用View，所以不会一直调用
         *
         * @param parent
         * @param viewType
         * @return
         */
        @Override
        public TemplateViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext).inflate(mResourceId, parent, false);
            TemplateViewHolder ret = new TemplateViewHolder(view);
            return ret;
        }

        /**
         * 最关键的回调函数
         * 此函数里要处理的问题有：
         * 显示item内容
         * 监听器的绑定
         * 字体的特殊显示（是否被用户浏览过）
         *
         * @param holder
         * @param position
         */
        @Override
        public void onBindViewHolder(TemplateViewHolder holder, final int position) {
            Story story = mStoryList.get(position);

            holder.item_title.setText(story.getTitle());
            /**
             * 这里不论如何都先设置一张"空"图片，是有效防止图片乱序的方法之一
             */
            holder.item_thumbnail.setImageResource(R.drawable.no_pic);
            /**
             * 再啰嗦一遍，这里要传入一个String类型的tag而不是long等基本类型
             */
            holder.item_thumbnail.setTag(String.valueOf(story.getId()));
            //Bitmap emptyThumbnail = BitmapUtils.getResizedBitmap(mContext.getResources(), R.drawable.no_pic, IMAGEVIEW_SIZE, IMAGEVIEW_SIZE);
            if (story.getImages() != null) {
                String urlString = story.getImages().get(0);
                Bitmap bitmap = CacheUtils.load(urlString);
                /**
                 * 如果缓存中有，就设置图片
                 * 如果没有，就启动AsyncTask下载封面图片，并且缓存（缓存操作在AsyncTask里面了）
                 */
                if (bitmap != null) {
                    holder.item_thumbnail.setImageBitmap(bitmap);
                } else {
                    new TemplateAsyncTask(story.getId()).execute(urlString);
                }
            }

            /**
             * "点击item"监听器
             */
            if (mOnItemClickListener != null) {
                holder.item_body.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mOnItemClickListener.onItemClick(v, position);
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return mStoryList.size();
        }

        /**
         * 然而并不明白这个回调函数的作用是什么……只要返回"唯一"的字段就可以了吗？
         *
         * @param position
         * @return
         */
        @Override
        public long getItemId(int position) {
            return mStoryList.get(position).getId();
        }

        /**
         * 在外部设置一个点击监听器
         *
         * @param listener
         */
        public void setOnItemClickListener(OnItemClickListener listener) {
            mOnItemClickListener = listener;
        }

        // 点击item的监听器和其回调
        interface OnItemClickListener {
            void onItemClick(View view, int position);
        }


        /**
         * 自定义ViewHolder
         */
        static class TemplateViewHolder extends RecyclerView.ViewHolder {

            TextView item_title;
            ImageView item_thumbnail;
            LinearLayout item_body;

            // 可以在这个函数里面就获取View了
            public TemplateViewHolder(View itemView) {
                super(itemView);
                item_title = (TextView) itemView.findViewById(R.id.f_item_title);
                item_thumbnail = (ImageView) itemView.findViewById(R.id.f_item_thumbnail);
                item_body = (LinearLayout) itemView.findViewById(R.id.f_item_body);
            }
        }

        /**
         * 自定义AsyncTask，加载每一个item中的封面（item_thumbnail）
         * 说明一下：这里是轻量级的图片，不需要用到线程池，AsyncTask就够了
         * 后期可能做成是static类，保留一个WeakReference<Class>
         */
        class TemplateAsyncTask extends AsyncTask<String, Void, Bitmap> {

            /**
             * 用来作为ImageView的Tag
             * 新闻的id
             */
            long newsId;

            /**
             * 用来作为Bitmap的存储的key
             * 新闻的封面图片的url，注意和上面的id的区别
             */
            String key;

            /**
             * 构造器，传入该新闻的id去代表这张封面
             */
            public TemplateAsyncTask(long id) {
                newsId = id;
            }

            @Override
            protected Bitmap doInBackground(String... params) {
                key = params[0];

                Bitmap ret = null;
                InputStream is = null;
                try {
                    is = new URL(params[0]).openStream();
                    ret = BitmapFactory.decodeStream(is);
                } catch (Exception e) {
                    LogUtils.log(e.getMessage());
                } finally {
                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                return ret;
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                if (bitmap != null) {
                    /**
                     * 这里千万要注意！
                     * 虽然id是long类型，但Tag最好传入String类型
                     * 实践证明如果传入long类型，会出现找不到ImageView的错误结果
                     */
                    String idString = String.valueOf(newsId);
                    ImageView iv = (ImageView) mRecyclerView.findViewWithTag(idString);
                    if (iv != null) {
                        /**
                         * 显示封面，相当于局部刷新，不用调用adapter的notifyXXChanged()
                         */
                        iv.setImageBitmap(bitmap);

                        /**
                         * 图片显示出来之后，记得加入缓存哦~
                         */
                        CacheUtils.dumpToDisk(key, bitmap);
                        CacheUtils.dumpToMemory(key, bitmap);
                    }
                }
            }
        }
    }
}
