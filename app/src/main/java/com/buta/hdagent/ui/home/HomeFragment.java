package com.buta.hdagent.ui.home;
import com.buta.hdagent.R;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.buta.hdagent.databinding.FragmentHomeBinding;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private RecyclerView recyclerView;
    private FolderAdapter fileAdapter;
    private List<String> folderNames = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        // 使用 ViewBinding 来获取视图
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // 初始化 RecyclerView
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // 加载文件夹名称
        loadFoldersFromAppDirectory();

        // 设置 RecyclerView 适配器

        fileAdapter = new FolderAdapter(folderNames);
        binding.recyclerView.setAdapter(fileAdapter);

        return root;
    }

    private void loadFoldersFromAppDirectory() {
        folderNames.clear();
        // Get the directory of profiles within the app
        File profilesDir = new File(getContext().getFilesDir(), "profiles");

        // Check if the directory exists and is a directory
        if (profilesDir.exists() && profilesDir.isDirectory()) {
            // Get all folder names
            String[] folderList = profilesDir.list();
            if (folderList != null) {
                customSort(folderList);
                folderNames.addAll(Arrays.asList(folderList));
            }
        }
    }
    public static void customSort(String[] folderNames) {
        Arrays.sort(folderNames, (a, b) -> {
            String prefixA = a.replaceAll("\\d+$", "");
            String prefixB = b.replaceAll("\\d+$", "");

            if (prefixA.equals(prefixB)) {
                String numA = a.replaceAll("\\D+", "");
                String numB = b.replaceAll("\\D+", "");
                return Integer.compare(Integer.parseInt(numA), Integer.parseInt(numB));
            }
            return a.compareTo(b);
        });
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
