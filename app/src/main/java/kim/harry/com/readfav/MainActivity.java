package kim.harry.com.readfav;

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

import com.koushikdutta.ion.Ion;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import kim.harry.com.readfav.model.Article;
import kim.harry.com.readfav.model.Domain;
import kim.harry.com.readfav.model.Time;

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
            }else{
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

        realm = Realm.getInstance(this);
        initUI();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent == null) {
            return;
        }

        url = getIntent().getStringExtra(EXTRA_NOTI_URL);
        if (TextUtils.isEmpty(url)) {
            url = getIntent().getStringExtra(Intent.EXTRA_TEXT);
        }else{
            focusUrl = url;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        menu.findItem(R.id.sorting).setChecked(archiveSort);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        archiveSort ^= true;
        invalidateOptionsMenu();
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
                ClipData.Item item = clipData.getItemAt(0);
                url = item.getText().toString();
                if (URLUtil.isNetworkUrl(url)) {
                    editText.getEditableText().clear();
                    editText.getEditableText().append(url);
                } else {
                    Toast.makeText(MainActivity.this, "invalid URL", Toast.LENGTH_SHORT).show();
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

    private void initUI() {
        freqDomain = ((TextView) findViewById(R.id.freq_domain));
        recyclerView = ((RecyclerView) findViewById(R.id.recyclerview));
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        gridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(gridLayoutManager);
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
        if (domains != null && domains.get(0) != null ) {
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
                freqDomain.setText("자주 보는 사이트는 : (" + topDomain.getCount() + "/" + sum+") \n"+ topDomain.getDomain());
            }
        }
        RealmResults<Article> rArticles = realm.where(Article.class).equalTo(Article.ARCHIVE_FEILD_NAME, archiveSort).findAllSorted("date", Sort.DESCENDING);
//        ReadAgainApplication.setFreqTime(this);
//        int freqHour = ApplicationPreferences.getInstance().getSaveTime();
//        int freqDay = ApplicationPreferences.getInstance().getDaySaveFreq();
//        Toast.makeText(this, "Freq DAY : "+ freqDay +" Hour : " + freqHour , Toast.LENGTH_SHORT).show();
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

    public class MainParseAsyncTask extends ParseProcessAsync{

        @Override
        protected void onPostExecute(final Article article) {
            super.onPostExecute(article);
            if (article == null) {
                Toast.makeText(MainActivity.this, R.string.parsing_fail, Toast.LENGTH_SHORT).show();
                return;
            }
            if (saveArticle(article)) {
                articles.add(0,article);
                adapter.notifyDataSetChanged();
                if (!TextUtils.isEmpty(article.getTitle())) {
                    Snackbar.make(findViewById(R.id.main), getString(R.string.succes, article.getTitle()), Snackbar.LENGTH_SHORT).setAction("Action", null)
                            .setAction("알람 설정", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent aralmIntent = new Intent(MainActivity.this, AlarmRegisterService.class);
                                    aralmIntent.putExtra(AlarmRegisterService.EXTRA_URL_PARAM, article.getUrl());
                                    Toast.makeText(MainActivity.this, "알림을 등록합니다.", Toast.LENGTH_SHORT).show();
                                    startService(aralmIntent);
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
                holder.description.setText(article.getContent());
                int color = getTextColor(article);
                holder.title.setTextColor(color);
                holder.description.setTextColor(color);
                if (TextUtils.equals(article.getUrl() ,focusUrl)) {
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
                        alertDialog.setNeutralButton(archiveSort ? R.string.recovery : R.string.archive, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                archiveArticle(clickedArticle, position);
                            }
                        });
                        alertDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
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
            int DEGREE = 5;
            long diff = (now.getTime() - article.getDate().getTime())/(1000*60); //시간단위로 구분.
            Log.d("diff" , String.valueOf(diff));
            if(diff < 1*DEGREE){
                colorCode = "#000000";
            } else if(diff < 2*DEGREE){
                colorCode = "#333333";
            }else if(diff < 3*DEGREE){
                colorCode = "#666666";
            }else if(diff < 4*DEGREE){
                colorCode = "#999999";
            }else if(diff < 5*DEGREE){
                colorCode = "#AAAAAA";
            } else {
                colorCode = "#EEEEEE";
            }
            int color;
            try {
                color = Color.parseColor(colorCode);
            }catch (IllegalArgumentException e){
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

//        Article rArticle = realm.createObject(Article.class);
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
        }else{
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
        rArticle.removeFromRealm();
        realm.commitTransaction();
        articles.remove(position);
        adapter.notifyDataSetChanged();
    }

    private void archiveArticle(Article clickedArticle, int position){
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
