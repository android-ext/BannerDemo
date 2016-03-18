package krelve.view;

import android.content.Context;
import android.media.Image;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

import java.util.ArrayList;
import java.util.List;

public class Kanner extends FrameLayout {
    private static final String TAG = "Kanner";
    private int count;
    private ImageLoader mImageLoader;
    /** 轮播图片集合 */
    private List<ImageView> imageViews;
    private Context context;
    private ViewPager vp;
    /** 是否自动播放 */
    private boolean isAutoPlay;
    private int currentItem;
    private int delayTime;
    /** 圆点容器 */
    private LinearLayout ll_dot;
    /** 圆点图片集合 */
    private List<ImageView> iv_dots;
    private Handler handler = new Handler();
    private DisplayImageOptions options;

    public Kanner(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        initImageLoader(context);
        initData();
    }

    public Kanner(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Kanner(Context context) {
        this(context, null);
    }

    private void initData() {
        imageViews = new ArrayList<ImageView>();
        iv_dots = new ArrayList<ImageView>();
        delayTime = 2000;
    }

    public void setImagesUrl(String[] imagesUrl) {
        initLayout();
        initImgFromNet(imagesUrl);
        showTime();
    }

    public void setImagesRes(int[] imagesRes) {
        initLayout();
        initImgFromRes(imagesRes);
        showTime();
    }

    private void initLayout() {

        // 初始化ViewPager 和 圆点容器控件
        View view = LayoutInflater.from(context).inflate( R.layout.kanner_layout, this, true);  // true
        vp = (ViewPager) view.findViewById(R.id.vp);
        ll_dot = (LinearLayout) view.findViewById(R.id.ll_dot);

        // 清空轮播图片集合
        imageViews.clear();
        // 清空圆点图片集合
        ll_dot.removeAllViews();
    }

    /**
     * @description: 初始化轮播图片和小圆点数据
     * @author: Ext
     * @time: 2016/3/18 15:27
     */
    private void initImgFromRes(final int[] imagesRes) {
        count = imagesRes.length;
        /** 添加圆点图片 */
        for (int i = 0; i < count; i++) {
            ImageView iv_dot = new ImageView(context);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.leftMargin = 5;
            params.rightMargin = 5;
            iv_dot.setImageResource(R.drawable.dot_blur);
            ll_dot.addView(iv_dot, params);
            iv_dots.add(iv_dot);
        }
        iv_dots.get(0).setImageResource(R.drawable.dot_focus);

        /** 添加轮播图片 */
        for (int i = 0; i <= count + 1; i++) {
            ImageView iv = new ImageView(context);
            iv.setTag(i);
            iv.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (v instanceof ImageView) {
                        ImageView imageView = (ImageView)v;
                        pageItemClickListener.pageItemClick(Kanner.this, (int)imageView.getTag());
                    }
                }
            });
            iv.setScaleType(ScaleType.FIT_XY);
//            iv.setBackgroundResource(R.drawable.loading);
            if (i == 0) {
                iv.setImageResource(imagesRes[count - 1]);
            } else if (i == count + 1) {
                iv.setImageResource(imagesRes[0]);
            } else {
                iv.setImageResource(imagesRes[i - 1]);
            }
            imageViews.add(iv);
        }
    }

    private void initImgFromNet(String[] imagesUrl) {
        count = imagesUrl.length;
        // 加载圆点图片
        for (int i = 0; i < count; i++) {
            ImageView iv_dot = new ImageView(context);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.leftMargin = 5;
            params.rightMargin = 5;
            iv_dot.setImageResource(R.drawable.dot_blur);
            ll_dot.addView(iv_dot, params);
            iv_dots.add(iv_dot);
        }
        iv_dots.get(0).setImageResource(R.drawable.dot_focus);

        // 加载网络图片
        for (int i = 0; i <= count + 1; i++) {
            ImageView iv = new ImageView(context);
            iv.setScaleType(ScaleType.FIT_XY);
//            iv.setBackgroundResource(R.mipmap.loading);
            if (i == 0) {
                mImageLoader.displayImage(imagesUrl[count - 1], iv, options);
            } else if (i == count + 1) {
                mImageLoader.displayImage(imagesUrl[0], iv, options);
            } else {
                mImageLoader.displayImage(imagesUrl[i - 1], iv, options);
            }
            imageViews.add(iv);
        }
    }

    private void showTime() {
        vp.setAdapter(new KannerPagerAdapter());
        vp.setFocusable(true);
        vp.setCurrentItem(1);
        currentItem = 1;
        vp.addOnPageChangeListener(new MyOnPageChangeListener());
        startPlay();
    }

    private void startPlay() {
        isAutoPlay = true;
        handler.postDelayed(task, 2000);
    }

    public void initImageLoader(Context context) {
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                context).threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .diskCacheFileNameGenerator(new Md5FileNameGenerator())
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .writeDebugLogs().build();
        ImageLoader.getInstance().init(config);
        options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();
        mImageLoader = ImageLoader.getInstance();
    }

    private final Runnable task = new Runnable() {

        @Override
        public void run() {
            if (isAutoPlay) {
                currentItem = currentItem % (count + 1) + 1;
                if (currentItem == 1) {
                    vp.setCurrentItem(currentItem, false);
                    handler.post(task);
                } else {
                    vp.setCurrentItem(currentItem);
                    handler.postDelayed(task, 3000);
                }
            } else {
                handler.postDelayed(task, 5000);
            }
        }
    };

    class KannerPagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return imageViews.size();
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(imageViews.get(position));
            return imageViews.get(position);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(imageViews.get(position));
        }

    }

    class MyOnPageChangeListener implements OnPageChangeListener {

        @Override
        public void onPageScrollStateChanged(int state) {
            switch (state) {
                case ViewPager.SCROLL_STATE_DRAGGING:
                    isAutoPlay = false;
                    break;
                case ViewPager.SCROLL_STATE_SETTLING:
                    isAutoPlay = true;
                    break;
                case ViewPager.SCROLL_STATE_IDLE: // 停止在某页
                    if (vp.getCurrentItem() == 0) {
                        vp.setCurrentItem(count, false);
                    } else if (vp.getCurrentItem() == count + 1) {
                        vp.setCurrentItem(1, false);
                    }
                    currentItem = vp.getCurrentItem();
                    isAutoPlay = true;
                    break;
            }
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            Log.i(TAG, "onPageScrolled = " + position);
        }

        @Override
        public void onPageSelected(int position) {
            Log.i(TAG, "onPageSelected = " + position);
            for (int i = 0; i < iv_dots.size(); i++) {
                if (i == position - 1) {
                    iv_dots.get(i).setImageResource(R.drawable.dot_focus);
                } else {
                    iv_dots.get(i).setImageResource(R.drawable.dot_blur);
                }
            }
        }
    }

    private PageItemClickListener pageItemClickListener;

    public void setPageItemClickListener(PageItemClickListener pageItemClickListener) {
        this.pageItemClickListener = pageItemClickListener;
    }

    /**
     * @description: 轮播页面单击事件监听接口
     * @author: Ext
     * @time: 2016/3/18 18:21
     */
    public interface PageItemClickListener {
        void pageItemClick(Kanner kanner, int position);
    }
}
