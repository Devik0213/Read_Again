package kim.harry.com.readfav;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.koushikdutta.ion.Ion;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import kim.harry.com.readfav.model.Article;
import kim.harry.com.readfav.model.ParcelArticle;

/**
 * Created by naver on 16. 3. 10..
 */
public class AlarmHandleService extends IntentService {

    private static final String EXTRA_ARTICLE = "EXTRA_ARTICLE";

    public AlarmHandleService(){
        super("AlarmHandleService");
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public AlarmHandleService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null) {
            return;
        }

        Bundle bundle = intent.getExtras();
        ParcelArticle article = bundle.getParcelable(EXTRA_ARTICLE);
        showNotification(article);
    }

    public static PendingIntent newPendingIntent(Context context, Article article) {
        Intent intent = new Intent(context, AlarmHandleService.class);
        intent.putExtra(EXTRA_ARTICLE, new ParcelArticle(article));
        return PendingIntent.getService(context, 3399, intent, PendingIntent.FLAG_ONE_SHOT);
    }
    private void showNotification(ParcelArticle article) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setContentTitle(article.getTitle());
        builder.setContentText(article.getContent());
        builder.setSmallIcon(R.drawable.ic_archive_white_48dp);
        builder.setAutoCancel(true);
        Bitmap bitmap = null;
        try {
            bitmap = Ion.with(this).load(article.getThumbnail()).asBitmap().get(5000, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Log.e("ALARM", String.valueOf(e));
        } catch (ExecutionException e) {
            Log.e("ALARM", String.valueOf(e));
        } catch (TimeoutException e) {
            Log.e("ALARM", String.valueOf(e));
        }
        if (bitmap != null) {
            builder.setLargeIcon(bitmap);
        }

        NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();
        bigText.setBigContentTitle(article.getTitle());
        bigText.bigText(article.getContent());
        builder.setStyle(bigText);

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(MainActivity.EXTRA_NOTI_URL, article.getUrl());
        PendingIntent pendingIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(5939393, builder.build());
    }
}
