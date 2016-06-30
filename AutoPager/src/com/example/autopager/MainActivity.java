package com.example.autopager;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.utils.StorageUtils;

/*
 * 图片轮播修改版--实现单张图片时不给滑动与轮播
 * **/
public class MainActivity extends Activity {
	AutoScrollViewPager mPosterPager;
	LinearLayout pointsLayout;
	PosterPagerAdapter adAdapter;
	// 循环间隔3秒
	private int interval = 3 * 1000;
	// 标记点集合
	private List<ImageView> points = null;
	// 当前轮播位置
	private int currentIndex = 0;
	String url[] = { 
			"http://img1.3lian.com/2015/w7/90/d/1.jpg",
			"http://img1.3lian.com/2015/w7/98/d/22.jpg",
			"http://img0.imgtn.bdimg.com/it/u=4168762024,1922499492&fm=21&gp=0.jpg" 
			};

	private OnTouchListener viewPagerOnTouch = new OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				mPosterPager.stopAutoScroll();
				break;

			case MotionEvent.ACTION_MOVE:
				tryAutoScroll();
				break;

			case MotionEvent.ACTION_UP:
				tryAutoScroll();
				break;

			default:
				break;
			}

			return false;
		}
	};

	/***
	 * 尝试开始自动滑动.
	 */
	private void tryAutoScroll() {
		if (adAdapter.dataList.size() > 1) {
			mPosterPager.startAutoScroll();
		} else {
			mPosterPager.stopAutoScroll();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		initImageLoader(this);
		
		for (int i = 0; i < url.length; i++) {
			list.add(url[i]);
		}
		pointsLayout = (LinearLayout) findViewById(R.id.activity_marketing_ad_indicator);
		mPosterPager = (AutoScrollViewPager) findViewById(R.id.activity_marketing_ad_viewpager);
		points = new ArrayList<ImageView>();
		initViewPager();
		initIndicator();
//		if (adAdapter != null) {
//			adAdapter.updateCarouselList(list);
//			adAdapter.notifyDataSetChanged();
//		}

	}

	@Override
	protected void onResume() {
		super.onResume();
		tryAutoScroll();
	}

	protected void onPause() {
		super.onPause();
		mPosterPager.stopAutoScroll();
	}

	ArrayList<String> list = new ArrayList<String>();

	private void initViewPager() {
		adAdapter = new PosterPagerAdapter(list);

		mPosterPager.setAdapter(adAdapter);
		mPosterPager.setInterval(interval);
		mPosterPager.setScrollDurationFactor(2f);
		mPosterPager.setOnTouchListener(viewPagerOnTouch);
		mPosterPager.setOnPageChangeListener(new PosterPageChange());
		mPosterPager
				.setSlideBorderMode(AutoScrollViewPager.SLIDE_BORDER_MODE_CYCLE);
	}

	private void initIndicator() {
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		lp.setMargins(3, 0, 3, 0);

		points.clear();
		pointsLayout.removeAllViews();

		int count = adAdapter.dataList.size();

		for (int i = 0; i < count; i++) {
			// 添加标记点
			ImageView point = new ImageView(this);

			boolean isCurrentIndex = i == (currentIndex % count);

			point.setBackgroundResource(isCurrentIndex ? R.drawable.icon_n
					: R.drawable.icon_o);

			point.setLayoutParams(lp);

			points.add(point);
			pointsLayout.addView(point);
		}
	}

	class PosterPagerAdapter extends PagerAdapter {
		private List<String> dataList;
		// 是否初始化
		private boolean isInit = false;

		public PosterPagerAdapter(List<String> list) {
			this.dataList = list;
		}

		/**
		 * 对轮播数据排序
		 * 
		 * @param list
		 */
		public void updateCarouselList(List<String> list) {
			if (list == null) {
				return;
			}
			dataList.clear();
			
			 Iterator<String> iterator = list.iterator();
			 String urlInfo=null;
			 while (iterator.hasNext())
	            {
				 urlInfo = iterator.next();
	           
	              dataList.add(urlInfo);
	             
	              urlInfo = null;
	            }
			// 更新数据后指示器重置.
			initIndicator();

			// 数据少的时候不滑动.
			tryAutoScroll();

			// 设置current position.
			if (dataList.size() > 1 && !isInit) {
				isInit = true;
				mPosterPager.setCurrentItem(dataList.size() * 5000, false);
			}
		}

		@Override
		public int getCount() {
			int size = dataList.size();
			if (size < 2) {
				return size;
			} else {
				return dataList.size() * 50000;
			}
		}

		DisplayImageOptions simpleOptions = new DisplayImageOptions.Builder()
				.showImageOnLoading(R.drawable.picture_bg_loding_3)
				.showImageForEmptyUri(R.drawable.picture_bg_loding_3)
				.showImageOnFail(R.drawable.picture_bg_loding_3)
				.cacheInMemory(true).cacheOnDisk(true).considerExifParams(true)
				.bitmapConfig(Bitmap.Config.RGB_565).build();

		@SuppressLint("NewApi")
		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			ImageView imageView = new ImageView(MainActivity.this);
			imageView.setAdjustViewBounds(true);
			imageView.setScaleType(ScaleType.CENTER_CROP);

			LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.WRAP_CONTENT);
			imageView.setLayoutParams(params);
			imageView.setOnTouchListener(viewPagerOnTouch);

			String info = dataList.get(position % dataList.size());
			// if (info.equals("marketing_curosue_default"))
			// {
			// imageView.setImageResource(R.drawable.marketing_curosue_default);
			// ((ViewPager) container).addView(imageView);
			// }
			// else
			// {
			ImageLoader.getInstance().displayImage(info, imageView,
					simpleOptions);
			((ViewPager) container).addView(imageView);
			//
			imageView.setOnClickListener(new PosterClickListener(1, position
					% dataList.size()));

			return imageView;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			((ViewPager) container).removeView((ImageView) object);
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}
	}

	class PosterPageChange implements OnPageChangeListener {
		@Override
		public void onPageScrollStateChanged(int arg0) {
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

		@Override
		public void onPageSelected(int position) {
			if (adAdapter.getCount() > 0) {
				currentIndex = position;
				updateIndicator(position);
			}
		}
	}

	private void updateIndicator(int position) {
		for (ImageView view : points) {
			view.setBackgroundResource(R.drawable.icon_o);
		}

		int index = position % adAdapter.dataList.size();
		points.get(index).setBackgroundResource(R.drawable.icon_n);
	}

	class PosterClickListener implements OnClickListener {
		/** 轮播 */
		private static final int CAROUSEL = 1;

		private int position;

		private int type;

		// 防止多次点击
		private boolean isRunning = false;

		public PosterClickListener(int type, int position) {
			this.type = type;
			this.position = position;
		}

		@Override
		public void onClick(View v) {
			if (isRunning) {
				return;
			}
			isRunning = true;
			if (type == CAROUSEL) {
				mPosterPager.stopAutoScroll();
				Toast.makeText(MainActivity.this, "--"+position, 0).show();

			}

			mPosterPager.postDelayed(new Runnable() {
				@Override
				public void run() {
					isRunning = false;
				}
			}, 1000);
		}
	}

	/**
	 * 初始化ImageLoader
	 */
	public static void initImageLoader(Context context) {
		File cacheDir = StorageUtils.getOwnCacheDirectory(context, "ab/cache");// 获取到缓存的目录地址
		Log.d("cacheDir", cacheDir.getPath() + "------->" + cacheDir);
		// 创建配置ImageLoader(所有的选项都是可选的,只使用那些你真的想定制)，这个可以设定在APPLACATION里面，设置为全局的配置参数
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
				context)
				// max width, max height，即保存的每个缓存文件的最大长宽
				.memoryCacheExtraOptions(480, 800)
				// Can slow ImageLoader, use it carefully (Better don't use
				// it)设置缓存的详细信息，最好不要设置这个
				.discCacheExtraOptions(480, 800, null)
				// 线程池内加载的数量
				.threadPoolSize(3)
				// 线程优先级
				.threadPriority(Thread.NORM_PRIORITY - 2)
				/*
				 * When you display an image in a small ImageView and later you
				 * try to display this image (from identical URI) in a larger
				 * ImageView so decoded image of bigger size will be cached in
				 * memory as a previous decoded image of smaller size. So the
				 * default behavior is to allow to cache multiple sizes of one
				 * image in memory. You can deny it by calling this method: so
				 * when some image will be cached in memory then previous cached
				 * size of this image (if it exists) will be removed from memory
				 * cache before.
				 */
				.denyCacheImageMultipleSizesInMemory()

				// You can pass your own memory cache
				// implementation你可以通过自己的内存缓存实现
				// .memoryCache(new UsingFreqLimitedMemoryCache(2 * 1024 *
				// 1024))
				// .memoryCacheSize(2 * 1024 * 1024)
				// 硬盘缓存50MB
				.diskCacheSize(50 * 1024 * 1024)
				// 将保存的时候的URI名称用MD5
				.diskCacheFileNameGenerator(new Md5FileNameGenerator())
				// 加密
				.diskCacheFileNameGenerator(new HashCodeFileNameGenerator())
				// 将保存的时候的URI名称用HASHCODE加密
				.tasksProcessingOrder(QueueProcessingType.LIFO)
				.diskCacheFileCount(100) // 缓存的File数量
				.diskCache(new UnlimitedDiscCache(cacheDir))// 自定义缓存路径
				// .defaultDisplayImageOptions(DisplayImageOptions.createSimple())
				// .imageDownloader(new BaseImageDownloader(context, 5 * 1000,
				// 30 * 1000)) // connectTimeout (5 s), readTimeout (30 s)超时时间
				.writeDebugLogs() // Remove for release app
				.build();
		// Initialize ImageLoader with configuration.
		ImageLoader.getInstance().init(config);// 全局初始化此配置
	}

}
