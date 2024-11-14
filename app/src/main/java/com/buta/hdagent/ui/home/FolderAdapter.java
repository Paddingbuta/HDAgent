package com.buta.hdagent.ui.home;
import com.buta.hdagent.R;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.io.File;
import java.util.List;

public class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.FolderViewHolder> {

    private List<String> folderNames;

    // Constructor to pass the folder list to the adapter
    public FolderAdapter(List<String> folderNames) {
        this.folderNames = folderNames;
    }

    @Override
    public FolderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate the layout for each item in the list
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        return new FolderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FolderViewHolder holder, int position) {
        // Bind the folder name to the TextView in the list item
        String folderName = folderNames.get(position);
        holder.textView.setText(folderName);
        // 设置长按事件监听器
        holder.itemView.setOnClickListener(v -> {
            // 在长按时弹出 PopupMenu
            showPopupMenu(v);
        });
    }

    @Override
    public int getItemCount() {
        // Return the size of the folder list
        return folderNames.size();
    }

    private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
        // 在PopupMenu中添加菜单项
        popupMenu.getMenuInflater().inflate(R.menu.file_menu, popupMenu.getMenu());

        // 设置菜单项点击事件
        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.menu_option_open) {
                // 执行编辑操作
                return true;
            } else if (item.getItemId() == R.id.menu_option_edit) {
                // 执行删除操作
                return true;
            } else if (item.getItemId() == R.id.menu_option_delete) {
                // 执行删除操作
                return true;
            } else {
                return false;
            }
        });

        popupMenu.show();
    }

    public static class FolderViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public FolderViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(android.R.id.text1);
        }
    }
}
