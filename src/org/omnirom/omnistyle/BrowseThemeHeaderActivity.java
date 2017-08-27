/*
 *  Copyright (C) 2017 The OmniROM Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.omnirom.omnistyle;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import org.omnirom.omnistyle.HeaderUtils.DaylightHeaderInfo;

public class BrowseThemeHeaderActivity extends Activity {
    private static final String TAG = "BrowseThemeHeaderActivity";
    private static final String DEFAULT_HEADER_PACKAGE = "com.android.systemui";
    private static final String STATUS_BAR_CUSTOM_HEADER_IMAGE = "status_bar_custom_header_image";
    private static final String STATUS_BAR_CUSTOM_HEADER_PROVIDER = "status_bar_custom_header_provider";

    private static final boolean DEBUG = true;
    private List<DaylightHeaderInfo> mHeadersList;
    private Resources mRes;
    private String mPackageName;
    private String mHeaderName;
    private ListView mHeaderListView;
    private HeaderListAdapter mHeaderListAdapter;
    private ProgressBar mProgress;
    private TextView mCreatorName;
    private String mCreatorLabel;
    private String mCreator;

    public class HeaderListAdapter extends ArrayAdapter<DaylightHeaderInfo> {
        private final LayoutInflater mInflater;

        public HeaderListAdapter(Context context) {
            super(context, R.layout.header_image, mHeadersList);
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            HeaderImageHolder holder = HeaderImageHolder.createOrRecycle(mInflater, convertView);
            convertView = holder.rootView;
            DaylightHeaderInfo di = mHeadersList.get(position);
            int resId = mRes.getIdentifier(di.mImage, "drawable", mPackageName);
            if (resId != 0) {
                holder.mHeaderImage.setImageDrawable(mRes.getDrawable(resId, null));
            } else {
                holder.mHeaderImage.setImageDrawable(null);
            }
            holder.mHeaderName.setText(di.mName != null ? di.mName : di.mImage);
            return convertView;
        }
    }

    public static class HeaderImageHolder {
        public View rootView;
        public ImageView mHeaderImage;
        public TextView mHeaderName;

        public static HeaderImageHolder createOrRecycle(LayoutInflater inflater, View convertView) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.header_image, null);
                HeaderImageHolder holder = new HeaderImageHolder();
                holder.rootView = convertView;
                holder.mHeaderImage = (ImageView) convertView.findViewById(R.id.header_image);
                holder.mHeaderName = (TextView) convertView.findViewById(R.id.header_name);
                convertView.setTag(holder);
                return holder;
            } else {
                // Get the ViewHolder back to get fast access to the TextView
                // and the ImageView.
                return (HeaderImageHolder) convertView.getTag();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_browse);

        getActionBar().setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_TITLE);

        mProgress = (ProgressBar) findViewById(R.id.browse_progress);
        mCreatorName = (TextView) findViewById(R.id.meta_creator);
        mCreatorLabel = getResources().getString(R.string.header_creator_label);
        Spinner s = (Spinner) findViewById(R.id.package_select);
        s.setVisibility(View.GONE);


        mHeadersList = new ArrayList<DaylightHeaderInfo>();
        mHeaderListView = (ListView) findViewById(R.id.package_images);
        mHeaderListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                DaylightHeaderInfo di = mHeadersList.get(i);
                Settings.System.putString(getContentResolver(), STATUS_BAR_CUSTOM_HEADER_IMAGE, mPackageName + "/" + di.mImage);
                Settings.System.putString(getContentResolver(), STATUS_BAR_CUSTOM_HEADER_PROVIDER, "static");
                Toast.makeText(BrowseThemeHeaderActivity.this, R.string.custom_header_image_notice, Toast.LENGTH_LONG).show();
            }
        });
        mHeaderListAdapter = new HeaderListAdapter(this);
        mHeaderListView.setAdapter(mHeaderListAdapter);
        loadThemeHeaderPackage();
        mHeaderListAdapter.notifyDataSetChanged();
        mCreatorName.setVisibility(TextUtils.isEmpty(mCreator) ? View.GONE : View.VISIBLE);
        mCreatorName.setText(mCreatorLabel + mCreator);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return true;
    }

    private void loadThemeHeaderPackage() {
        if (DEBUG) Log.i(TAG, "Load header pack ");
        mProgress.setVisibility(View.VISIBLE);
        mPackageName = "org.omnirom.omnistyle";
        mHeaderName = "org.omnirom.omnistyle.theme";

        try {
            PackageManager packageManager = getPackageManager();
            mRes = packageManager.getResourcesForApplication(mPackageName);
            mCreator = HeaderUtils.loadHeaders(mRes, mHeaderName, mHeadersList);
        } catch (Exception e) {
            Log.e(TAG, "Failed to load header pack " + mHeaderName, e);
            mRes = null;
        }
        mProgress.setVisibility(View.GONE);
    }
}
