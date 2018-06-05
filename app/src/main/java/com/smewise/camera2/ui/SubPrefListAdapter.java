package com.smewise.camera2.ui;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.smewise.camera2.R;


/**
 * Created by wenzhe on 11/22/17.
 */

public class SubPrefListAdapter extends RecyclerView.Adapter<SubPrefListAdapter.MyViewHolder> {

    private CamListPreference mPref;
    private Context mContext;
    private PrefItemClickListener mListener;
    private int mTextColor;
    private int mHighlightColor;

    public interface PrefItemClickListener {
        void onItemClick(String key, String value);
    }


    public SubPrefListAdapter(Context context, CamListPreference pref) {
        mContext = context;
        mPref = pref;
        if (pref instanceof CamMenuPreference) {
            ((CamMenuPreference) pref).dynamicUpdateValue(mContext);
        }
        mTextColor = context.getResources().getColor(R.color.options_text_color);
        mHighlightColor = context.getResources().getColor(R.color.options_highlight_color);
    }

    public void setClickListener(PrefItemClickListener listener) {
        mListener = listener;
    }

    public void updateDataSet(CamListPreference pref) {
        mPref = pref;
        if (pref instanceof CamMenuPreference) {
            ((CamMenuPreference) pref).dynamicUpdateValue(mContext);
        }
        notifyDataSetChanged();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.option_item_layout, parent,
                false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        holder.itemText.setText(mPref.getEntries()[position]);
        if (position == mPref.getHighLightIdx()) {
            holder.itemText.setTextColor(mHighlightColor);
        }else {
            holder.itemText.setTextColor(mTextColor);
        }
        if (mPref.getEntryIcons() != null) {
            holder.itemIcon.setImageResource(mPref.getEntryIcons()[position]);
            holder.itemIcon.setVisibility(View.VISIBLE);
        } else {
            holder.itemIcon.setVisibility(View.GONE);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onItemClick(mPref.getKey(),
                            mPref.getEntryValues()[position].toString());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mPref.getEntries().length;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView itemIcon;
        TextView itemText;
        MyViewHolder(View itemView) {
            super(itemView);
            itemIcon = (ImageView) itemView.findViewById(R.id.item_icon);
            itemText = (TextView) itemView.findViewById(R.id.item_text);
        }
    }

}
