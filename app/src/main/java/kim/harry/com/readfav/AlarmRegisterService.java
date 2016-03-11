package kim.harry.com.readfav;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.builder.Builders;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import kim.harry.com.readfav.model.Article;
import kim.harry.com.readfav.model.Domain;

/**
 * Created by naver on 16. 3. 10..
 */
public class AlarmRegisterService extends IntentService{
    public static final String EXTRA_URL_PARAM = "url";

    public AlarmRegisterService(){
        super("AlarmRegisterService");
    }
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public AlarmRegisterService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("AlarmRegisterService" , "onHandleIntent");
        if (intent == null) {
            return;
        }

//        String url = intent.getStringExtra(EXTRA_URL_PARAM);
//        if (!TextUtils.isEmpty(url)) {
//            return;
//        }

        process();
    }

    private void process() {
        Article article = getArticle();
        if (article == null) {
            return;
        }
        registerAlarm(article);
    }

    void registerAlarm(Article article){
        int dayReadFreq = ApplicationPreferences.getInstance().getDatReadFreq();
        int readTime = ApplicationPreferences.getInstance().getReadTime();
        if (dayReadFreq == 0) {
            dayReadFreq = 1;
        }
        Date now = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(now);
        /**
         * Real :하루추가.
         */
//        c.add(Calendar.DATE, 1);
//        c.set(Calendar.HOUR_OF_DAY, readTime);
        /**
         * DEV : 4분후,
         */
        c.set(Calendar.MINUTE, 4);

        PendingIntent pendingIntent = AlarmHandleService.newPendingIntent(this, article);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
        alarmManager.set(AlarmManager.RTC_WAKEUP, c.getTime().getTime(), pendingIntent);

        /**
         TODO Mashmallow 대응을 위한 코드.
         *
         //        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
         //            alarmManager.setAllow();
         //        }else{
         //            alarmManager.set(AlarmManager.RTC_WAKEUP, targetMillis, LocalPushBroadcastReceiver.newPendingIntent(this));
         //        }
         */
    }

    public Article getArticle() {
        Realm realm = Realm.getInstance(this);

        //알람할께 있으면 가장 오래된 알람.
//        Article article = realm.where(Article.class).not().equalTo(Article.ALARM, 1).findAllSorted(Article.ALARM, Sort.ASCENDING).first();
        Article article = null;
        if (article == null) { // 알람설정된게 없다면,
            try {
                article = realm.where(Article.class)
                        .equalTo("read", false)
                        .findAllSorted("date", Sort.ASCENDING).first();
            } catch (Exception e){
                Log.e("DB", String.valueOf(e));
            }
        }

        if (article == null) {
//            Domain domain = realm.where(Domain.class).findAllSorted("count", Sort.DESCENDING).first();
            RealmResults<Domain> domains = realm.where(Domain.class).findAll();

            int maxCount = 0;
            int sum = 0;
            Domain topDomain = null;
            for (Domain domain : domains) {
                int count = domain.getCount();
                if (count > maxCount) {
                    maxCount = domain.getCount();
                    topDomain = domain;
                }
                sum += count;
            }
            if (topDomain.getCount() > 3) {
                topDomain = null;
            }
            if (topDomain == null) {
                topDomain.setName("NAVER");
                topDomain.setDomain("www.naver.com");
                topDomain.setFavicon("http://www.naver.com/favicon.ico");
            }

            article = new Article();
            article.setTitle(topDomain.getName());
            article.setUrl(topDomain.getDomain());
            article.setThumbnail(topDomain.getFavicon());
            article.setContent(getString(R.string.recom_site, topDomain.getCount() ,sum, topDomain.getName()));
        }
        return article;
    }
}
