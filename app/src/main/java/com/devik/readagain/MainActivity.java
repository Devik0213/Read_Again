package com.devik.readagain;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.devik.readagain.model.Article;
import com.devik.readagain.model.Domain;
import com.devik.readagain.model.Time;
import com.google.firebase.crash.FirebaseCrash;
import com.koushikdutta.ion.Ion;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;


public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_NOTI_URL = "noti_url";
    private RecyclerView recyclerView;

    private String url;
    private ArrayList<Article> articles = new ArrayList<Article>(0);
    private CustomAdapter adapter;
    private Realm realm;
    private ClipboardManager clipboardManager;
    private String focusUrl;
    private boolean archiveSort = false;
    private TextView freqDomain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getIntent() != null) {
            url = getIntent().getStringExtra(EXTRA_NOTI_URL);
            if (!TextUtils.isEmpty(url)) {
                focusUrl = url;
            } else {
                url = getIntent().getStringExtra(Intent.EXTRA_TEXT);
            }
        }
        clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showInput();
            }
        });

        // Create the Realm configuration
//        RealmConfiguration realmConfig = new RealmConfiguration.Builder(this).build();
//        // Open the Realm for the UI thread.
        FirebaseCrash.report(new UnSupportException("테스트"));
        realm = Realm.getDefaultInstance();
        initUI();
    }
