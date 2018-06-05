package com.smewise.camera2.ui;

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

    public interface PrefClickListener {
        void onClick(View view, String key, CamListPreference preference);
    }

    public PrefListAdapter(Context context, PreferenceGroup group) {
        mContext = context;
        mGroup = group;
    }

    public void setClickListener(PrefClickListener listener) {
        mListener = listener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.option_item_layout, parent,
                false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        CamListPreference preference = mGroup.get(position);
        if (TextUtils.isEmpty(preference.getTitle())) {
            holder.itemText.setVisibility(View.GONE);
        } else {
            holder.itemText.setVisibility(View.VISIBLE);
            holder.itemText.setText(preference.getTitle());
        }
        holder.itemIcon.setImageResource(preference.getIcon());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onClick(v, mGroup.get(position).getKey(), mGroup.get(position));
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
            itemIcon = (ImageView) itemView.findViewById(R.id.item_icon);
            itemText = (TextView) itemView.findViewById(R.id.item_text);
        }
    }

}
