package kr.hyosang.samples;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

import kr.hyosang.common.BitmapCache;
import kr.hyosang.common.HttpImageCache;

/**
 * Created by Hyosang on 2016-02-24.
 */
public class HttpBitmapCache extends Activity {
    private ListView mListView;
    private ListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.local_bitmap_activity);

        mListView = (ListView) findViewById(R.id.bitmap_list);
        mAdapter = new ListAdapter();
        mListView.setAdapter(mAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        loadImages();
    }

    private void loadImages() {
        mAdapter.add("http://www.endlessicons.com/wp-content/uploads/2015/08/search-icon-2-214x214.png");
        mAdapter.add("http://www.endlessicons.com/wp-content/uploads/2014/11/wifi-icon-2-214x214.png");
        mAdapter.add("http://www.endlessicons.com/wp-content/uploads/2014/11/wifi-icon-1-214x214.png");
        mAdapter.add("http://www.endlessicons.com/wp-content/uploads/2014/09/time-stamp-icon-214x214.png");
        mAdapter.add("http://www.endlessicons.com/wp-content/uploads/2014/05/mortar-board-214x214.png");
        mAdapter.add("http://www.endlessicons.com/wp-content/uploads/2014/08/mortar-board-icon-2-214x214.png");
        mAdapter.add("http://www.endlessicons.com/wp-content/uploads/2014/05/global-icon-214x214.png");
        mAdapter.add("http://www.endlessicons.com/wp-content/uploads/2013/09/test-tube-214x214.png");
        mAdapter.add("http://www.endlessicons.com/wp-content/uploads/2013/08/round-eye-frame-icon-214x214.png");
        mAdapter.add("http://www.endlessicons.com/wp-content/uploads/2013/07/microphone-icon-214x214.png");
        mAdapter.add("http://www.endlessicons.com/wp-content/uploads/2013/07/street-name-post-icon-214x214.png");
        mAdapter.add("http://www.endlessicons.com/wp-content/uploads/2013/06/battery-icon-214x214.png");
        mAdapter.add("http://www.endlessicons.com/wp-content/uploads/2013/06/paper-plane-icon-214x214.png");
        mAdapter.add("http://www.endlessicons.com/wp-content/uploads/2013/09/paint-palette-icon-214x214.png");
        mAdapter.add("http://www.endlessicons.com/wp-content/uploads/2013/08/lab-beaker-icon-214x214.png");

        mAdapter.notifyDataSetChanged();
    }

    private class ListAdapter extends BaseAdapter {
        private ArrayList<String> mList = new ArrayList<String>();
        private HttpImageCache mCache = new HttpImageCache(HttpBitmapCache.this);

        public void add(String path) {
            mList.add(path);
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public String getItem(int position) {
            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;

            if(convertView == null) {
                convertView = LayoutInflater.from(HttpBitmapCache.this).inflate(R.layout.bitmap_list_item, parent, false);

                holder = new ViewHolder();
                holder.imageView = (ImageView) convertView.findViewById(R.id.bitmap_image);
                holder.txt = (TextView) convertView.findViewById(R.id.bitmap_txt);

                convertView.setTag(holder);
            }else {
                holder = (ViewHolder) convertView.getTag();
            }

            String path = getItem(position);

            holder.txt.setText(path);
            mCache.setBitmap(holder.imageView, path);

            return convertView;
        }
    }

    private class ViewHolder {
        public ImageView imageView;
        public TextView txt;
    }
}
