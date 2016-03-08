package kim.harry.com.readfav;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
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
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.koushikdutta.ion.Ion;

import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.Inflater;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import kim.harry.com.readfav.model.Article;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;

    private String url;
    private ArrayList<Article> articles = new ArrayList<Article>(0);
    private CustomAdapter adapter;
    private Realm realm;
    private ClipboardManager clipboardManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getIntent() != null) {
            url = getIntent().getStringExtra(Intent.EXTRA_TEXT);
            if (!TextUtils.isEmpty(url)) {
                showInput();
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

        realm = Realm.getInstance(MainActivity.this);
        initUI();

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
        RealmResults<Article> rArticles = realm.where(Article.class).findAllSorted("date", Sort.DESCENDING);
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
        protected void onPostExecute(Article article) {
            super.onPostExecute(article);
            if (article == null) {
                Toast.makeText(MainActivity.this, R.string.parsing_fail, Toast.LENGTH_SHORT).show();
                return;
            }
            if (saveArticle(article)) {
                articles.add(0,article);
                adapter.notifyDataSetChanged();
                if (!TextUtils.isEmpty(article.getTitle())) {
                    Snackbar.make(findViewById(R.id.main), getString(R.string.succes, article.getTitle()), Snackbar.LENGTH_SHORT).setAction("Action", null).show();
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
                ArticleItemViewHolder.ClickEventListener listener = new ArticleItemViewHolder.ClickEventListener() {

                    @Override
                    public void onClick(View v) {
                        Article clickedArticle = getArticle(position);
                        String url = clickedArticle.getUrl();
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(url));
                        startActivity(i);
                        readArticle(article);

                    }

                    @Override
                    public void onLongClick(View v) {
                        final Article clickedArticle = getArticle(position);
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                        alertDialog.setTitle(getString(R.string.alert_remove, clickedArticle.getTitle()));
                        alertDialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                removeArticle(clickedArticle, position);
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
        Article exist = realm.where(Article.class).equalTo("url", article.getUrl()).findFirst();
        if (exist != null) {
            return false;
        }
        realm.beginTransaction();
        Article rArticle = realm.createObject(Article.class);
        rArticle.setUrl(article.getUrl());
        rArticle.setTitle(article.getTitle());
        rArticle.setContent(article.getContent());
        rArticle.setThumbnail(article.getThumbnail());
        realm.commitTransaction();
        return true;
    }

    private void readArticle(Article article) {
        realm.beginTransaction();
        Article rArticle = realm.where(Article.class).equalTo("url", article.getUrl()).findFirst();
        rArticle.setRead(true);
        article.setRead(true);
        realm.commitTransaction();
        adapter.notifyDataSetChanged();
    }

    private void removeArticle(Article clickedArticle, int position) {

        realm.beginTransaction();
        Article rArticle = realm.where(Article.class).equalTo("url", clickedArticle.getUrl()).findFirst();
        rArticle.removeFromRealm();
        realm.commitTransaction();

        articles.remove(position);
        adapter.notifyDataSetChanged();
    }
}
