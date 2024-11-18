package com.buta.hdagent;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.EditText;
import android.widget.Toast;
import android.view.View;
import com.buta.hdagent.ui.home.FolderAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Helper class to handle Import Dialog and data parsing
public class ImportDialogHelper {


    // DataEntry class to hold each line's parsed data
    public static class DataEntry {
        public String id;
        public String high;
        public String low;
        public String token;
        public DataEntry(String id, String high, String low, String token) {
            this.id = id;
            this.high = high;
            this.low = low;
            this.token = token;
        }
    }

    // Method to show the Import dialog
    public static void showImportDialog(Context context, View view, FolderAdapter folderAdapter) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        dialogBuilder.setTitle(context.getString(R.string.ImportData));

        // Large EditText for multiline input
        final EditText input = new EditText(context);
        input.setHint(context.getString(R.string.InputHint));
        input.setMinLines(1);
        input.setMaxLines(10000);
        dialogBuilder.setView(input);

        // Set up OK and Cancel buttons
        dialogBuilder.setPositiveButton(context.getString(R.string.OK), (dialog, which) -> {
            String inputText = input.getText().toString().trim();
            List<DataEntry> dataEntries = parseAndValidateInput(inputText, view, folderAdapter);
            MainActivity.navController.navigate(R.id.nav_home);
        });

        dialogBuilder.setNegativeButton(context.getString(R.string.Cancel), (dialog, which) -> dialog.dismiss());
        dialogBuilder.show();
    }

    // Method to parse and validate each line
    private static List<DataEntry> parseAndValidateInput(String inputText, View view, FolderAdapter folderAdapter) {
        List<DataEntry> dataEntries = new ArrayList<>();
        // Updated regex to match the format high-low followed by a token
        Pattern pattern = Pattern.compile("^(\\d+)-(\\d+)\\s+([\\w]+)$");

        String[] lines = inputText.split("\\n");
        int idCounter = 1;  // Starting identifier

        for (String line : lines) {
            Matcher matcher = pattern.matcher(line.trim());
            if (matcher.matches()) {
                String high = matcher.group(1);
                String low = matcher.group(2);
                String id = high + "-" + low;
                String token = matcher.group(3);
                ArchiveUtils.createArchiveFile(view.getContext(), Integer.toString(idCounter), id, token, "EN", folderAdapter, null);
                // Create a new DataEntry with sequential id, high, low, and token
                dataEntries.add(new DataEntry(String.valueOf(idCounter), high, low, token));
                idCounter++;  // Increment the identifier for the next entry
            } else {
                return null;  // Return null if any line does not match the expected format
            }
        }
        return dataEntries;
    }

}
