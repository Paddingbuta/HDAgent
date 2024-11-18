package com.buta.hdagent;

import android.content.Context;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;

import com.buta.hdagent.ui.home.FolderAdapter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ArchiveUtils {
    public static void createArchiveFile(Context context, String archiveName, String id, String token,
                                         String language, FolderAdapter folderAdapter, String NamePre) {
        try {
            String high = "";
            String low = "";
            if (archiveName.isEmpty() || id.isEmpty() || token.isEmpty()) {
                Toast.makeText(context, "All fields must be filled", Toast.LENGTH_LONG).show();
                return;
            }

            // Split the ID into high and low parts
            String[] idParts = id.split("-");
            if (idParts.length == 2) {
                high = idParts[0];
                low = idParts[1];
            } else {
                Toast.makeText(context, "ID must be in the format 'XX-XXXXXX'", Toast.LENGTH_LONG).show();
                return;
            }
            // Default language to "EN" if not provided
            if (language.isEmpty()) {
                language = "EN";
            }

            if (NamePre != null){
                String basePath = "/data/data/com.buta.hdagent/files/profiles/";
                File originalFolder = new File(basePath + NamePre);
                File newFolder = new File(basePath + archiveName);
                originalFolder.renameTo(newFolder);
            }
            // Get the application-specific file directory path
            File appDirectory = new File(context.getFilesDir(), "profiles");
            File archiveDirectory = new File(appDirectory, archiveName);

            // Create the directory if it doesn't exist
            if (!archiveDirectory.exists()) {
                archiveDirectory.mkdirs();
            }

            // Create the storage_new.xml file
            File xmlFile = new File(archiveDirectory, "storage_new.xml");
            FileOutputStream fos = new FileOutputStream(xmlFile);
            String xmlContent = "<?xml version='1.0' encoding='utf-8' standalone='yes' ?>\n" +
                    "<map>\n" +
                    "    <string name=\"passToken_env3\">" + token + "</string>\n" +
                    "    <string name=\"higher_env3\">" + high + "</string>\n" +
                    "    <string name=\"language_code_env3\">" + language + "</string>\n" +
                    "    <string name=\"lower_env3\">" + low + "</string>\n" +
                    "</map>\n";
            fos.write(xmlContent.getBytes());
            fos.close();

            // Show success message and add the new folder to the adapter
            if (folderAdapter != null) {
                folderAdapter.addFolder(archiveName);
            }

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Failed to create archive", Toast.LENGTH_LONG).show();
        }
    }

    public static void createSCArchiveFile(Context context, String archiveName, String id, String token,
                                           String scid, String sctoken, FolderAdapter folderAdapter, String NamePre) {
        try {
            if (archiveName.isEmpty() || id.isEmpty() || token.isEmpty()) {
                Toast.makeText(context, "All fields must be filled", Toast.LENGTH_LONG).show();
                return;
            }

            if (NamePre != null){
                String basePath = "/data/data/com.buta.hdagent/files/sc_profiles/";
                File originalFolder = new File(basePath + NamePre);
                File newFolder = new File(basePath + archiveName);
                originalFolder.renameTo(newFolder);
            }
            // Get the application-specific file directory path
            File appDirectory = new File(context.getFilesDir(), "sc_profiles");
            File archiveDirectory = new File(appDirectory, archiveName);

            // Create the directory if it doesn't exist
            if (!archiveDirectory.exists()) {
                archiveDirectory.mkdirs();
            }

            // Create the storage_new.xml file
            File xmlFile = new File(archiveDirectory, "storage_new.xml");
            FileOutputStream fos = new FileOutputStream(xmlFile);
            String xmlContent = "<?xml version='1.0' encoding='utf-8' standalone='yes' ?>\n" +
                    "<map>\n" +
                    "    <string name=\"Token\">" + token + "</string>\n" +
                    "    <string name=\"ID\">" + id + "</string>\n" +
                    "    <string name=\"SCToken\">" + sctoken + "</string>\n" +
                    "    <string name=\"SCID\">" + scid + "</string>\n" +
                    "</map>\n";
            fos.write(xmlContent.getBytes());
            fos.close();

            // Show success message and add the new folder to the adapter
            if (folderAdapter != null) {
                folderAdapter.addFolder(archiveName);
            }

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Failed to create archive", Toast.LENGTH_LONG).show();
        }
    }
}
