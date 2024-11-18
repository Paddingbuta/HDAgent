package com.buta.hdagent;
import com.buta.hdagent.ui.home.FolderAdapter;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ImageButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.buta.hdagent.databinding.ActivityMainBinding;
import com.buta.hdagent.ui.home.FolderAdapter;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    public static NavController navController;
    private RecyclerView recyclerView;
    private FolderAdapter folderAdapter;
    private List<String> folderNames;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        String languageCode = prefs.getString("language", "en");
        Resources resources = getResources();
        Configuration config = resources.getConfiguration();
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        config.setLocale(locale);
        resources.updateConfiguration(config, resources.getDisplayMetrics());

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        folderNames = new ArrayList<>();

        // Find the ImageButton by its ID
        ImageButton menuButton = findViewById(R.id.menu_button); // Ensure R.id.menu_button is correct

        // Set an OnClickListener for the ImageButton
        binding.appBarMain.menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Resources resources = getResources();
                Configuration config = resources.getConfiguration();
                Locale currentLocale = config.getLocales().get(0);

                String newLanguageCode;
                String[] languages = {"en", "zh", "ja", "vi", "th", "ar"};
                int currentIndex = -1;
                for (int i = 0; i < languages.length; i++) {
                    if (languages[i].equals(currentLocale.getLanguage())) {
                        currentIndex = i;
                        break;
                    }
                }
                int nextIndex = (currentIndex + 1) % languages.length;
                newLanguageCode = languages[nextIndex];

                Locale newLocale = new Locale(newLanguageCode);
                Locale.setDefault(newLocale);
                config.setLocale(newLocale);
                resources.updateConfiguration(config, resources.getDisplayMetrics());

                SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
                prefs.edit().putString("language", newLanguageCode).apply();

                Intent intent = getIntent();
                finish();
                startActivity(intent);
            }
        });
        binding.appBarMain.menuButtonNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String directoryPath = "/data/data/com.supercell.hayday/shared_prefs";
                File directory = new File(directoryPath);
                try {
                    if (checkDirectoryExists(directoryPath)) {
                        deleteDirectoryContents(directoryPath);
                    }
                    String packageName = "com.supercell.hayday";
                    String killCommand = "pkill -f " + packageName;
                    executeSuCommand(killCommand);
                    String startCommand = "am start -n " + packageName + "/com.supercell.hayday.GameApp";
                    executeSuCommand(startCommand);
                } catch (Exception e) {
                    Log.e("OpenAction", "发生错误：" + e.getMessage(), e);
                }

            }
        });

        // Initialize RecyclerView and FolderAdapter
        recyclerView = findViewById(R.id.recyclerView);
        folderAdapter = new FolderAdapter(folderNames);
        recyclerView.setAdapter(folderAdapter);

        setSupportActionBar(binding.appBarMain.toolbar);

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery)
                .setOpenableLayout(drawer)
                .build();

        binding.appBarMain.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create an AlertDialog builder
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(getString(R.string.BatchOperations));

                // Set options for the dialog
                String[] options = {getString(R.string.Import), getString(R.string.Export)};
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Handle the click event for each option
                        switch (which) {
                            case 0: // Import selected
                                ImportDialogHelper.showImportDialog(MainActivity.this, view, folderAdapter);
                                break;
                            case 1: // Export selected
                                List<String> folderNames = FolderAdapter.getFolderNames();
                                ExportDialogHelper.showExportDialog(MainActivity.this, view, folderNames);
                                break;
                        }
                    }
                });

                // Show the dialog
                builder.show();
            }
        });

        binding.appBarMain.fabSecond.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int currentDestinationId = navController.getCurrentDestination().getId();
                if (currentDestinationId == R.id.nav_gallery) {
                    LayoutInflater inflater = LayoutInflater.from(view.getContext());
                    View dialogView = inflater.inflate(R.layout.dialog_input_sc, null);

                    EditText inputArchiveName = dialogView.findViewById(R.id.input_archive_name);
                    EditText inputId = dialogView.findViewById(R.id.input_id);
                    EditText inputToken = dialogView.findViewById(R.id.input_token);
                    EditText inputscId = dialogView.findViewById(R.id.input_scid);
                    EditText inputscToken = dialogView.findViewById(R.id.input_sctoken);
                    AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                    builder.setView(dialogView)
                            .setTitle(getString(R.string.NewArchive))
                            .setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String archiveName = inputArchiveName.getText().toString().trim();
                                    String id = inputId.getText().toString().trim();
                                    String token = inputToken.getText().toString().trim();
                                    String scid = inputscId.getText().toString().trim();
                                    String sctoken = inputscToken.getText().toString().trim();
                                    if (ContextCompat.checkSelfPermission(view.getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                        ActivityCompat.requestPermissions((Activity) view.getContext(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                                    } else {
                                        ArchiveUtils.createSCArchiveFile(view.getContext(), archiveName, id, token, scid, sctoken, folderAdapter, null);
                                        navController.navigate(R.id.nav_gallery);
                                    }
                                }
                            })
                            .setNegativeButton(getString(R.string.Cancel), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });

                    builder.create().show();
                    return;
                }
                LayoutInflater inflater = LayoutInflater.from(view.getContext());
                View dialogView = inflater.inflate(R.layout.dialog_input, null);

                EditText inputArchiveName = dialogView.findViewById(R.id.input_archive_name);
                EditText inputId = dialogView.findViewById(R.id.input_id);
                EditText inputToken = dialogView.findViewById(R.id.input_token);
                EditText inputLanguage = dialogView.findViewById(R.id.input_language);

                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setView(dialogView)
                        .setTitle(getString(R.string.NewArchive))
                        .setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String archiveName = inputArchiveName.getText().toString().trim();
                                String id = inputId.getText().toString().trim();
                                String token = inputToken.getText().toString().trim();
                                String language = inputLanguage.getText().toString().trim();
                                if (ContextCompat.checkSelfPermission(view.getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                    ActivityCompat.requestPermissions((Activity) view.getContext(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                                } else {
                                    ArchiveUtils.createArchiveFile(view.getContext(), archiveName, id, token, language, folderAdapter, null);
                                    navController.navigate(R.id.nav_home);
                                }
                            }
                        })
                        .setNegativeButton(getString(R.string.Cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

                builder.create().show();
            }
        });

        binding.appBarMain.fabThird.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int currentDestinationId = navController.getCurrentDestination().getId();
                System.out.println(currentDestinationId);
                if (currentDestinationId == R.id.nav_gallery) {
                    LayoutInflater inflater = LayoutInflater.from(view.getContext());
                    View dialogView = inflater.inflate(R.layout.dialog_input_sc, null);
                    Map<String, String> result = XmlAttributeReader.queryHDAttributes();
                    System.out.println(result);
                    JSONObject jsonObject = null;
                    try {
                        if (result.get("SCID_PROD_ACCOUNTS") != null && result.get("passToken_env3") != null){
                            jsonObject = new JSONObject(result.get("SCID_PROD_ACCOUNTS"));
                        } else {
                            Toast.makeText(view.getContext(), getString(R.string.Filenotfound), Toast.LENGTH_SHORT).show();
                            return;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    String SCID = result.get("SCID_PROD_CURRENT_ACCOUNT_SUPERCELL_ID");

                    String email = "";
                    String token = "";
                    if (jsonObject.has(SCID)) {
                        try {
                            JSONObject targetObject = jsonObject.getJSONObject(SCID);
                            System.out.println(targetObject);
                            token = targetObject.getString("token");
                            email = targetObject.getString("email");
                            System.out.println(token);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        System.out.println("Token not found");
                        return;
                    }

                    EditText inputArchiveName = dialogView.findViewById(R.id.input_archive_name);
                    inputArchiveName.setText(email);
                    EditText inputId = dialogView.findViewById(R.id.input_id);
                    inputId.setText(result.get("higher_env3") + "-" + result.get("lower_env3"));
                    EditText inputToken = dialogView.findViewById(R.id.input_token);
                    inputToken.setText(result.get("passToken_env3"));
                    EditText inputscId = dialogView.findViewById(R.id.input_scid);
                    inputscId.setText(SCID);
                    EditText inputscToken = dialogView.findViewById(R.id.input_sctoken);
                    inputscToken.setText(token);

                    AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                    builder.setView(dialogView)
                            .setTitle(getString(R.string.NewArchive))
                            .setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String archiveName = inputArchiveName.getText().toString().trim();
                                    String id = inputId.getText().toString().trim();
                                    String token = inputToken.getText().toString().trim();
                                    String scid = inputscId.getText().toString().trim();
                                    String sctoken = inputscToken.getText().toString().trim();
                                    if (ContextCompat.checkSelfPermission(view.getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                        ActivityCompat.requestPermissions((Activity) view.getContext(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                                    } else {
                                        ArchiveUtils.createSCArchiveFile(view.getContext(), archiveName, id, token, scid, sctoken, folderAdapter, null);
                                        navController.navigate(R.id.nav_gallery);
                                    }
                                }
                            })
                            .setNegativeButton(getString(R.string.Cancel), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });

                    builder.create().show();
                    return;
                }
                LayoutInflater inflater = LayoutInflater.from(view.getContext());
                View dialogView = inflater.inflate(R.layout.dialog_input, null);
                Map<String, String> result = XmlAttributeReader.queryHDAttributes();
                EditText inputArchiveName = dialogView.findViewById(R.id.input_archive_name);
                if (result.get("passToken_env3") == null){
                    Toast.makeText(view.getContext(), getString(R.string.Filenotfound), Toast.LENGTH_SHORT).show();
                    return;
                }
                EditText inputId = dialogView.findViewById(R.id.input_id);
                inputId.setText(result.get("higher_env3") + "-" + result.get("lower_env3"));
                EditText inputToken = dialogView.findViewById(R.id.input_token);
                inputToken.setText(result.get("passToken_env3"));
                EditText inputLanguage = dialogView.findViewById(R.id.input_language);
                inputLanguage.setText(result.get("language_code_env3"));

                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setView(dialogView)
                        .setTitle(getString(R.string.NewArchive))
                        .setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String archiveName = inputArchiveName.getText().toString().trim();
                                String id = inputId.getText().toString().trim();
                                String token = inputToken.getText().toString().trim();
                                String language = inputLanguage.getText().toString().trim();
                                if (ContextCompat.checkSelfPermission(view.getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                    ActivityCompat.requestPermissions((Activity) view.getContext(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                                } else {
                                    ArchiveUtils.createArchiveFile(view.getContext(), archiveName, id, token, language, folderAdapter, null);
                                    navController.navigate(R.id.nav_home);
                                }
                            }
                        })
                        .setNegativeButton(getString(R.string.Cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

                builder.create().show();
            }
        });
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);

        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);

        NavigationUI.setupWithNavController(navigationView, navController);
        navController.navigate(R.id.nav_home);
        int currentDestinationId = navController.getCurrentDestination().getId();
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                navController.navigate(R.id.nav_home);
                binding.appBarMain.fab.show();
            } else if (id == R.id.nav_gallery) {
                navController.navigate(R.id.nav_gallery);
                binding.appBarMain.fab.hide();
            }

            // 关闭侧滑菜单
            drawer.closeDrawer(GravityCompat.START);
            return true;
        });

    }
    public static boolean checkDirectoryExists(String path) {
        String command = "[ -d \"" + path + "\" ] && echo \"exists\"";
        return executeSuCommand(command).contains("exists");
    }


    public static boolean deleteDirectoryContents(String path) {
        String command = "rm -rf " + path + "/*";
        return executeSuCommand(command).isEmpty();
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
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.action_about));
            builder.setMessage(Html.fromHtml(getString(R.string.AboutMsg), Html.FROM_HTML_MODE_LEGACY));


            // 设置“确定”按钮
            builder.setPositiveButton(getString(R.string.OK), (dialog, which) -> {
                dialog.dismiss(); // 关闭对话框
            });

            // 显示对话框
            builder.show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

}