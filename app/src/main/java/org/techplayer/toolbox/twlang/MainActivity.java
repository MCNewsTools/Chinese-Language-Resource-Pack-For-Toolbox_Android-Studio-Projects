package org.techplayer.toolbox.twlang;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import flipagram.assetcopylib.AssetCopier;

import static org.techplayer.toolbox.twlang.R.menu.main;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    String myFile = "/games/com.mojang/resource_packs/toolbox-zh_TW";
    String myPath = Environment.getExternalStorageDirectory() + myFile;
    File f = new File(myPath); // 存放路徑資料夾

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri=Uri.parse("market://details?id=org.techplayer.toolbox.twlang");
                Intent i=new Intent(Intent.ACTION_VIEW,uri);
                startActivity(i); // 啟動 Google Play

                Toast.makeText(MainActivity.this, R.string.message_open_google_play, Toast.LENGTH_LONG).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setItemIconTintList(null);

        // 判斷資料夾是否存在
        if (!f.exists()) {
            ((TextView)findViewById(R.id.btn_download)).setText(R.string.button_install); // 按鈕顯示安裝
        } else {
            ((TextView)findViewById(R.id.btn_download)).setText(R.string.button_re_install); // 按鈕顯示重新安裝
        }

        checkPermission();
    }

    public boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            for (int i=0;i<files.length;i++) {
                if (!deleteDir(files[i]))
                    return false;
            }
        }

        return dir.delete();
    }

    private void startSample() {
        findViewById(R.id.btn_download).setOnClickListener(new View.OnClickListener() {
            Button btn = (Button) findViewById(R.id.btn_download);
            @Override
            public void onClick(View v) {
                btn.setEnabled(false); // 禁用按鈕

                deleteDir(f); // 刪除資料夾文件

                ((TextView)findViewById(R.id.btn_download)).setText(R.string.button_text_installing);

                int count = -1;

                try {
                    File destDir = Environment.getExternalStoragePublicDirectory(myFile);
                    destDir.mkdirs();
                    count = new AssetCopier(MainActivity.this)
                            .withFileScanning()
                            .copy("toolbox-zh_TW", destDir); // 要複製在 assets 內資料夾或檔案名稱
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (count == -1) {
                    // 失敗
                    f.delete(); // 刪除空資料夾

                    btn.setEnabled(true); // 啟用按鈕

                    Toast.makeText(MainActivity.this, R.string.message_error, Toast.LENGTH_LONG).show();

                    ((TextView)findViewById(R.id.btn_download)).setText(R.string.button_text_report);
                    findViewById(R.id.btn_download).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Uri uri=Uri.parse("mailto:mc.stream@reh.tw?subject=問題回報 - ToolBox 正體中文資源包");
                            Intent i=new Intent(Intent.ACTION_VIEW,uri);
                            startActivity(i); // 啟動 Mail (mc.stream@reh.tw)
                        }
                    });
                } else {
                    // 成功
                    btn.setEnabled(true); // 啟用按鈕

                    Toast.makeText(MainActivity.this, R.string.message_successful, Toast.LENGTH_LONG).show();

                    ((TextView)findViewById(R.id.btn_download)).setText(R.string.button_text_install_done);
                    findViewById(R.id.btn_download).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = getPackageManager().getLaunchIntentForPackage("io.mrarm.mctoolbox");
                            if (intent != null) {
                                startActivity(intent); // 啟動 Toolbox for Minecraft: PE
                                Toast.makeText(getApplication(), R.string.message_open_toolbox, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getApplication(), R.string.message_no_install_toolbox, Toast.LENGTH_SHORT).show();

                                Uri uri=Uri.parse("market://details?id=io.mrarm.mctoolbox");
                                Intent i=new Intent(Intent.ACTION_VIEW,uri);
                                startActivity(i); // 啟動 Google Play (io.mrarm.mctoolbox)
                            }
                        }
                    });
                }
            }
        });
    }

    private void checkPermission() {
        final String permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        int permissionCheck = ContextCompat.checkSelfPermission(this, permission);

        if (permissionCheck != PermissionChecker.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{permission}, 0);
            return;
        }
        startSample();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != 0) return;
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startSample();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_delete) {
            deleteDir(f); // 刪除資料夾文件
            ((TextView)findViewById(R.id.btn_download)).setText(R.string.button_install); // 按鈕顯示安裝
            Toast.makeText(getApplication(), R.string.message_delete, Toast.LENGTH_SHORT).show();
            startSample();

            return true;
        } else if (id == R.id.action_share) {
            // 分享功能
            String subject = getString(R.string.app_name);
            String body = getString(R.string.text_share_body) + "\nhttps://play.google.com/store/apps/details?id=org.techplayer.toolbox.twlang";
            String chooserTitle = getString(R.string.text_share_to);

            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, body);
            startActivity(Intent.createChooser(sharingIntent, chooserTitle));

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_google_play) {
            Uri uri=Uri.parse("market://details?id=org.techplayer.toolbox.twlang");
            Intent i=new Intent(Intent.ACTION_VIEW,uri);
            startActivity(i); // 啟動 Google Play (org.techplayer.toolbox.twlang)

            Toast.makeText(MainActivity.this, R.string.message_open_google_play, Toast.LENGTH_LONG).show();
        } else if (id == R.id.nav_contact) {
            CharSequence[] contactList = {"FB Messenger", "Line", "Telegram", "Twitter"};
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle(R.string.alertdialog_contact_list_title)
                    .setItems(contactList, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int which) {
                            switch (which) {
                                case 0:
                                    Intent messenger = getPackageManager().getLaunchIntentForPackage("com.facebook.orca");
                                    if (messenger != null) {
                                        Uri uri=Uri.parse("fb-messenger://user/100002587292678");
                                        Intent i=new Intent(Intent.ACTION_VIEW,uri);
                                        startActivity(i); // 啟動 FB Messenger APP (@GoneToneDY)
                                    } else {
                                        ConnectivityManager cManager=(ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
                                        NetworkInfo info = cManager.getActiveNetworkInfo();
                                        if (info != null && info.isAvailable()) {
                                            Uri uri=Uri.parse("https://www.messenger.com/t/100002587292678/");
                                            Intent i=new Intent(Intent.ACTION_VIEW,uri);
                                            startActivity(i); // 啟動 FB Messenger 網頁 (@GoneToneDY)
                                        } else {
                                            Toast.makeText(MainActivity.this, R.string.message_no_network, Toast.LENGTH_LONG).show();
                                        }
                                    }
                                    break;
                                case 1:
                                    Intent line = getPackageManager().getLaunchIntentForPackage("jp.naver.line.android");
                                    if (line != null) {
                                        Uri uri=Uri.parse("line://ti/p/RgfP0uDWKe");
                                        Intent i=new Intent(Intent.ACTION_VIEW,uri);
                                        startActivity(i); // 啟動 Line APP (ID：29022716)
                                    } else {
                                        new AlertDialog.Builder(MainActivity.this)
                                                .setMessage(R.string.alertdialog_message_no_install_line)
                                                .setCancelable(false)
                                                .setPositiveButton(R.string.alertdialog_button_yes,
                                                        new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                Uri uri=Uri.parse("market://details?id=jp.naver.line.android");
                                                                Intent i=new Intent(Intent.ACTION_VIEW,uri);
                                                                startActivity(i); // 啟動 Google Play (jp.naver.line.android)
                                                            }
                                                        }
                                                )
                                                .setNegativeButton(R.string.alertdialog_button_no,
                                                        new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                // 關閉視窗
                                                            }
                                                        }
                                                )
                                                .show();
                                    }
                                    break;
                                case 2:
                                    Intent telegram = getPackageManager().getLaunchIntentForPackage("org.telegram.messenger");
                                    if (telegram != null) {
                                        Uri uri=Uri.parse("tg://resolve?domain=GoneTone");
                                        Intent i=new Intent(Intent.ACTION_VIEW,uri);
                                        startActivity(i); // 啟動 Telegram APP (@GoneTone)
                                    } else {
                                        ConnectivityManager cManager=(ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
                                        NetworkInfo info = cManager.getActiveNetworkInfo();
                                        if (info != null && info.isAvailable()) {
                                            Uri uri=Uri.parse("https://web.telegram.org/#/im?tgaddr=tg%3A%2F%2Fresolve%3Fdomain%3DGoneTone");
                                            Intent i=new Intent(Intent.ACTION_VIEW,uri);
                                            startActivity(i); // 啟動 Telegram 網頁 (@GoneTone)
                                        } else {
                                            Toast.makeText(MainActivity.this, R.string.message_no_network, Toast.LENGTH_LONG).show();
                                        }
                                    }
                                    break;
                                case 3:
                                    Intent twitter = getPackageManager().getLaunchIntentForPackage("com.twitter.android");
                                    if (twitter != null) {
                                        Uri uri=Uri.parse("twitter://user?user_id=2742134276");
                                        Intent i=new Intent(Intent.ACTION_VIEW,uri);
                                        startActivity(i); // 啟動 Twitter APP (@TPGoneTone)
                                    } else {
                                        ConnectivityManager cManager=(ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
                                        NetworkInfo info = cManager.getActiveNetworkInfo();
                                        if (info != null && info.isAvailable()) {
                                            Uri uri=Uri.parse("https://twitter.com/TPGoneTone");
                                            Intent i=new Intent(Intent.ACTION_VIEW,uri);
                                            startActivity(i); // 啟動 Twitter 網頁 (@TPGoneTone)
                                        } else {
                                            Toast.makeText(MainActivity.this, R.string.message_no_network, Toast.LENGTH_LONG).show();
                                        }
                                    }
                                    break;
                            }
                        }
                    }).show();
        } else if (id == R.id.nav_mcpe_google_play) {
            Intent intent = getPackageManager().getLaunchIntentForPackage("com.mojang.minecraftpe");
            if (intent != null) {
                startActivity(intent); // 啟動 Minecraft: Pocket Edition
            } else {
                Uri uri=Uri.parse("market://details?id=com.mojang.minecraftpe");
                Intent i=new Intent(Intent.ACTION_VIEW,uri);
                startActivity(i); // 啟動 Google Play (com.mojang.minecraftpe)
            }
        } else if (id == R.id.nav_toolbox_google_play) {
            Intent intent = getPackageManager().getLaunchIntentForPackage("io.mrarm.mctoolbox");
            if (intent != null) {
                startActivity(intent); // 啟動 Toolbox for Minecraft: PE
            } else {
                Uri uri=Uri.parse("market://details?id=io.mrarm.mctoolbox");
                Intent i=new Intent(Intent.ACTION_VIEW,uri);
                startActivity(i); // 啟動 Google Play (io.mrarm.mctoolbox)
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
