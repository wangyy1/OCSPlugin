package com.jzxl.ocs_plugin.base.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.jzxl.ocs_plugin.entity.LocationInfo;
import com.jzxl.ocs_plugin.R;
import com.jzxl.ocs_plugin.base.adapter.viewholder.LocationInfoViewHolder;

import java.util.List;

/**
 * @author wangyongyong
 * @date
 * @Description
 */

public class LocationAdapter extends BaseListAdapter<LocationInfo, LocationInfoViewHolder> {

    public LocationAdapter(List<LocationInfo> data) {
        super(data);
    }

    @Override
    protected LocationInfoViewHolder createHolder(int position, ViewGroup parent) {
        return new LocationInfoViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_location_info_list, parent, false));
    }

    @Override
    protected void bindData(int position, LocationInfoViewHolder holder, LocationInfo data) {
        holder.setData(data);
    }
}
