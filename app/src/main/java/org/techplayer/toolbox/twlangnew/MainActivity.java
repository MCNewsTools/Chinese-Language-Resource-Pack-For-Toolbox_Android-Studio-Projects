package org.techplayer.toolbox.twlangnew;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AlertDialog;
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

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    Context context = this;
    File resourcePackVersionFile = null;

    String toolboxVersion = "4.3.8.2"; // 依據的 Toolbox 版本
    String resourcePackVersion = "4.3.8.2"; // 資源包版本
    String resourcePackName = "toolbox-zh"; // 資源包名稱 (也用於檔案資料夾名稱開頭關鍵字)
    String resourcePackFilenameExtension = "zip"; // 資源包檔案副檔名
    File resourcePackDirectory = new File(Environment.getExternalStorageDirectory() + "/games/com.mojang/resource_packs/"); // 資源包放置目錄

    private InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.resourcePackVersionFile = new File(context.getFilesDir(), "version.txt"); // 資源包版本驗證檔路徑

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Google 廣告
        MobileAds.initialize(this, "ca-app-pub-3794226192931198~1269445465");
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        //AdRequest adRequest = new AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR).addTestDevice("7BE475BB0E62BCD925DAB220ED46DE43").build();
        mAdView.loadAd(adRequest);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gotoRatingMessage(); // 前往 Google Play 評分訊息
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setItemIconTintList(null); // Icon 圖片不覆蓋顏色

        checkPermission(); // 檢查權限
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
        getMenuInflater().inflate(R.menu.main, menu);
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
            deleteResourcePack(); // 執行刪除資源包
            return true;
        } else if (id == R.id.action_share) {
            // 分享功能
            String subject = getString(R.string.app_name);
            String body = getString(R.string.text_share_body) + "\nhttps://play.google.com/store/apps/details?id=" + context.getPackageName();
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

        if (id == R.id.nav_website) {
            ConnectivityManager cManager=(ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = cManager.getActiveNetworkInfo();
            if (info != null && info.isAvailable()) {
                Uri uri = Uri.parse("https://blog.reh.tw/");
                Intent i = new Intent(Intent.ACTION_VIEW, uri);
                if (i.resolveActivity(getPackageManager()) != null) {
                    startActivity(i); // 啟動網站
                } else {
                    Toast.makeText(MainActivity.this, R.string.message_open_website_error, Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(MainActivity.this, R.string.message_no_network, Toast.LENGTH_LONG).show(); // 無網路
            }
        } else if (id == R.id.nav_google_play) {
            gotoRatingMessage(); // 前往 Google Play 評分訊息
        } else if (id == R.id.nav_mcpe) {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle(R.string.alertdialog_title_message)
                    .setIcon(R.drawable.ic_dialog_message)
                    .setMessage(R.string.alertdialog_message_open_mcbe)
                    .setCancelable(false)
                    .setNegativeButton(android.R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // 空白，退出 Dialog
                                }
                            }
                    )
                    .setPositiveButton(android.R.string.ok,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = getPackageManager().getLaunchIntentForPackage("com.mojang.minecraftpe");
                                    if (intent != null) {
                                        startActivity(intent); // 啟動 Minecraft: Bedrock Edition
                                    } else {
                                        Uri uri = Uri.parse("market://details?id=com.mojang.minecraftpe");
                                        Intent i = new Intent(Intent.ACTION_VIEW, uri);
                                        if (i.resolveActivity(getPackageManager()) != null) {
                                            startActivity(i); // 啟動 Google Play (com.mojang.minecraftpe)
                                        } else {
                                            Toast.makeText(MainActivity.this, R.string.message_open_market_error, Toast.LENGTH_LONG).show();
                                        }
                                    }
                                }
                            }
                    )
                    .show();
        } else if (id == R.id.nav_toolbox) {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle(R.string.alertdialog_title_message)
                    .setIcon(R.drawable.ic_dialog_message)
                    .setMessage(R.string.alertdialog_message_open_toolbox)
                    .setCancelable(false)
                    .setNegativeButton(android.R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // 空白，退出 Dialog
                                }
                            }
                    )
                    .setPositiveButton(android.R.string.ok,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = getPackageManager().getLaunchIntentForPackage("io.mrarm.mctoolbox");
                                    if (intent != null) {
                                        startActivity(intent); // 啟動 Toolbox for Minecraft: PE
                                    } else {
                                        Uri uri = Uri.parse("market://details?id=io.mrarm.mctoolbox");
                                        Intent i = new Intent(Intent.ACTION_VIEW, uri);
                                        if (i.resolveActivity(getPackageManager()) != null) {
                                            startActivity(i); // 啟動 Google Play (com.mojang.minecraftpe)
                                        } else {
                                            Toast.makeText(MainActivity.this, R.string.message_open_market_error, Toast.LENGTH_LONG).show();
                                        }
                                    }
                                }
                            }
                    )
                    .show();
        } else if (id == R.id.nav_developer_facebook) {
            Intent messenger = getPackageManager().getLaunchIntentForPackage("com.facebook.orca");
            if (messenger != null) {
                Uri uri = Uri.parse("fb://profile/100002587292678");
                Intent i = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(i); // 啟動 Facebook APP (@GoneToneDY)
            } else {
                ConnectivityManager cManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo info = cManager.getActiveNetworkInfo();
                if (info != null && info.isAvailable()) {
                    Uri uri = Uri.parse("https://www.facebook.com/GoneToneDY");
                    Intent i = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(i); // 啟動 Facebook 網頁 (@GoneToneDY)
                } else {
                    Toast.makeText(MainActivity.this, R.string.message_no_network, Toast.LENGTH_LONG).show(); // 無網路
                }
            }
        } else if (id == R.id.nav_developer_twitter) {
            Intent twitter = getPackageManager().getLaunchIntentForPackage("com.twitter.android");
            if (twitter != null) {
                Uri uri = Uri.parse("twitter://user?user_id=2742134276");
                Intent i = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(i); // 啟動 Twitter APP (@TPGoneTone)
            } else {
                ConnectivityManager cManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo info = cManager.getActiveNetworkInfo();
                if (info != null && info.isAvailable()) {
                    Uri uri = Uri.parse("https://twitter.com/TPGoneTone");
                    Intent i = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(i); // 啟動 Twitter 網頁 (@TPGoneTone)
                } else {
                    Toast.makeText(MainActivity.this, R.string.message_no_network, Toast.LENGTH_LONG).show(); // 無網路
                }
            }
        } else if (id == R.id.nav_developer_discord) {
            ConnectivityManager cManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = cManager.getActiveNetworkInfo();
            if (info != null && info.isAvailable()) {
                Uri uri = Uri.parse("https://discord.reh.tw");
                Intent i = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(i); // 啟動網頁 (discord.reh.tw)
            } else {
                Toast.makeText(MainActivity.this, R.string.message_no_network, Toast.LENGTH_LONG).show(); // 無網路
            }
        } else if (id == R.id.nav_github_source) {
            ConnectivityManager cManager=(ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = cManager.getActiveNetworkInfo();
            if (info != null && info.isAvailable()) {
                Uri uri = Uri.parse("https://github.com/MCNewsTools/Chinese-Language-Resource-Pack-For-Toolbox_Android-Studio-Projects");
                Intent i = new Intent(Intent.ACTION_VIEW, uri);
                if (i.resolveActivity(getPackageManager()) != null) {
                    startActivity(i); // 啟動 GitHub 網頁專案頁
                } else {
                    Toast.makeText(MainActivity.this, R.string.message_open_website_error, Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(MainActivity.this, R.string.message_no_network, Toast.LENGTH_LONG).show(); // 無網路
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    // 安裝資源包
    private void installResourcePack() {
        Button btn = (Button) findViewById(R.id.button_install);
        btn.setEnabled(false); // 禁用按鈕
        ((TextView)findViewById(R.id.button_install)).setText(R.string.button_text_waiting_installation); // 按鈕顯示等待安裝中

        new AlertDialog.Builder(MainActivity.this)
                .setTitle(R.string.alertdialog_title_message)
                .setIcon(R.drawable.ic_dialog_message)
                .setMessage(String.format((String) this.getResources().getText(R.string.alertdialog_message_install), toolboxVersion))
                .setCancelable(false)
                .setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 空白，退出 Dialog

                                // Google 插頁式廣告
                                Button btn = (Button) findViewById(R.id.button_install);
                                btn.setEnabled(false); // 禁用按鈕
                                ((TextView)findViewById(R.id.button_install)).setText(R.string.button_text_loading); // 按鈕顯示加載中
                                loadInterstitialAd(); // 加載 Google 插頁式廣告
                            }
                        }
                )
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intentMcpe = getPackageManager().getLaunchIntentForPackage("com.mojang.minecraftpe");
                                if (intentMcpe != null) {
                                    Intent intentToolbox = getPackageManager().getLaunchIntentForPackage("io.mrarm.mctoolbox");
                                    if (intentToolbox != null) {
                                        deleteDirectory(resourcePackDirectory, resourcePackName); // 刪除資源包
                                        copyAssetsFile(resourcePackName + "." + resourcePackFilenameExtension); // 從 assets 複製資源包 zip 檔案至資源包放置目錄

                                        startActivity(intentToolbox); // 啟動 Toolbox for Minecraft: PE

                                        try {
                                            Thread.sleep(3150); //延遲 3.15 秒
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                        Toast.makeText(getApplication(), R.string.message_open_toolbox, Toast.LENGTH_LONG).show();

                                        // 寫入版本號在資源包版本驗證檔
                                        try {
                                            FileWriter fw = new FileWriter(resourcePackVersionFile, false);
                                            BufferedWriter bw = new BufferedWriter(fw);
                                            bw.write(resourcePackVersion);
                                            bw.close();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }

                                        // Google 插頁式廣告
                                        Button btn = (Button) findViewById(R.id.button_install);
                                        btn.setEnabled(false); // 禁用按鈕
                                        ((TextView)findViewById(R.id.button_install)).setText(R.string.button_text_loading); // 按鈕顯示加載中
                                        loadInterstitialAd(); // 加載 Google 插頁式廣告

                                        ((TextView)findViewById(R.id.button_install)).setText(R.string.button_re_install); // 按鈕顯示重新安裝
                                    } else {
                                        Uri uri = Uri.parse("market://details?id=io.mrarm.mctoolbox");
                                        Intent i = new Intent(Intent.ACTION_VIEW, uri);
                                        if (i.resolveActivity(getPackageManager()) != null) {
                                            startActivity(i); // 啟動 Google Play (io.mrarm.mctoolbox)
                                        }

                                        Toast.makeText(getApplication(), R.string.message_no_install_toolbox, Toast.LENGTH_LONG).show();

                                        // Google 插頁式廣告
                                        Button btn = (Button) findViewById(R.id.button_install);
                                        btn.setEnabled(false); // 禁用按鈕
                                        ((TextView)findViewById(R.id.button_install)).setText(R.string.button_text_loading); // 按鈕顯示加載中
                                        loadInterstitialAd(); // 加載 Google 插頁式廣告
                                    }
                                } else {
                                    Uri uri = Uri.parse("market://details?id=com.mojang.minecraftpe");
                                    Intent i = new Intent(Intent.ACTION_VIEW, uri);
                                    if (i.resolveActivity(getPackageManager()) != null) {
                                        startActivity(i); // 啟動 Google Play (com.mojang.minecraftpe)
                                    }

                                    Toast.makeText(getApplication(), R.string.message_no_install_mcbe, Toast.LENGTH_LONG).show();

                                    // Google 插頁式廣告
                                    Button btn = (Button) findViewById(R.id.button_install);
                                    btn.setEnabled(false); // 禁用按鈕
                                    ((TextView)findViewById(R.id.button_install)).setText(R.string.button_text_loading); // 按鈕顯示加載中
                                    loadInterstitialAd(); // 加載 Google 插頁式廣告
                                }
                            }
                        }
                )
                .show();
    }

    // 刪除資源包
    private void deleteResourcePack() {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle(R.string.alertdialog_title_warning)
                .setIcon(R.drawable.ic_dialog_warning)
                .setMessage(R.string.alertdialog_message_delete)
                .setCancelable(false)
                .setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 空白，退出 Dialog
                            }
                        }
                )
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 判斷資源包放置資料夾是否存在
                                if (resourcePackDirectory.exists()) {
                                    // 存在
                                    int makeText = R.string.message_no_delete; // 無法刪除訊息
                                    for (File target : resourcePackDirectory.listFiles()) {
                                        String _str = target.getName();

                                        // 判斷資源包是否存在
                                        if (_str.startsWith(resourcePackName)) {
                                            // 存在
                                            deleteDirectory(resourcePackDirectory, resourcePackName); // 刪除資源包
                                            makeText = R.string.message_delete; // 刪除成功訊息
                                            break;
                                        }
                                    }
                                    Toast.makeText(getApplication(), makeText, Toast.LENGTH_LONG).show(); // 顯示訊息
                                } else {
                                    // 不存在
                                    Toast.makeText(getApplication(), R.string.message_no_delete, Toast.LENGTH_LONG).show(); // 顯示無法刪除訊息
                                }

                                ((TextView)findViewById(R.id.button_install)).setText(R.string.button_install); // 按鈕顯示安裝
                            }
                        }
                )
                .show();
    }

    // 從 assets 複製檔案
    private void copyAssetsFile(String fileName) {
        AssetManager assetManager = this.getAssets();

        InputStream in = null;
        OutputStream out = null;

        // 複製資源包 zip 檔案
        try {
            in = assetManager.open(fileName);
            String newFileName = resourcePackDirectory + "/" + fileName;
            out = new FileOutputStream(newFileName);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;
            out.flush();
            out.close();
            out = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 刪除檔案和資料夾
    public boolean deleteDirectory(File path, String headerKeyword) {
        if (!path.exists()) {
            return false;
        }

        if (path.isFile()) {
            path.delete();
            return true;
        }

        boolean del = false;

        for (File target : path.listFiles()) {
            String _str = target.getName();

            if (headerKeyword == null || _str.startsWith(headerKeyword)) {
                deleteDirectory(target, null);
                target.delete();
                del = true;
            }
        }

        return del;
    }

    /* 檢查權限 */
    private void checkPermission() {
        final String permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        int permissionCheck = ContextCompat.checkSelfPermission(this, permission);

        if (permissionCheck != PermissionChecker.PERMISSION_GRANTED) {
            Button btn = (Button) findViewById(R.id.button_install);
            btn.setEnabled(false); // 禁用按鈕
            ((TextView)findViewById(R.id.button_install)).setText(R.string.button_text_verify_permissions); // 按鈕顯示驗證權限中

            new AlertDialog.Builder(MainActivity.this)
                    .setTitle(R.string.alertdialog_title_warning)
                    .setIcon(R.drawable.ic_dialog_warning)
                    .setMessage(R.string.alertdialog_message_authorization)
                    .setCancelable(false)
                    .setPositiveButton(android.R.string.ok,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission}, 0);
                                }
                            }
                    )
                    .show();
            return;
        } else {
            // Google 插頁式廣告
            Button btn = (Button) findViewById(R.id.button_install);
            btn.setEnabled(false); // 禁用按鈕
            ((TextView)findViewById(R.id.button_install)).setText(R.string.button_text_loading); // 按鈕顯示加載中
            loadInterstitialAd(); // 加載 Google 插頁式廣告

            customUrlScheme(); // 應用程式 Custom URL scheme 判斷
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != 0) return;
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Google 插頁式廣告
            Button btn = (Button) findViewById(R.id.button_install);
            btn.setEnabled(false); // 禁用按鈕
            ((TextView)findViewById(R.id.button_install)).setText(R.string.button_text_loading); // 按鈕顯示加載中
            loadInterstitialAd(); // 加載 Google 插頁式廣告

            customUrlScheme(); // 應用程式 Custom URL scheme 判斷
        } else {
            checkPermission(); // 檢查權限
        }
    }

    /* 開始 */
    private void startSample() {
        Button btn = (Button) findViewById(R.id.button_install);
        btn.setEnabled(true); // 啟用按鈕

        // 判斷資源包放置資料夾是否存在
        if (!resourcePackDirectory.exists()) {
            // 不存在
            ((TextView)findViewById(R.id.button_install)).setText(R.string.button_install); // 按鈕顯示安裝
        } else {
            // 存在
            // 讀取版本紀錄並判斷
            String buttonText = (String) this.getResources().getText(R.string.button_install); // 按鈕顯示安裝
            for (File target : resourcePackDirectory.listFiles()) {
                String _str = target.getName();

                // 判斷資源包是否存在
                if (_str.startsWith(resourcePackName)) {
                    // 存在
                    buttonText = (String) this.getResources().getText(R.string.button_re_install); // 按鈕顯示重新安裝

                    try {
                        if (resourcePackVersionFile.exists()) {
                            // 讀取在裝置上的版本紀錄檔案
                            FileReader versionFileReader = new FileReader(resourcePackVersionFile);
                            BufferedReader versionBufferedReader = new BufferedReader(versionFileReader);
                            String versionReadText = "";
                            String versionTextLine = versionBufferedReader.readLine();
                            while (versionTextLine != null) {
                                versionReadText += versionTextLine;
                                versionTextLine = versionBufferedReader.readLine();
                            }

                            // 版本比對
                            if (!resourcePackVersion.equals(versionReadText)) {
                                buttonText = String.format((String) this.getResources().getText(R.string.button_update), "v" + resourcePackVersion); // 按鈕顯示更新
                            }
                        } else {
                            buttonText = String.format((String) this.getResources().getText(R.string.button_update), "v" + resourcePackVersion); // 按鈕顯示更新
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
            ((TextView)findViewById(R.id.button_install)).setText(buttonText); // 更新按鈕顯示文字
        }
    }

    // 前往 Google Play 評分訊息
    private void gotoRatingMessage() {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle(R.string.alertdialog_title_goto_google_play)
                .setIcon(R.drawable.ic_menu_google_play)
                .setMessage(R.string.alertdialog_message_goto_rating)
                .setCancelable(false)
                .setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 空白，退出 Dialog
                            }
                        }
                )
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Uri uri = Uri.parse("market://details?id=" + context.getPackageName());
                                Intent i = new Intent(Intent.ACTION_VIEW, uri);
                                if (i.resolveActivity(getPackageManager()) != null) {
                                    startActivity(i); // 啟動 Google Play
                                    Toast.makeText(MainActivity.this, R.string.message_open_google_play, Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(MainActivity.this, R.string.message_open_market_error, Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                )
                .show();
    }

    // 應用程式 Custom URL scheme 判斷
    private void customUrlScheme() {
        Intent intent = getIntent();
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri uri = intent.getData();
            String status = uri.getQueryParameter("status");
            if (status.equals("install")) {
                installResourcePack(); // 執行安裝資源包
            } else if (status.equals("delete")) {
                deleteResourcePack(); // 執行刪除資源包
            }
        }
    }

    // Google 插頁式廣告
    private void loadInterstitialAd() {
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-3794226192931198/4506956665");
        mInterstitialAd.setAdListener(new AdListener() {

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();

                startSample(); // 開始

                // 當 button_install 按鈕被點擊時
                findViewById(R.id.button_install).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mInterstitialAd.isLoaded()) {
                            mInterstitialAd.show(); // 顯示 Google 插頁式廣告
                        }
                    }
                });
            }

            @Override
            public void onAdClosed() {
                super.onAdClosed();

                installResourcePack(); // 執行安裝資源包
            }

            @Override
            public void onAdFailedToLoad(int i) {
                super.onAdFailedToLoad(i);

                startSample(); // 開始

                // 當 button_install 按鈕被點擊時
                findViewById(R.id.button_install).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        installResourcePack(); // 執行安裝資源包
                    }
                });
            }
        });

        AdRequest adRequest = new AdRequest.Builder().build();
        //AdRequest adRequest = new AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR).addTestDevice("7BE475BB0E62BCD925DAB220ED46DE43").build();
        mInterstitialAd.loadAd(adRequest);
    }
}
