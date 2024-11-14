package com.buta.hdagent;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;

import com.buta.hdagent.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);
        binding.appBarMain.fabSecond.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater inflater = LayoutInflater.from(view.getContext());
                View dialogView = inflater.inflate(R.layout.dialog_input, null);

                EditText inputArchiveName = dialogView.findViewById(R.id.input_archive_name);
                EditText inputId = dialogView.findViewById(R.id.input_id);
                EditText inputToken = dialogView.findViewById(R.id.input_token);
                EditText inputLanguage = dialogView.findViewById(R.id.input_language);

                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setView(dialogView)
                        .setTitle("New Archive")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String archiveName = inputArchiveName.getText().toString().trim();
                                String id = inputId.getText().toString().trim();
                                String token = inputToken.getText().toString().trim();
                                String language = inputLanguage.getText().toString().trim();

                                // 检查是否有空白项
                                if (archiveName.isEmpty() || id.isEmpty() || token.isEmpty()) {
                                    Toast.makeText(view.getContext(), "All fields must be filled", Toast.LENGTH_LONG).show();
                                } else {
                                    String high = "";
                                    String low = "";
                                    String[] idParts = id.split("-");
                                    if (idParts.length == 2) {
                                        high = idParts[0];
                                        low = idParts[1];
                                    } else {
                                        Toast.makeText(view.getContext(), "ID must be in the format 'XX-XXXXXX'", Toast.LENGTH_LONG).show();
                                        return;
                                    }
                                    if (language.isEmpty()) {
                                        language = "EN";
                                    }
                                    if (ContextCompat.checkSelfPermission(view.getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                        ActivityCompat.requestPermissions((Activity) view.getContext(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                                    } else {
                                        createArchiveFile(view.getContext(), archiveName, high, low, token, language);
                                    }
                                }
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

                builder.create().show();
            }
        });
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

// 设置 AppBarConfiguration，指定哪些菜单项是顶级目的地
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery)
                .setOpenableLayout(drawer)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);

// 设置 ActionBar 和 NavController 的关联
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);

// 让 NavigationView 与 NavController 进行关联
        NavigationUI.setupWithNavController(navigationView, navController);

// 设置 NavigationView 的项选择监听器
        navigationView.setNavigationItemSelectedListener(item -> {
            // 获取当前选中的菜单项ID
            int id = item.getItemId();

            // 如果当前选择的菜单项ID与当前导航的ID相同，则手动重新导航以刷新页面
            if (id == R.id.nav_home) {
                // 强制重新导航到 home Fragment
                navController.navigate(R.id.nav_home);
            } else if (id == R.id.nav_gallery) {
                // 强制重新导航到 gallery Fragment
                navController.navigate(R.id.nav_gallery);
            }

            // 关闭侧滑菜单
            drawer.closeDrawer(GravityCompat.START);
            return true;
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }


    private void createArchiveFile(Context context, String archiveName, String high, String low, String token, String language) {
        try {
            // 获取应用专属文件夹路径
            File appDirectory = new File(context.getFilesDir(), "profiles");
            File archiveDirectory = new File(appDirectory, archiveName);

            // 创建文件夹
            if (!archiveDirectory.exists()) {
                archiveDirectory.mkdirs();
            }

            // 创建storage_new.xml文件
            File xmlFile = new File(archiveDirectory, "storage_new.xml");
            FileOutputStream fos = new FileOutputStream(xmlFile);
            String xmlContent = "<?xml version='1.0' encoding='utf-8' standalone='yes' ?>\n" +
                    "<map>\n" +
                    "    <string name=\"passToken_env3\">" + token + "</string>\n" +
                    "    <string name=\"higher_env3\">" + high + "</string>\n" +
                    "    <string name=\"language_code_env3\">"+ language  +"</string>\n" +
                    "    <string name=\"lower_env3\">" + low + "</string>\n" +
                    "</map>\n";
            fos.write(xmlContent.getBytes());
            fos.close();

            Toast.makeText(context, "Archive created successfully", Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Failed to create archive", Toast.LENGTH_LONG).show();
        }
    }
}