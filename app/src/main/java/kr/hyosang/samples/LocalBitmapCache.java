package kr.hyosang.samples;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

import kr.hyosang.common.BitmapCache;

/**
 * Created by Hyosang on 2016-02-22.
 */
public class LocalBitmapCache extends Activity {
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
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/and_image/";

        Log.d("TEST", "Path = " + path);
        File d = new File(path);

        Log.d("TEST", "Exists = " + d.exists());
        Log.d("TEST", "Dir = " + d.isDirectory());
        Log.d("TEST", "Can read = " + d.canRead());

        File [] ff = d.listFiles();

        Log.d("TEST", "ff = " + ff);
        for(File f : ff) {
            if(f.getName().toUpperCase().endsWith("JPG") || f.getName().toUpperCase().endsWith("PNG")) {
                mAdapter.add(f.getAbsolutePath());
            }
        }

        mAdapter.notifyDataSetChanged();
    }

    private class ListAdapter extends BaseAdapter {
        private ArrayList<String> mList = new ArrayList<String>();
        private BitmapCache mCache = new BitmapCache(LocalBitmapCache.this);

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
                convertView = LayoutInflater.from(LocalBitmapCache.this).inflate(R.layout.bitmap_list_item, parent, false);

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
