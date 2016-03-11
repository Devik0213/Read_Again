package kim.harry.com.readfav;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.URLUtil;

import org.jsoup.Jsoup;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Date;

import io.realm.Realm;
import io.realm.RealmResults;
import kim.harry.com.readfav.model.Article;
import kim.harry.com.readfav.model.Domain;
import kim.harry.com.readfav.model.Time;

/**
 * Created by Naver on 16. 2. 24..
 */
public class ParseIntentService extends IntentService {

    public static final String RESULT_SUCCES = "Achieve succes";
    public static final String RESULT_FAIL = "Achieve fail";
    private NotificationManager notificationManager;

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
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NotificationHelper.notificationId);

        org.jsoup.nodes.Document result = null;
        try {
            result = Jsoup.connect(url).get();
//            if (result != null) {
//                if (URLUtil.isNetworkUrl(result.location())) {
//                    result = Jsoup.connect(url).get();
//                }
//            }
        } catch (IOException ie) {
            Log.e("TAG", String.valueOf(ie));
            return;
        } catch (Exception e){
            Log.e("TAG", String.valueOf(e));
        }

        if (result == null) {
            return;
        }

        String title = result.title();
        if (TextUtils.isEmpty(title)) {
            title = "No Title            ";
        }
        String content = result.select("meta[property=og:description]").attr("content");
        if (TextUtils.isEmpty(content)) {
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
            return ;
        }
        realm.beginTransaction();
        realm.copyToRealm(article);
        realm.commitTransaction();

        Time time = new Time(ActionType.SAVE.ordinal(), new Date());
        realm.beginTransaction();
        realm.copyToRealm(time);
        realm.commitTransaction();

        String domainUrl;
        try {
            domainUrl = MainActivity.getDomainName(article.getUrl());
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
            return;
        }else{
            int count = domain.getCount() + 1;
            domain.setCount(count);
        }
        realm.commitTransaction();
        RealmResults<Domain> domains = realm.where(Domain.class).findAll();
        for (Domain domainss : domains) {
            Log.d("DOMAIN " , String.valueOf(domainss.getCount()));
        }
        NotificationHelper.onParseCompleted(this, article);
    }
}
