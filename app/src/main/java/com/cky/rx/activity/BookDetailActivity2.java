package com.cky.rx.activity;

import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;

import com.cky.rx.R;
import com.cky.rx.adapter.BookDetailAdapter;
import com.cky.rx.fragment.base.BaseActivity;
import com.cky.rx.fragment.base.BundleKey;
import com.cky.rx.model.BookDetailResult;
import com.cky.rx.model.BookItemToShow;
import com.cky.rx.network.Network;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.schedulers.Schedulers;

public class BookDetailActivity2 extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = BookDetailActivity2.class.getSimpleName();
    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.fab)
    FloatingActionButton fab;
//    @Bind(R.id.drawer_layout)
//    DrawerLayout drawer;
    @Bind(R.id.rv_book_detail)
    RecyclerView rvBookDetail;
    @Bind(R.id.progressBar)
    ProgressBar pbLoading;
    @Bind(R.id.app_bar_layout)
    AppBarLayout appBarLayout;

    private BookDetailAdapter adapter;

    private String BookId;
    private String downloadLink;
    private String BookName;

    public static void start(Context context, BookItemToShow bookItemToShow) {
        Intent intent = new Intent(context, BookDetailActivity2.class);
        intent.putExtra(BundleKey.BOOKID, bookItemToShow.id);
        context.startActivity(intent);
    }

    Observer<BookDetailResult> observer = new Observer<BookDetailResult>() {
        @Override
        public void onCompleted() {
            Log.d(TAG, "onCompleted------->");
        }

        @Override
        public void onError(Throwable e) {
            Log.d(TAG, "onError------->" + e.getMessage());
            if (pbLoading.getVisibility() == View.VISIBLE) {
                pbLoading.setVisibility(View.GONE);
            }
        }

        @Override
        public void onNext(BookDetailResult bookDetailResult) {

            downloadLink = bookDetailResult.Download;
            BookName = bookDetailResult.Title;
            toolbar.setTitle(BookName);
            Log.d(TAG, "onNext------->" + bookDetailResult.Description);
            adapter = new BookDetailAdapter(BookDetailActivity2.this, bookDetailResult);
            rvBookDetail.setAdapter(adapter);
            if (rvBookDetail.getVisibility() == View.GONE) {
                rvBookDetail.setVisibility(View.VISIBLE);
            }
            if (pbLoading.getVisibility() == View.VISIBLE) {
                pbLoading.setVisibility(View.GONE);
            }
        }
    };

    private void getBookDetail(String bookId) {
        subscription = Network.getIteBooksApi()
                .getBookDetail(bookId)
                .doOnSubscribe(new Action0() {
                    @Override
                    public void call() {

                        if (pbLoading.getVisibility() == View.GONE) {
                            pbLoading.setVisibility(View.VISIBLE);
                        }
                        if (rvBookDetail.getVisibility() == View.VISIBLE) {
                            rvBookDetail.setVisibility(View.GONE);
                        }

                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail2);

        ButterKnife.bind(this);

        rvBookDetail.setLayoutManager(new LinearLayoutManager(BookDetailActivity2.this));
        rvBookDetail.setHasFixedSize(true);

        //状态栏透明化 以实现 抽屉的 全屏化
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window = getWindow();
            window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (downloadLink != null) {
                    showTipDialog(downloadLink);
                }

                /*
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                        */
            }
        });
/*
        //抽屉
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
*/
        BookId = getIntent().getStringExtra(BundleKey.BOOKID);
        getBookDetail(BookId);

    }

    @Override
    public void onBackPressed() {
/*
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
*/
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.book_detail_activity2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        /*
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        */
        return super.onOptionsItemSelected(item);
    }

    private void showTipDialog(final String downloadLink) {

        String message = getString(R.string.go_to_download_without_name);
        if (BookName != null) {
           message = getString(R.string.go_to_download, BookName);
        } else {
            BookName = BookId;
        }
        new AlertDialog.Builder(BookDetailActivity2.this)
                .setTitle(getString(R.string.tip))
                .setMessage(message)
                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //String referer = "http://www.it-ebooks.info/book/"+BookId+"/";
                        String referer = getString(R.string.referer, BookId);
                        /*
                        Uri uri = Uri.parse(downloadLink);
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.setData(uri);
                        BookDetailActivity.this.startActivity(intent);
                        */
                        DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);

                        Uri uri = Uri.parse(downloadLink);
                        DownloadManager.Request request = new DownloadManager.Request(uri);

                        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
                        request.addRequestHeader("Referer", referer);
                        request.allowScanningByMediaScanner();
                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

                        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                            request.setDestinationInExternalPublicDir(
                                    getString(R.string.download_file_folder_name),
                                    getString(R.string.download_file_name, BookName)
                            );
                        }

                        long id = downloadManager.enqueue(request);

                        //new Thread(getTrueDownloadLinkTask).start();

                    }
                })
                .show();
    }
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        /*
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }
        */
        //drawer.closeDrawer(GravityCompat.START);

        return true;
    }
}