package com.smewise.camera2.data;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.smewise.camera2.R;


/**
 * Created by wenzhe on 11/22/17.
 */

public class ProMenuAdapter extends RecyclerView.Adapter<ProMenuAdapter.MyViewHolder> {

    private PreferenceGroup mGroup;
    private Context mContext;
    private PrefClickListener mListener;
    private int mTextColor;

    public interface PrefClickListener {
        void onClick(View view, int position, CamListPreference preference);
    }

    public ProMenuAdapter(Context context, PreferenceGroup group) {
        mContext = context;
        mGroup = group;
        mTextColor = context.getResources().getColor(R.color.menu_text_color);
    }

    public PreferenceGroup getPrefGroup() {
        return mGroup;
    }

    public void setClickListener(PrefClickListener listener) {
        mListener = listener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(
                R.layout.pro_menu_item_layout, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        CamListPreference preference = mGroup.get(position);
        holder.itemTitle.setText(preference.getTitle());
        holder.itemSummary.setText(preference.getSummary());
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
        TextView itemTitle;
        TextView itemSummary;
        MyViewHolder(View itemView) {
            super(itemView);
            itemTitle = itemView.findViewById(R.id.item_title);
            itemSummary = itemView.findViewById(R.id.item_summary);
        }
    }

}
