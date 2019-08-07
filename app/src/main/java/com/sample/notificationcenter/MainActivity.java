package com.sample.notificationcenter;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,
        MessageAdapter.OnItemClickListener {

    private RecyclerView recyclerView;

    private List<MessageBean> list;
    private MessageAdapter adapter;

    private int position;

    private Button refresh;
    private View visibilityLayout;
    private TextView newsTitleText;
    private TextView newsContentText;
    private Button leftButton;
    private Button rightButton;

    private TextView tvUnread;
    private Button all;
    private Button read;
    private Button delete;
    private Button edit;

    //编辑模式
    private boolean editMode;
    //对应全选按钮
    private boolean clicked;
    //区分是不是删除确认界面
    private boolean confirm;

    private List<Integer> selectedPosition = new ArrayList<>();

    private int unread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        visibilityLayout = findViewById(R.id.visibility_layout);
        newsTitleText = findViewById(R.id.news_title);
        newsContentText = findViewById(R.id.news_content);
        leftButton = findViewById(R.id.left_button);
        rightButton = findViewById(R.id.right_button);

        tvUnread = findViewById(R.id.unread);
        all = findViewById(R.id.selectall_button);
        read = findViewById(R.id.readed_button);
        delete = findViewById(R.id.delete_main_button);
        edit = findViewById(R.id.edit_button);

        leftButton.setOnClickListener(this);
        rightButton.setOnClickListener(this);
        all.setOnClickListener(this);
        read.setOnClickListener(this);
        delete.setOnClickListener(this);
        edit.setOnClickListener(this);

        refresh = findViewById(R.id.refresh_button);
        refresh.setOnClickListener(this);
        recyclerView = findViewById(R.id.message_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        //增加或减少条目动画效果，不要就注掉
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        if(getNews() == null){
            list = new ArrayList<>();
        }else {
            list = getNews();
        }
        adapter = new MessageAdapter(this, list);
        adapter.setOnItemClickListener(this);
        recyclerView.setAdapter(adapter);

        edit.setEnabled(true);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.refresh_button:
                setData();
                updateUnRead();
                break;
            case R.id.left_button:
            case R.id.right_button:
                buttonConfirm(id);
                break;
            case R.id.selectall_button:
                selectAll();
                break;
            case R.id.readed_button:
                messageRead();
                break;
            case R.id.delete_main_button:
                deleteMessage();
                break;
            case R.id.edit_button:
                editMessage();
                break;
        }
    }

    /**
     * 列表item点击事件处理
     *
     * @param position
     */
    @Override
    public void onItemClick(int position) {
        all.setText("全选");
        clicked = false;
        this.position = position;
        MessageBean bean = list.get(position);
        if (!editMode) {
            //非编辑模式点击item
            refresh(bean.getTitle(), bean.getMessage(), bean.getType());
            bean.setRead(true);
            adapter.notifyItemChanged(position, bean.isRead());
        } else {
            //编辑模式下点击item
            boolean isChecked = bean.isChecked();
            isChecked = !isChecked;
            bean.setChecked(isChecked);
            adapter.notifyItemChanged(position, bean.isChecked());
        }
        updateUnRead();
    }

    private void updateUnRead() {
        unread = 0;
        for (int i = 0; i < list.size(); i++) {
            if (!list.get(i).isRead()) {
                unread ++;
            }
        }

        if (unread == 0) {
            tvUnread.setVisibility(View.GONE);
        } else {
            tvUnread.setVisibility(View.VISIBLE);
            tvUnread.setText(String.valueOf(unread));
        }
    }

    /**
     * 列表中checkbox选中状态变化的处理
     *
     * @param position
     */
    @Override
    public void onChecked(int position) {
        all.setText("全选");
        clicked = false;
        this.position = position;
        MessageBean bean = list.get(position);
        setCheckData(bean.isChecked());
    }

    /**
     * 右边界面的左右两个按钮的点击事件处理
     *
     * @param id
     */
    private void buttonConfirm(int id) {
        if (!confirm) {
            int type = list.get(position).getType();
            showToast(id, type);
        } else {
            confirm = false;
            if (id == R.id.left_button) {
                //确认删除消息
                List<MessageBean> removeList = new ArrayList<>();
                for (int i = 0; i < selectedPosition.size(); i++) {
                    int position = selectedPosition.get(i);
                    MessageBean bean = list.get(position);
                    removeList.add(bean);
                }
                //删除选中item对应的数据
                list.removeAll(removeList);
                //清空已选中item的position数据
                selectedPosition.clear();
            }

            //删除后如果没数据了 隐藏功能键 且编辑按钮不可点击
            if (list.size() == 0) {
                showEdit(View.INVISIBLE);
                edit.setText("编辑");
                edit.setEnabled(false);
                editMode = false;
                refresh.setEnabled(true);
                adapter.editMode = false;
            }

            visibilityLayout.setVisibility(View.INVISIBLE);
            //删除后选中项为0，"已读"、"删除"按钮置灰
            read.setEnabled(false);
            delete.setEnabled(false);

            all.setText("全选");
            adapter.checkAll = false;
            adapter.checkNone = true;

            //所有item点击状态设为false
//            for (MessageBean bean : list) {
//                bean.setChecked(false);
//            }
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * 设为已读按钮
     */
    private void messageRead() {
        //设置选中item的已读状态
        for (int i = 0; i < selectedPosition.size(); i++) {
            MessageBean bean = list.get(selectedPosition.get(i));
            bean.setRead(true);
        }
        adapter.notifyDataSetChanged();
        Toast.makeText(this, "已读", Toast.LENGTH_SHORT).show();
    }

    /**
     * 删除按钮
     */
    private void deleteMessage() {
        confirm = true;
        visibilityLayout.setVisibility(View.VISIBLE);

        newsTitleText.setVisibility(View.INVISIBLE);
        newsContentText.setText("确定删除所选（有）信息吗");
        rightButton.setVisibility(View.VISIBLE);
        leftButton.setVisibility(View.VISIBLE);
        leftButton.setText("确定删除");
        rightButton.setText("取消");
    }

    /**
     * 编辑按钮
     */
    private void editMessage() {
        editMode = !editMode;
        if (editMode) {
            //编辑
            showEdit(View.VISIBLE);
            refresh.setEnabled(false);
            edit.setText("取消");
            visibilityLayout.setVisibility(View.INVISIBLE);
        } else {
            //取消编辑
            showEdit(View.INVISIBLE);
            refresh.setEnabled(true);
            edit.setText("编辑");
            visibilityLayout.setVisibility(View.VISIBLE);

            all.setText("全选");
            clicked = false;
            adapter.checkAll = false;
            adapter.checkNone = true;
            for (MessageBean bean : list) {
                bean.setChecked(false);
            }
            visibilityLayout.setVisibility(View.INVISIBLE);

            updateUnRead();
        }
        adapter.editMode = editMode;
        adapter.notifyDataSetChanged();
    }

    /**
     * 全选按钮
     */
    private void selectAll() {
        clicked = !clicked;
        if (clicked) {
            //全选
            all.setText("取消全选");
            adapter.checkAll = true;
            adapter.checkNone = false;

            if (!read.isEnabled()) {
                read.setEnabled(true);
                delete.setEnabled(true);
            }

            for (MessageBean bean : list) {
                bean.setChecked(true);
            }

        } else {
            read.setEnabled(false);
            delete.setEnabled(false);
            //反选
            all.setText("全选");
            adapter.checkAll = false;
            adapter.checkNone = true;

            for (MessageBean bean : list) {
                bean.setChecked(false);
            }
        }
        adapter.notifyDataSetChanged();
    }

    /**
     * 设置列表中的选中项
     *
     * @param checked
     */
    private void setCheckData(boolean checked) {
        if (checked) {
            //checkbox为选中状态
            selectedPosition.add(position);
            if (!read.isEnabled()) {
                read.setEnabled(true);
                delete.setEnabled(true);
            }
        } else {
            int size = selectedPosition.size();
            if (size > 0) {
                for (int i = 0; i < size; i++) {
                    if (selectedPosition.get(i) == position) {
                        selectedPosition.remove(i);
                        break;
                    }
                }
            }

            if (selectedPosition.size() == 0) {
                read.setEnabled(false);
                delete.setEnabled(false);
            }
        }

        Toast.makeText(this, "" + selectedPosition.size(), Toast.LENGTH_SHORT).show();
        if (selectedPosition.size() == 0) {
            all.setText("全选");
            clicked = false;
        } else if (selectedPosition.size() == list.size()) {
            all.setText("取消全选");
            clicked = true;
        }
    }

    /**
     * 点击编辑后显示各个功能按钮
     *
     * @param visibility
     */
    private void showEdit(int visibility) {
        all.setVisibility(visibility);
        read.setVisibility(visibility);
        delete.setVisibility(visibility);
        read.setEnabled(false);
        delete.setEnabled(false);
    }

    /**
     * 右边按钮点击后对应的Toast
     *
     * @param id
     * @param type
     */
    private void showToast(int id, int type) {
        String text = "";
        if (id == R.id.left_button) {
            switch (type) {
                case 5:
                    text = "导航成功";
                    break;
                case 6:
                    text = "成功车检";
                    break;
                case 3:
                    text = "保养成功";
                    break;
                case 4:
                    text = "查看详情";
                    break;
            }
        } else if (id == R.id.right_button) {
            if (type == 6) {
                text = "查看详情";
            }
        }
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    /**
     * 根据不同type来设置右边界面的显示
     *
     * @param newsTitle
     * @param newsContent
     * @param type
     */
    private void refresh(String newsTitle, String newsContent, int type) {
        visibilityLayout.setVisibility(View.VISIBLE);

        newsTitleText.setText(newsTitle);//刷新新闻标题
        newsContentText.setText(newsContent);//刷新新闻内容
        switch (type) {
            case 2:
                rightButton.setVisibility(View.INVISIBLE);
                leftButton.setVisibility(View.INVISIBLE);
                break;
            case 5:
                leftButton.setText("导航");
                leftButton.setVisibility(View.VISIBLE);
                rightButton.setVisibility(View.INVISIBLE);
                break;
            case 6:
                leftButton.setText("我已车检");
                rightButton.setText("查看详情");
                leftButton.setVisibility(View.VISIBLE);
                rightButton.setVisibility(View.VISIBLE);
                break;
            case 3:
                leftButton.setText("前去保养");
                leftButton.setVisibility(View.VISIBLE);
                rightButton.setVisibility(View.INVISIBLE);
                break;
            case 4:
                leftButton.setText("查看详情");
                leftButton.setVisibility(View.VISIBLE);
                rightButton.setVisibility(View.INVISIBLE);
                break;
        }
    }

    /**
     * 手动添加数据
     */
    private void setData() {
        int type = (int) (Math.random() * 10 + 1);
        switch (type) {
            case 1:
                list.add(0, new MessageBean("行程评分",
                        "本次行驶距离：xx公里；油耗：xx;急加速：xx次；急减速：xx次，急转弯：xx次。建议减速慢行，平稳行驶，可减少油耗，降低安全风险，祝您用车愉快！", "2019-01-01", 0, 2));
                break;
            case 2:
                list.add(0, new MessageBean("车辆保养提醒",
                        "尊敬的用户，累计行驶公里数，达到保养里程，请联系上汽大通官方4S店预约保养，谢谢。", "2019-01-01", 1, 3));
                break;
            case 3:
                list.add(0, new MessageBean("保养预约到期提醒",
                        "尊敬的用户，您的爱车，在年月日时间有一次维保服务预约。预约门店地址：xxx，联系电话xxx。", "2019-01-01", 1, 4));
                break;
            case 4:
                list.add(0, new MessageBean("车检提醒", "您的【车辆昵称】还有xx天要进行车检，请于x年x月x日前去完成车检，谢谢。", "2019-01-01", 1, 6));
                break;
            case 5:
                list.add(0, new MessageBean("目的地推送", "您收到来自xxx发送的目的地：上海市杨浦区军工路2500号。", "2019-01-01", 1, 5));
                break;
            case 6:
                list.add(0, new MessageBean("行程提醒", "15分钟后开车去公司。", "2019-01-01", 0, 5));
                break;
            case 7:
                list.add(0, new MessageBean("低油量提醒", "前油量偏低，点击前往附近加油站加油，保证车辆正常行驶", "2019-01-01", 0, 5));
                break;
            case 8:
                list.add(0, new MessageBean("可续里程不足", "您的车辆可续里程不足以到达目的地，请前往最近加油站加油。", "2019-01-01", 0, 5));
                break;
            case 9:
                list.add(0, new MessageBean("天气提醒", "明天有雨，请记得带伞。", "2019-01-01", 0, 2));
                break;
            case 10:
                list.add(0, new MessageBean("促销活动", "运营商提供的活动消息体", "2019-01-01", 0, 4));
                break;
        }

        if (adapter != null) {
            if (list.size() == 1) {
                edit.setEnabled(true);
            }
//            int lastPosition = list.size() - 1;
            //刷新列表数据
            adapter.notifyItemInserted(0);
            /**
             * 如果不需要动画效果
             * 就删掉 adapter.notifyItemInserted(lastPosition);
             * 用 adapter.notifyDataSetChanged();
             */
            recyclerView.scrollToPosition(0);
        }
    }
    /**
     * 从数据库中初始化模拟新闻数据
     * 将数据库的信息按倒序方式输出
     */
    private List<MessageBean> getNews() {
        List<MessageBean> mNewsList = new ArrayList<>();
        Cursor cursor = this.getContentResolver().query(MetaData.TableMetaData.CONTENT_URI, new String[]{"id", MetaData.TableMetaData.FIELD_TITLE, MetaData.TableMetaData.FIELD_MESSAGE,
                MetaData.TableMetaData.FIELD_FLAG, MetaData.TableMetaData.FIELD_TIME, MetaData.TableMetaData.FIELD_TYPE}, null, null, null);
        if (cursor == null) {
            Toast.makeText(this, "当前没有数据", Toast.LENGTH_SHORT).show();
            return null;
        }
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndex("id"));
            String title = cursor.getString(cursor.getColumnIndex("title"));
            String message = cursor.getString(cursor.getColumnIndex("message"));
            int flag = cursor.getInt(cursor.getColumnIndex("flag"));
            String time = cursor.getString(cursor.getColumnIndex("time"));
            int type = cursor.getInt(cursor.getColumnIndex("type"));
            MessageBean news = new MessageBean(title,message,time,flag,type);
            mNewsList.add(news);
        }
        cursor.close();
        Collections.reverse(mNewsList);
        return mNewsList;
    }

    public void modify(MessageBean messageBean) {
        ContentValues values = new ContentValues();
        values.put(MetaData.TableMetaData.FIELD_FLAG, 1);
        Uri uri = Uri.parse(MetaData.TableMetaData.CONTENT_URI.toString() + "/" + messageBean.getId());
        getContentResolver().update(uri, values, null, null);
    }

    public void delete(MessageBean messageBean){
        Uri uri = Uri.parse("content://com.sample.notificationcenter.newsprovider/messages/"+messageBean.getId());
        ContentResolver cr = getContentResolver();
        cr.delete(uri, null, null);
    }
}