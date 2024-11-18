package com.buta.hdagent;
import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;

import java.util.List;
import java.util.Map;

public class ExportDialogHelper {

    public static void showExportDialog(Context context, View view, List<String> folderNames) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        dialogBuilder.setTitle(context.getString(R.string.ExportData));

        final EditText input = new EditText(context);

        StringBuilder contentBuilder = new StringBuilder();
        for (String name : folderNames) {
            contentBuilder.append(name);
            contentBuilder.append(" ");
            Map<String, String> result = XmlAttributeReader.queryXmlAttributes(name);
            contentBuilder.append(result.get("higher_env3"));
            contentBuilder.append("-");
            contentBuilder.append(result.get("lower_env3"));
            contentBuilder.append(" ");
            contentBuilder.append(result.get("passToken_env3"));
            contentBuilder.append("\n");
        }
        input.setText(contentBuilder.toString().trim());

        ScrollView scrollView = new ScrollView(context);
        scrollView.addView(input);

        dialogBuilder.setView(scrollView);

        dialogBuilder.setPositiveButton(context.getString(R.string.OK), (dialog, which) -> dialog.dismiss());

        dialogBuilder.create().show();
    }
}
