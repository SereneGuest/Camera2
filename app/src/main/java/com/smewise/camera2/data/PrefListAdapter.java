package com.smewise.camera2.data;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.smewise.camera2.R;


/**
 * Created by wenzhe on 11/22/17.
 */

public class PrefListAdapter extends RecyclerView.Adapter<PrefListAdapter.MyViewHolder> {

    private PreferenceGroup mGroup;
    private Context mContext;
    private PrefClickListener mListener;
    private int mHighlightIndex = -1;
    private int mTextColor;
    private int mHighlightColor;

    public interface PrefClickListener {
        void onClick(View view, int position, CamListPreference preference);
    }

    public PrefListAdapter(Context context, PreferenceGroup group) {
        mContext = context;
        mGroup = group;
        mTextColor = context.getResources().getColor(R.color.menu_text_color);
        mHighlightColor = context.getResources().getColor(R.color.menu_highlight_color);
    }

    public PreferenceGroup getPrefGroup() {
        return mGroup;
    }

    public void updateHighlightIndex(int index, boolean notify) {
        int preIndex = mHighlightIndex;
        mHighlightIndex = index;
        if (notify) {
            notifyItemChanged(preIndex);
            notifyItemChanged(mHighlightIndex);
        }
    }

    public void setClickListener(PrefClickListener listener) {
        mListener = listener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.menu_item_layout, parent,
                false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        CamListPreference preference = mGroup.get(position);
        int color = position == mHighlightIndex ? mHighlightColor : mTextColor;
        holder.itemText.setTextColor(color);
        if (TextUtils.isEmpty(preference.getTitle())) {
            holder.itemText.setVisibility(View.GONE);
        } else {
            holder.itemText.setVisibility(View.VISIBLE);
            holder.itemText.setText(preference.getTitle());
        }
        if (CamListPreference.RES_NULL == preference.getIcon()) {
            holder.itemIcon.setVisibility(View.GONE);
        } else {
            holder.itemIcon.setVisibility(View.VISIBLE);
            holder.itemIcon.setImageResource(preference.getIcon());
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onClick(v, position, mGroup.get(position));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mGroup.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView itemIcon;
        TextView itemText;
        MyViewHolder(View itemView) {
            super(itemView);
            itemIcon = itemView.findViewById(R.id.item_icon);
            itemText = itemView.findViewById(R.id.item_text);
        }
    }

}
