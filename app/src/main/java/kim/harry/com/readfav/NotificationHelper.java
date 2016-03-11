package kim.harry.com.readfav;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.webkit.URLUtil;
import android.widget.Toast;

import kim.harry.com.readfav.model.Article;

/**
 * Created by Naver on 16. 2. 24..
 */
public class NotificationHelper {
    public static final int notificationId = 0x383838;
    private static final String ACTION_PARSE = "com.readAgain.ACTION_PARSE";
    public static final String EXTRA_PARSE_URL = "extra_parse_url";
    private static final String ACTION_LAUNCH = "com.readAgain.ACTION_LAUNCH";

    static void onClipboardChangeNotification(Context context, String url){
        Intent parseIntent = new Intent(context, ParseIntentService.class);
        parseIntent.putExtra(EXTRA_PARSE_URL, url);
        final long requestCode = (int) System.currentTimeMillis();
        PendingIntent pendingIntent = PendingIntent.getService(context, (int) requestCode, parseIntent, PendingIntent.FLAG_ONE_SHOT);
        //32짜리로 만들어야함.
        NotificationCompat.Action parseAction = new NotificationCompat.Action(R.drawable.ic_move_to_inbox_black_48dp, context.getString(R.string.save), pendingIntent);

        if (!URLUtil.isNetworkUrl(url)) {
            return;
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context);
        notificationBuilder.setContentTitle("이거 저장 해둘까?").setContentText(url);
        notificationBuilder.setSmallIcon(R.drawable.ic_move_to_inbox_black_48dp);
        notificationBuilder.setWhen(System.currentTimeMillis());
        notificationBuilder.addAction(parseAction);
        notificationBuilder.addAction(new NotificationCompat.Action(R.drawable.ic_clear_black_48dp, "아니", null));
        notificationBuilder.setAutoCancel(true);

        notificationBuilder.setPriority(Notification.PRIORITY_HIGH).setDefaults(Notification.DEFAULT_VIBRATE);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify("TAG", notificationId, notificationBuilder.build());
    }

    static void onParseCompleted(Context context, Article article){
        Intent mainIntent = new Intent(context, MainActivity.class);
        mainIntent.putExtra(MainActivity.EXTRA_NOTI_URL, article.getUrl());
        final long requestCode = (int) System.currentTimeMillis();
        PendingIntent pendingIntent = PendingIntent.getActivity(context, (int) requestCode, mainIntent, PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context);
        notificationBuilder.setContentTitle(context.getString(R.string.complete))
                .setContentText(context.getString(R.string.go_to_main, article.getTitle()))
                .setSmallIcon(R.drawable.ic_move_to_inbox_black_48dp)
                .setWhen(System.currentTimeMillis())
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify("TAG", notificationId, notificationBuilder.build());
    }
}
