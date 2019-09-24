package com.jzxl.ocs_plugin.base.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wangyongyong
 * @date
 * @Description
 */
public abstract class BaseListAdapter<T, H extends BaseListAdapter.ViewHolder> extends BaseAdapter {
    private final List<T> mData;

    public BaseListAdapter(List<T> data) {
        mData = data == null ? new ArrayList<T>() : data;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public T getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final H holder;
        if (convertView == null) {
            holder = createHolder(position, parent);
            convertView = holder.itemView;
        } else {
            holder = (H) convertView.getTag();
        }
        bindData(position, holder, getItem(position));
        return convertView;
    }

    public void update(List<T> data) {
        if (mData != null && mData.size() > 0) {
            mData.clear();
        }
        addData(data);
    }

    public void addData(List<T> data) {
        if (data != null) {
            mData.addAll(data);
        }
        notifyDataSetChanged();
    }

    protected abstract H createHolder(int position, ViewGroup parent);

    /**
     * 设置列表里的视图内容
     *
     * @param position 在列表中的位置
     * @param holder   该位置对应的视图
     */
    protected abstract void bindData(int position, H holder, T data);

    public static abstract class ViewHolder<T> {
        public final View itemView;

        public ViewHolder(View itemView) {
            this.itemView = itemView;
            itemView.setTag(this);
        }

        public abstract void setData(T data);
    }


}
