package kim.harry.com.readfav;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

/**
 * Created by Naver on 16. 2. 24..
 */
public class NotificationHelper {
    private static final String ACTION_PARSE = "com.readAgain.ACTION_PARSE";
    private static final String EXTRA_PARSE_URL = "extra_parse_url";
    private static final String ACTION_LAUNCH = "com.readAgain.ACTION_LAUNCH";

    static void showNotification(Context context, String url){
        Intent parseIntent = new Intent(ACTION_PARSE);
        parseIntent.putExtra(EXTRA_PARSE_URL, url);
        PendingIntent pendingCancelIntent = PendingIntent.getBroadcast(context, 1, parseIntent, PendingIntent.FLAG_ONE_SHOT);
        NotificationCompat.Action parseAction = new NotificationCompat.Action(R.drawable.ic_download_comp, context.getString(R.string.ok), pendingCancelIntent);


        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context);
        notificationBuilder.setContentTitle("Copy!!").setContentText(url);
        notificationBuilder.setSmallIcon(R.drawable.ic_archive_white_48dp);
        notificationBuilder.setWhen(System.currentTimeMillis());
        notificationBuilder.addAction(parseAction);

        notificationBuilder.setPriority(Notification.PRIORITY_MAX);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify("TAG", 1, notificationBuilder.build());
    }
}
