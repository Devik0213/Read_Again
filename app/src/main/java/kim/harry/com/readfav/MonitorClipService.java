package kim.harry.com.readfav;

import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.URLUtil;

public class MonitorClipService extends Service {
    private ClipboardManager clipboardManager;
    private ClipboardListener clipboardManagerListender;

    public MonitorClipService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }


    @Override
    public void onCreate() {
        super.onCreate();
        clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        clipboardManagerListender = new ClipboardListener() {
            @Override
            public void onPrimaryClipChanged() {
                if (clipboardManager == null) {
                    Log.e("URL : ", " error");
                }
                ClipData clipData = clipboardManager.getPrimaryClip();
                ClipData.Item item = clipData.getItemAt(0);

                if (item == null || TextUtils.isEmpty(item.getText()) || !URLUtil.isNetworkUrl(String.valueOf(item.getText()))) {
                    return;
                }
                String url = item.getText().toString();
                Log.d("URL ___ ", url);
                if (!TextUtils.isEmpty(url)) {
                    NotificationHelper.onClipboardChangeNotification(MonitorClipService.this, url);
                }
            }
        };
        clipboardManager.addPrimaryClipChangedListener(clipboardManagerListender);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        clipboardManager.removePrimaryClipChangedListener(clipboardManagerListender);
    }
}
