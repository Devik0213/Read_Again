package kim.harry.com.readfav;

import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.Date;

import io.realm.Realm;
import kim.harry.com.readfav.model.Article;
import kim.harry.com.readfav.model.Time;

/**
 * Created by Naver on 16. 2. 24..
 */
public class ParseProcessAsync extends AsyncTask<String, Void, Article> {
    @Override
    protected Article doInBackground(String... params) {
        String url = params[0];
        try {
            org.jsoup.nodes.Document result = Jsoup.connect(url).get();
            Log.d("PARSE msg", (result == null ? "NULL" :result.toString()));
            if (result == null) {
                return null;
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
//            article.setDate(new Date());
            return article;
        } catch (IOException e) {
            Log.d("TAG", e.toString());
        } catch (Exception e){
            return null;
        }
        return null;
    }
}
