package com.buta.hdagent.ui.home;
import com.buta.hdagent.AESUtils;
import com.buta.hdagent.XmlAttributeReader;
import static com.buta.hdagent.MainActivity.navController;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.recyclerview.widget.RecyclerView;

import com.buta.hdagent.ArchiveUtils;
import com.buta.hdagent.MainActivity;
import com.buta.hdagent.R;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.FolderViewHolder> {

    private static List<String> folderNames;

    public FolderAdapter(List<String> folderNames) {
        this.folderNames = folderNames;

    }
    public void addFolder(String newFolderName) {
        // Check if the new folder name already exists in the list
        if (!folderNames.contains(newFolderName)) {
            folderNames.add(newFolderName);  // Add new item to the list
            customSort(folderNames);   // Sort the list after adding the new folder name
            notifyItemInserted(folderNames.size() - 1);  // Notify adapter to refresh
        } else {
            // If the folder name already exists, skip adding
            Log.d("FolderAdapter", "File already exists: " + newFolderName);
        }
    }

    public void customSort(List<String> folderNames) {
        Collections.sort(folderNames, (a, b) -> {
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
    public FolderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate the custom layout for each item
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_folder, parent, false);
        return new FolderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FolderViewHolder holder, int position) {
        String folderName = folderNames.get(position);
        holder.textView.setText(folderName);

        // Set up click listener to show PopupMenu on item click
        holder.itemView.setOnClickListener(v -> showPopupMenu(v, folderName));
    }

    @Override
    public int getItemCount() {
        return folderNames.size();
    }

    public static List<String> getFolderNames() {
        return folderNames;
    }
    // Show PopupMenu with context options for the item
    private void showPopupMenu(View view, String folderName) {
        PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
        popupMenu.getMenuInflater().inflate(R.menu.file_menu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.menu_option_open) {
                String directoryPath = "/data/data/com.supercell.hayday/shared_prefs";
                File directory = new File(directoryPath);

                try {
                    if (checkDirectoryExists(directoryPath)) {
                        deleteDirectoryContents(directoryPath);
                    } else {
                        System.out.println("目录不存在。");
                    }

                    Map<String, String> xmlContent = XmlAttributeReader.queryXmlAttributes(folderName);

                    try {
                        String checkCommand = "[ -d \"" + directoryPath + "\" ] && echo \"exists\"";
                        if (executeSuCommand(checkCommand).contains("exists")) {
                            String filePath = directoryPath + "/storage_new.xml";
                            String xml = "<?xml version='1.0' encoding='utf-8' standalone='yes' ?>\n" +
                                    "<map>\n" +
                                    "    <string name=\"" + AESUtils.encryptECB("passToken_env3").trim() + "\">" + AESUtils.encryptCBC(xmlContent.get("passToken_env3")).trim() + "</string>\n" +
                                    "    <string name=\"" + AESUtils.encryptECB("higher_env3").trim() + "\">" + AESUtils.encryptCBC(xmlContent.get("higher_env3")).trim() + "</string>\n" +
                                    "    <string name=\"" + AESUtils.encryptECB("lower_env3").trim() + "\">" + AESUtils.encryptCBC(xmlContent.get("lower_env3")).trim() + "</string>\n" +
                                    "    <string name=\"" + AESUtils.encryptECB("language_code_env3").trim() + "\">" + AESUtils.encryptCBC(xmlContent.get("language_code_env3")).trim() + "</string>\n" +
                                    "    <string name=\"" + AESUtils.encryptECB("music_env3").trim() + "\">" + AESUtils.encryptCBC("false").trim() + "</string>\n" +
                                    "    <string name=\"" + AESUtils.encryptECB("sounds_env3").trim() + "\">" + AESUtils.encryptCBC("false").trim() + "</string>\n" +
                                    "</map>\n";

// 使用 System.out.println 直接打印 XML
                            System.out.println(xml);

// 构建 writeCommand，避免不必要的转义
                            String writeCommand = "echo \"" + xml.replace("\"", "\\\"") + "\" > " + filePath;

// 打印 writeCommand 用于调试
                            System.out.println(writeCommand);

                            if (executeSuCommand(writeCommand).isEmpty()) {
                                System.out.println("XML 文件已成功创建并写入内容。");
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    String packageName = "com.supercell.hayday";
                    String killCommand = "pkill -f " + packageName;
                    executeSuCommand(killCommand);
                    String startCommand = "am start -n " + packageName + "/com.supercell.hayday.GameApp";
                    executeSuCommand(startCommand);
                } catch (Exception e) {
                    Log.e("OpenAction", "发生错误：" + e.getMessage(), e);
                }
                return true;
            } else if (itemId == R.id.menu_option_edit) {
                LayoutInflater inflater = LayoutInflater.from(view.getContext());
                View dialogView = inflater.inflate(R.layout.dialog_input, null);

                EditText inputArchiveName = dialogView.findViewById(R.id.input_archive_name);
                inputArchiveName.setText(folderName);
                Map<String, String> result = XmlAttributeReader.queryXmlAttributes(folderName);
                EditText inputId = dialogView.findViewById(R.id.input_id);
                inputId.setText(result.get("higher_env3") + "-" + result.get("lower_env3"));
                EditText inputToken = dialogView.findViewById(R.id.input_token);
                inputToken.setText(result.get("passToken_env3"));
                EditText inputLanguage = dialogView.findViewById(R.id.input_language);
                inputLanguage.setText(result.get("language_code_env3"));

                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setView(dialogView)
                        .setTitle(view.getContext().getString(R.string.EditArchive))
                        .setPositiveButton(view.getContext().getString(R.string.OK), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String archiveName = inputArchiveName.getText().toString().trim();
                                String id = inputId.getText().toString().trim();
                                String token = inputToken.getText().toString().trim();
                                String language = inputLanguage.getText().toString().trim();
                                if (ContextCompat.checkSelfPermission(view.getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                    ActivityCompat.requestPermissions((Activity) view.getContext(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                                } else {
                                    ArchiveUtils.createArchiveFile(view.getContext(), archiveName, id, token, language, null, folderName);
                                    navController.navigate(R.id.nav_home);
                                }
                            }
                        })
                        .setNegativeButton(view.getContext().getString(R.string.Cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

                builder.create().show();
                return true;
            } else if (itemId == R.id.menu_option_delete) {
                // Execute delete action
                showDeleteConfirmationDialog(view, folderName);
                return true;
            } else {
                return false;
            }
        });


        popupMenu.show();
    }
    private void showDeleteConfirmationDialog(View view, String folderName) {
        // Create the AlertDialog builder
        new AlertDialog.Builder(view.getContext())
                .setTitle(view.getContext().getString(R.string.ConfirmDelete))
                .setMessage(view.getContext().getString(R.string.ConfirmDeleteText) + " " + folderName + " ?")
                .setPositiveButton(view.getContext().getString(R.string.Delete), (dialog, which) -> {
                    // Perform the deletion
                    deleteFolder(folderName, view);
                })
                .setNegativeButton(view.getContext().getString(R.string.Cancel), (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void deleteFolder(String folderName, View view) {
        // Define the path to the profiles/files directory
        String directoryPath = "/data/data/com.buta.hdagent/files/profiles/" + folderName;
        File directory = new File(directoryPath);
        Log.d("FolderAdapter", "Attempting to delete folder at path: " + directory.getAbsolutePath());
        if (directory.exists() && directory.isDirectory()) {
            // Delete the folder and its contents
            deleteRecursive(directory);
            int position = folderNames.indexOf(folderName);
            navController.navigate(R.id.nav_home);
            Toast.makeText(view.getContext(), "Folder \"" + folderName + "\" deleted.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(view.getContext(), "Folder \"" + folderName + "\" not found.", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                deleteRecursive(child);
            }
        }
        fileOrDirectory.delete();
    }
    public static boolean checkDirectoryExists(String path) {
        String command = "[ -d \"" + path + "\" ] && echo \"exists\"";
        return executeSuCommand(command).contains("exists");
    }


    public static boolean deleteDirectoryContents(String path) {
        String command = "rm -rf " + path + "/*";
        return executeSuCommand(command).isEmpty(); // rm 不会有输出表示成功
    }

    private static String executeSuCommand(String command) {
        StringBuilder output = new StringBuilder();
        try {
            Process process = Runtime.getRuntime().exec("su");
            process.getOutputStream().write((command + "\nexit\n").getBytes());
            process.getOutputStream().flush();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return output.toString();
    }
    public static class FolderViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public FolderViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.folder_name);
        }
    }
}
