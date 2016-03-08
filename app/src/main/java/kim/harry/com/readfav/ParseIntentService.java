package kim.harry.com.readfav;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import org.jsoup.Jsoup;

import java.io.IOException;

import io.realm.Realm;
import kim.harry.com.readfav.model.Article;

/**
 * Created by Naver on 16. 2. 24..
 */
public class ParseIntentService extends IntentService {

    public static final String RESULT_SUCCES = "Achieve succes";
    public static final String RESULT_FAIL = "Achieve fail";

    public ParseIntentService() {
        super("ParseIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null) {
            return;
        }

        String url = intent.getStringExtra(NotificationHelper.EXTRA_PARSE_URL);
        if (url == null) {
            return;
        }
        Log.e("URL1URL1URL1URL1URL1URL1URL1URL1URL1URL1URL1 : " , url);
        org.jsoup.nodes.Document result = null;
        try {
            result = Jsoup.connect(url).get();
        } catch (IOException e) {
            NotificationHelper.onParseCompleted(this, RESULT_FAIL);
            Log.e("TAG", e.getCause().toString());
            return;
        }
        if (result == null) {
            return;
        }
        String title = result.title();
        if (title == null) {
            title = "No Title            ";
        }
        String content = result.select("meta[property=og:description]").attr("content");
        if (content == null) {
            content = "No content description";
        }
        String imageUrl = result.select("meta[property=og:image]").attr("content");
        String articleUrl = result.location();
        if (articleUrl == null) {
            articleUrl = url;
        }
        Article article = new Article(title, content, imageUrl, articleUrl);

        Realm realm = Realm.getInstance(this);

        Article exist = realm.where(Article.class).equalTo("url", article.getUrl()).findFirst();
        if (exist != null) {
            NotificationHelper.onParseCompleted(this, RESULT_FAIL);
            return ;
        }
        realm.beginTransaction();
        Article rArticle = realm.createObject(Article.class);
        rArticle.setUrl(article.getUrl());
        rArticle.setTitle(article.getTitle());
        rArticle.setContent(article.getContent());
        rArticle.setThumbnail(article.getThumbnail());
        realm.commitTransaction();
        NotificationHelper.onParseCompleted(this, RESULT_SUCCES);
    }
}
