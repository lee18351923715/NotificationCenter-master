package com.sample.notificationcenter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private Context context;
    private List<MessageBean> list;
    private OnItemClickListener itemClickListener;

    //是否是编辑状态
    public boolean editMode;
    //点击了全选
    public boolean checkAll;
    //点击了取消全选
    public boolean checkNone;

    public MessageAdapter(Context context, List<MessageBean> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = View.inflate(context, R.layout.item_message, null);
        View view = LayoutInflater.from(context).inflate(R.layout.item_message, parent, false);
        final MessageViewHolder holder = new MessageViewHolder(view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (itemClickListener != null) {
                    checkAll = false;
                    checkNone = false;
                    itemClickListener.onItemClick(holder.getLayoutPosition());
                }
            }
        });

        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (itemClickListener != null) {
                    int position = holder.getLayoutPosition();
                    MessageBean bean = list.get(position);
                    bean.setChecked(b);
                    itemClickListener.onChecked(position);
                }
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, final int position) {
        MessageBean bean = list.get(position);
        holder.title.setText(bean.getTitle());
        holder.time.setText(bean.getTime());
        holder.content.setText(bean.getMessage());

        if (editMode) {
            holder.read.setVisibility(View.INVISIBLE);
            holder.checkBox.setVisibility(View.VISIBLE);

        } else {
            if (bean.isRead()) {
                holder.read.setVisibility(View.INVISIBLE);
            } else {
                holder.read.setVisibility(View.VISIBLE);
            }
            holder.checkBox.setVisibility(View.INVISIBLE);
        }

        if (bean.isChecked()) {
            holder.checkBox.setChecked(true);
        } else {
            holder.checkBox.setChecked(false);
        }

        if (checkAll) {
            holder.checkBox.setChecked(true);
        }

        if (checkNone) {
            holder.checkBox.setChecked(false);
        }

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
        void onChecked(int position);
    }

    public void setOnItemClickListener(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        ImageView read;
        CheckBox checkBox;
        TextView title;
        TextView time;
        TextView content;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            read = itemView.findViewById(R.id.read);
            checkBox = itemView.findViewById(R.id.checkbox);
            title = itemView.findViewById(R.id.title);
            time = itemView.findViewById(R.id.time);
            content = itemView.findViewById(R.id.content);
        }
    }
}
