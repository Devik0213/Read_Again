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
import android.widget.Toast;

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
        Notification.Action parseAction = new Notification.Action(R.drawable.ic_add_box_black_48dp, context.getString(R.string.ok), pendingIntent);


        Notification.Builder notificationBuilder = new Notification.Builder(context);
        notificationBuilder.setContentTitle("Copy!!").setContentText(url);
        notificationBuilder.setSmallIcon(R.drawable.ic_move_to_inbox_black_48dp);
        notificationBuilder.setWhen(System.currentTimeMillis());
        notificationBuilder.addAction(parseAction);
        notificationBuilder.setAutoCancel(true);

        notificationBuilder.setPriority(Notification.PRIORITY_HIGH).setDefaults(Notification.DEFAULT_VIBRATE);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify("TAG", notificationId, notificationBuilder.build());
    }

    static void onParseCompleted(Context context, String msg){
//        if (TextUtils.equals(msg, ParseIntentService.RESULT_SUCCES)) {
//            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
//        }
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }
}