//
//    @Override
//    protected void onNewIntent(Intent intent) {
//        super.onNewIntent(intent);
//        if (intent == null) {
//            return;
//        }
//
//        url = getIntent().getStringExtra(EXTRA_NOTI_URL);
//        if (TextUtils.isEmpty(url)) {
//            url = getIntent().getStringExtra(Intent.EXTRA_TEXT);
//        }else{
//            focusUrl = url;
//        }
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        menu.findItem(R.id.sorting).setChecked(archiveSort);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        archiveSort ^= true;
        Toast.makeText(this, getText(archiveSort ? R.string.archive : R.string.basic) + "모드", Toast.LENGTH_SHORT).show();
        refreshData();
        return super.onOptionsItemSelected(item);
    }

    void showInput() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
        final View input = LayoutInflater.from(MainActivity.this).inflate(R.layout.input, null);
        final EditText editText = (EditText) input.findViewById(R.id.input_url);
        if (!TextUtils.isEmpty(url)) {
            editText.getEditableText().append(url);
        }
        dialog.setView(input);
        Button copyFromClipboard = (Button) input.findViewById(R.id.copy_button);
        copyFromClipboard.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ClipData clipData = clipboardManager.getPrimaryClip();
                if (clipData == null) {
                    return;
                }
                ClipData.Item item = clipData.getItemAt(0);
                url = Extractor.getUrl(item.getText().toString());
                if (URLUtil.isNetworkUrl(url)) {
                    editText.getEditableText().clear();
                    editText.getEditableText().append(url);
                } else {
                    Toast.makeText(MainActivity.this, "invalid URL : " + url , Toast.LENGTH_SHORT).show();
                }
            }
        });

        dialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                parsingFromUrl(url);
            }
        });
        dialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    GridLayoutManager getGrid(){
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        gridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        return gridLayoutManager;
    }

    LinearLayoutManager getLinear(){
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        return linearLayoutManager;

    }
    private void initUI() {
        freqDomain = ((TextView) findViewById(R.id.freq_domain));
        recyclerView = ((RecyclerView) findViewById(R.id.recyclerview));
        recyclerView.setLayoutManager(getGrid());
        adapter = new CustomAdapter();
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!TextUtils.isEmpty(url)) {
            showInput();
        }
        refreshData();
    }

    private void refreshData() {
        RealmResults<Domain> domains = realm.where(Domain.class).findAll();

        if (domains.size() != 0 && domains.get(0) != null) {
            int topDomainCount = 0;
            int sum = 0;
            Domain topDomain = null;

            for (Domain domain : domains) {
                if (domain.getCount() > topDomainCount) {
                    topDomainCount = domain.getCount();
                    topDomain = domain;
                }
                sum += domain.getCount();
            }
            if (topDomain != null) {
                freqDomain.setText("자주 보는 사이트는 : (" + topDomain.getCount() + "/" + sum + ") \n" + topDomain.getDomain());
            }
        }
        RealmResults<Article> rArticles = realm.where(Article.class).equalTo(Article.ARCHIVE_FEILD_NAME, archiveSort).findAllSorted(Article.SAVE_DATE, Sort.DESCENDING);
        articles.clear();
        articles.addAll(rArticles);
        adapter.notifyDataSetChanged();

    }

    public void parsingFromUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            url = "http://www.naver.com";
        }

        MainParseAsyncTask processAsync = new MainParseAsyncTask();
        processAsync.execute(url);
    }

    public class MainParseAsyncTask extends ParseProcessAsync {

        @Override
        protected void onPostExecute(final Article article) {
            super.onPostExecute(article);
            if (article == null) {
                Toast.makeText(MainActivity.this, getString(R.string.parsing_fail, "입력한 URL 에서 데이터를 제공하지않음"), Toast.LENGTH_SHORT).show();
                return;
            }
            if (saveArticle(article)) {
                articles.add(0, article);
                adapter.notifyDataSetChanged();
                if (!TextUtils.isEmpty(article.getTitle())) {
                    Snackbar.make(findViewById(R.id.main), getString(R.string.succes, article.getTitle()), Snackbar.LENGTH_SHORT).setAction("Action", null)
                            .setAction("알람 설정", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    //미지원
                                    FirebaseCrash.report(new UnSupportException("저장후>알람설정"));

//                                    Intent aralmIntent = new Intent(MainActivity.this, AlarmRegisterService.class);
//                                    aralmIntent.putExtra(AlarmRegisterService.EXTRA_URL_PARAM, article.getUrl());
//                                    Toast.makeText(MainActivity.this, "알림을 등록합니다.", Toast.LENGTH_SHORT).show();
//                                    startService(aralmIntent);
                                }
                            }).show();
                }
                return;
            }
            Toast.makeText(MainActivity.this, R.string.already_exist, Toast.LENGTH_SHORT).show();
        }
    }


    public class CustomAdapter extends RecyclerView.Adapter<ArticleItemViewHolder> {


        @Override
        public ArticleItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.
                    from(MainActivity.this).
                    inflate(R.layout.article_item, parent, false);
            return new ArticleItemViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(ArticleItemViewHolder holder, final int position) {
            final Article article = articles.get(position);
            if (article != null) {
                if (article.getThumbnail() != null) {
                    Ion.with(MainActivity.this).load(article.getThumbnail()).intoImageView(holder.thumbnail);
                } else {
                    holder.thumbnail.setImageDrawable(getDrawable(R.drawable.notification_template_icon_bg));
                }
                holder.title.setText(article.getTitle());
                holder.description.setText(String.valueOf(article.getContent()));
                int color = getTextColor(article);
                holder.title.setTextColor(color);
                holder.description.setTextColor(color);
                if (TextUtils.equals(article.getUrl(), focusUrl)) {
                    holder.title.setTextColor(Color.RED);
                    recyclerView.getLayoutManager().scrollToPosition(position);
                }
                ArticleItemViewHolder.ClickEventListener listener = new ArticleItemViewHolder.ClickEventListener() {
                    @Override
                    public void onClick(View v) {
                        Article clickedArticle = getArticle(position);
                        readArticle(clickedArticle);
                        String url = clickedArticle.getUrl();
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(url));
                        startActivity(i);
                    }

                    @Override
                    public void onLongClick(View v) {
                        final Article clickedArticle = getArticle(position);
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                        alertDialog.setTitle(getString(R.string.alert_dialog_title));
                        alertDialog.setMessage(clickedArticle.getTitle());
                        alertDialog.setPositiveButton(R.string.remove, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                removeArticle(clickedArticle, position);
                            }
                        });
                        alertDialog.setNeutralButton(R.string.share, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                                shareIntent.putExtra(Intent.EXTRA_TEXT, article.getTitle() + "\n" + article.getUrl());
                                shareIntent.setType("text/plain");
                                startActivity(Intent.createChooser(shareIntent, "공유할 App 을 고르세요"));
                            }
                        });
                        alertDialog.setNegativeButton(R.string.remove, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                removeArticle(clickedArticle, position);
                            }
                        });
                        alertDialog.setNegativeButton(archiveSort ? R.string.recovery : R.string.archive, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                archiveArticle(clickedArticle, position);
                            }
                        });

                        alertDialog.show();
                    }
                };
                holder.setEventListener(listener);
            }
        }

        private int getTextColor(Article article) {
            String colorCode;
            Date now = new Date();
            long dayAgo = (now.getTime() - article.getDate().getTime()) / (1000 * 60 * 60 * 24); //일 단위로 구분.
            Log.d("diff", String.valueOf(dayAgo));
            if (dayAgo < 1) {
                colorCode = "#000000";
            } else if (dayAgo < 2) {
                colorCode = "#333333";
            } else if (dayAgo < 3) {
                colorCode = "#666666";
            } else if (dayAgo < 4) {
                colorCode = "#999999";
            } else if (dayAgo < 5) {
                colorCode = "#AAAAAA";
            } else {
                colorCode = "#EEEEEE";
            }
            int color;
            try {
                color = Color.parseColor(colorCode);
            } catch (IllegalArgumentException e) {
                Log.e("COLOR", String.valueOf(e));
                color = Color.BLACK;
            }
            return color;
        }

        Article getArticle(int position) {
            return articles.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public int getItemCount() {
            return articles.size();
        }

    }

    private boolean saveArticle(Article article) {
        Article exist = realm.where(Article.class).equalTo(Article.URL_FEILD_NAME, article.getUrl()).findFirst();
        if (exist != null) {
            return false;
        }

        realm.beginTransaction();
        realm.copyToRealm(article);
        realm.commitTransaction();

        String domainUrl;
        try {
            domainUrl = getDomainName(article.getUrl());
        } catch (URISyntaxException e) {
            Log.e("AAA", String.valueOf(e));
            domainUrl = "www.naver.com";
        }

        realm.beginTransaction();
        Domain domain = realm.where(Domain.class).equalTo(Domain.DOMAIN, domainUrl).findFirst();
        if (domain == null) {
            domain = new Domain();
            domain.setName(article.getTitle());
            domain.setDomain(domainUrl);
            domain.setCount(1);
            realm.copyToRealmOrUpdate(domain);
        } else {
            int count = domain.getCount() + 1;
            domain.setCount(count);
        }
        realm.commitTransaction();

        Time time = new Time(ActionType.SAVE.ordinal(), new Date());
        realm.beginTransaction();
        realm.copyToRealm(time);
        realm.commitTransaction();
        return true;
    }

    private void readArticle(Article article) {
        Article rArticle = realm.where(Article.class).equalTo(Article.URL_FEILD_NAME, article.getUrl()).findFirst();
        realm.beginTransaction();
        if (rArticle == null) {
            realm.copyFromRealm(article);
        }
        realm.commitTransaction();

        Time time = new Time(ActionType.READ.ordinal(), new Date());
        realm.beginTransaction();
        realm.copyToRealm(time);
        realm.commitTransaction();
        adapter.notifyDataSetChanged();
    }

    private void removeArticle(Article clickedArticle, int position) {
        Article rArticle = realm.where(Article.class).equalTo(Article.URL_FEILD_NAME, clickedArticle.getUrl()).findFirst();
        if (rArticle == null) {
            return;
        }
        realm.beginTransaction();
        rArticle.deleteFromRealm();
        realm.commitTransaction();
        articles.remove(position);
        adapter.notifyDataSetChanged();
    }

    private void archiveArticle(Article clickedArticle, int position) {
        Article rArticle = realm.where(Article.class).equalTo(Article.URL_FEILD_NAME, clickedArticle.getUrl()).findFirst();
        // 아카이브 하는날의 우선순위를 주기위한 값.
        if (rArticle == null) {
            return;
        }
        realm.beginTransaction();
        rArticle.setArchive(!archiveSort);//현재 아카이브 상태의 반대로.
        rArticle.setDate(new Date());
        realm.commitTransaction();
        articles.remove(position);
        adapter.notifyDataSetChanged();
    }

    public static String getDomainName(String url) throws URISyntaxException {
        URI uri = null;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            Log.e("URL PARSe", String.valueOf(e));
        }

        String domain = uri.getHost();
        return domain.startsWith("www.") ? domain.substring(4) : domain;
    }

}
