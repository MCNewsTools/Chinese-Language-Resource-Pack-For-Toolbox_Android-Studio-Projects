package org.techplayer.toolbox.twlangnew;

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

import com.google.android.gms.ads.*;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;

import com.sromku.simple.storage.SimpleStorage;
import com.sromku.simple.storage.Storage;

import java.io.File;
import java.io.IOException;

import flipagram.assetcopylib.AssetCopier;

import static org.techplayer.toolbox.twlangnew.R.menu.main;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, RewardedVideoAdListener {
    Storage storage = SimpleStorage.getExternalStorage(); // 初始化檔案管理

    String resourcePackFile = "/games/com.mojang/resource_packs/toolbox-zh_TW/";
    String resourcePackPath = Environment.getExternalStorageDirectory() + resourcePackFile;
    File resourcePackF = new File(resourcePackPath); // 存放路徑資料夾

    private RewardedVideoAd mAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Button btn = (Button) findViewById(R.id.btn_download);
        btn.setEnabled(false); // 禁用按鈕
        ((TextView)findViewById(R.id.btn_download)).setText(R.string.button_text_loading); // 按鈕顯示加載中

        // Google 廣告應用程式 ID
        MobileAds.initialize(this, "ca-app-pub-3794226192931198~1269445465");

        // Google 獎勵型影片廣告
        mAd = MobileAds.getRewardedVideoAdInstance(this);
        mAd.setRewardedVideoAdListener(this);

        loadRewardedVideoAd(); // 加載獎勵型影片廣告

        // 呼叫 Google 橫幅廣告
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri=Uri.parse("market://details?id=org.techplayer.toolbox.twlangnew");
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

        checkPermission();
    }

    // 安裝資源包
    private void installResourcePack(int message) {
        Button btn = (Button) findViewById(R.id.btn_download);

        ((TextView)findViewById(R.id.btn_download)).setText(R.string.button_text_installing); // 按鈕顯示安裝中

        int count = -1;

        try {
            File destDir = resourcePackF;
            destDir.mkdirs();
            count = new AssetCopier(MainActivity.this)
                    .withFileScanning()
                    .copy("toolbox-zh_TW", destDir); // 要複製在 assets 內資料夾或檔案名稱
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (count == -1) {
            // 失敗
            btn.setEnabled(true); // 啟用按鈕

            new AlertDialog.Builder(MainActivity.this)
                    .setTitle(R.string.alertdialog_title_error)
                    .setIcon(R.drawable.ic_dialog_alert)
                    .setMessage(R.string.alertdialog_message_error)
                    .setCancelable(false)
                    .setNegativeButton(R.string.alertdialog_button_report,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    contact(); // 聯絡方式選擇
                                }
                            }
                    )
                    .setPositiveButton(R.string.alertdialog_button_close,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // 空白，退出 Dialog
                                }
                            }
                    )
                    .show();

            ((TextView)findViewById(R.id.btn_download)).setText(R.string.button_re_install); // 按鈕顯示重新安裝
        } else {
            // 成功
            btn.setEnabled(true); // 啟用按鈕

            new AlertDialog.Builder(MainActivity.this)
                    .setTitle(R.string.alertdialog_title_successful)
                    .setIcon(R.drawable.ic_dialog_info)
                    .setMessage(message)
                    .setCancelable(false)
                    .setNegativeButton(R.string.alertdialog_button_start,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
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
                            }
                    )
                    .setPositiveButton(R.string.alertdialog_button_close,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // 空白，退出 Dialog
                                }
                            }
                    )
                    .show();

            ((TextView)findViewById(R.id.btn_download)).setText(R.string.button_re_install); // 按鈕顯示重新安裝
        }
    }

    // 預先加載獎勵型影片廣告
    private void loadRewardedVideoAd() {
        mAd.loadAd("ca-app-pub-3794226192931198/6315093863", new AdRequest.Builder().build());
    }

    // 獎勵型影片廣告觀看完畢執行
    @Override
    public void onRewarded(RewardItem reward) {
        int amount = reward.getAmount();
        String currency = reward.getType();

        if (currency.equals("download_resource_pack") && amount >= 1) {
            installResourcePack(R.string.alertdialog_message_successful); // 執行安裝資源包
        }
    }

    // The following listener methods are optional.
    @Override
    public void onRewardedVideoAdLeftApplication() {

    }

    // 獎勵型影片廣告關閉執行
    @Override
    public void onRewardedVideoAdClosed() {
        Button btn = (Button) findViewById(R.id.btn_download);
        btn.setEnabled(false); // 禁用按鈕
        ((TextView)findViewById(R.id.btn_download)).setText(R.string.button_text_loading); // 按鈕顯示加載中
        loadRewardedVideoAd(); // 加載獎勵型影片廣告

        // 判斷資料夾不存在執行
        if (!resourcePackF.exists()) {
            installResourcePack(R.string.alertdialog_message_exception_successful); // 執行安裝資源包

            /*
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle(R.string.alertdialog_title_warning)
                    .setIcon(R.drawable.ic_dialog_alert)
                    .setMessage(R.string.alertdialog_message_not_viewing_ad)
                    .setCancelable(false)
                    .setNegativeButton(R.string.alertdialog_button_ok,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // 空白，退出 Dialog
                                }
                            }
                    )
                    .show();
            */
        }
    }

    // 獎勵型影片廣告無法加載執行
    @Override
    public void onRewardedVideoAdFailedToLoad(int errorCode) {
        Button btn = (Button) findViewById(R.id.btn_download);
        btn.setEnabled(true); // 啟用按鈕

        ConnectivityManager cManager=(ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cManager.getActiveNetworkInfo();
        if (info != null && info.isAvailable()) {
            ((TextView)findViewById(R.id.btn_download)).setText(R.string.button_load_failed_retry); // 按鈕顯示加載失敗
        } else {
            // 判斷資料夾是否存在
            if (!resourcePackF.exists()) {
                ((TextView)findViewById(R.id.btn_download)).setText(R.string.button_install); // 按鈕顯示安裝
            } else {
                ((TextView)findViewById(R.id.btn_download)).setText(R.string.button_re_install); // 按鈕顯示重新安裝
            }
        }

        findViewById(R.id.btn_download).setOnClickListener(new View.OnClickListener() {
            Button btn = (Button) findViewById(R.id.btn_download);
            @Override
            public void onClick(View v) {
                btn.setEnabled(false); // 禁用按鈕

                ConnectivityManager cManager=(ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo info = cManager.getActiveNetworkInfo();
                if (info != null && info.isAvailable()) {
                    btn.setEnabled(false); // 禁用按鈕
                    ((TextView)findViewById(R.id.btn_download)).setText(R.string.button_text_loading); // 按鈕顯示加載中
                    loadRewardedVideoAd(); // 加載獎勵型影片廣告
                } else {
                    installResourcePack(R.string.alertdialog_message_no_network_successful); // 執行安裝資源包
                }
            }
        });
    }

    // 獎勵型影片廣告加載後執行
    @Override
    public void onRewardedVideoAdLoaded() {
        Button btn = (Button) findViewById(R.id.btn_download);
        btn.setEnabled(true); // 啟用按鈕

        // 判斷資料夾是否存在
        if (!resourcePackF.exists()) {
            ((TextView)findViewById(R.id.btn_download)).setText(R.string.button_install); // 按鈕顯示安裝
        } else {
            ((TextView)findViewById(R.id.btn_download)).setText(R.string.button_re_install); // 按鈕顯示重新安裝
        }
    }

    // 獎勵型影片廣告開啟時執行
    @Override
    public void onRewardedVideoAdOpened() {
        storage.deleteDirectory(resourcePackFile); // 刪除資料夾
    }

    // 獎勵型影片廣告運行時執行
    @Override
    public void onRewardedVideoStarted() {

    }

    // 聯絡方式選擇
    private void contact() {
        CharSequence[] contactList = {"FB Messenger", "Line", "Telegram", "Twitter"};
        new AlertDialog.Builder(MainActivity.this)
                .setTitle(R.string.alertdialog_title_contact_list)
                .setIcon(R.drawable.ic_dialog_email)
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
                                        Toast.makeText(MainActivity.this, R.string.message_no_network, Toast.LENGTH_LONG).show(); // 無網路
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
                                        Toast.makeText(MainActivity.this, R.string.message_no_network, Toast.LENGTH_LONG).show(); // 無網路
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
                                        Toast.makeText(MainActivity.this, R.string.message_no_network, Toast.LENGTH_LONG).show(); // 無網路
                                    }
                                }
                                break;
                        }
                    }
                }).show();
    }

    private void startSample() {
        findViewById(R.id.btn_download).setOnClickListener(new View.OnClickListener() {
            Button btn = (Button) findViewById(R.id.btn_download);
            @Override
            public void onClick(View v) {
                btn.setEnabled(false); // 禁用按鈕

                // 顯示獎勵型影片廣告
                if (mAd.isLoaded()) {
                    mAd.show();
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
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle(R.string.alertdialog_title_warning)
                    .setIcon(R.drawable.ic_dialog_alert)
                    .setMessage(R.string.alertdialog_message_delete)
                    .setCancelable(false)
                    .setNegativeButton(R.string.alertdialog_button_ok,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    storage.deleteDirectory(resourcePackFile); // 刪除資料夾
                                    ((TextView)findViewById(R.id.btn_download)).setText(R.string.button_install); // 按鈕顯示安裝
                                    Toast.makeText(getApplication(), R.string.message_delete, Toast.LENGTH_SHORT).show();
                                }
                            }
                    )
                    .setPositiveButton(R.string.alertdialog_button_cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // 空白，退出 Dialog
                                }
                            }
                    )
                    .show();

            startSample();

            return true;
        } else if (id == R.id.action_share) {
            // 分享功能
            String subject = getString(R.string.app_name);
            String body = getString(R.string.text_share_body) + "\nhttps://play.google.com/store/apps/details?id=org.techplayer.toolbox.twlangnew";
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
            Uri uri=Uri.parse("market://details?id=org.techplayer.toolbox.twlangnew");
            Intent i=new Intent(Intent.ACTION_VIEW,uri);
            startActivity(i); // 啟動 Google Play (org.techplayer.toolbox.twlang)

            Toast.makeText(MainActivity.this, R.string.message_open_google_play, Toast.LENGTH_LONG).show();
        } else if (id == R.id.nav_contact) {
            contact(); // 聯絡方式選擇
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
        } else if (id == R.id.nav_github_source) {
            ConnectivityManager cManager=(ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = cManager.getActiveNetworkInfo();
            if (info != null && info.isAvailable()) {
                Uri uri=Uri.parse("https://github.com/MCNewsTools/Toolbox-Traditional-Chinese-Resource-Pack_StudioProjects");
                Intent i=new Intent(Intent.ACTION_VIEW,uri);
                startActivity(i); // 啟動 GitHub 網頁專案頁
            } else {
                Toast.makeText(MainActivity.this, R.string.message_no_network, Toast.LENGTH_LONG).show(); // 無網路
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
