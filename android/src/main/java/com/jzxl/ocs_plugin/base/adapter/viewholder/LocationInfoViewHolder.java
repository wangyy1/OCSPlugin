package com.jzxl.ocs_plugin.base.adapter.viewholder;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.jzxl.ocs_plugin.entity.LocationInfo;
import com.jzxl.ocs_plugin.R;
import com.jzxl.ocs_plugin.base.adapter.BaseListAdapter;

/**
 * @author wangyongyong
 * @date
 * @Description
 */

public class LocationInfoViewHolder extends BaseListAdapter.ViewHolder<LocationInfo> {
    private TextView mLocationName;
    private TextView mLocationAddress;
    private ImageView mLocationImg;
    private Context mContext;

    public LocationInfoViewHolder(View itemView) {
        super(itemView);
        mContext = itemView.getContext();
        mLocationName = (TextView) itemView.findViewById(R.id.item_location_info_name);
        mLocationAddress = (TextView) itemView.findViewById(R.id.item_location_info_address);
        mLocationImg = (ImageView) itemView.findViewById(R.id.item_location_info_img);
    }

    @Override
    public void setData(LocationInfo data) {
        mLocationName.setText(data.getName());
        mLocationAddress.setText(data.getAddress());
        if (data.isSelected()) {
            mLocationName.setTextColor(mContext.getResources().getColor(R.color.colorPrimary));
            mLocationAddress.setTextColor(mContext.getResources().getColor(R.color.colorPrimary));
            mLocationImg.setVisibility(View.VISIBLE);
        } else {
            mLocationName.setTextColor(Color.BLACK);
            mLocationAddress.setTextColor(mContext.getResources().getColor(R.color.color_999999));
            mLocationImg.setVisibility(View.INVISIBLE);
        }
    }
}
