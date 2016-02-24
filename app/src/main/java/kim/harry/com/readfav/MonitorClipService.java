package kim.harry.com.readfav;

import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

public class MonitorClipService extends Service {
    private ClipboardManager clipboardManager;

    public MonitorClipService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        clipboardManager.addPrimaryClipChangedListener(new ClipboardListener(){
            @Override
            public void onPrimaryClipChanged() {
                if (clipboardManager == null) {
                    Log.e("URL : " ," error");
                }
                ClipData clipData = clipboardManager.getPrimaryClip();
                ClipData.Item item = clipData.getItemAt(0);

                String url = item.getText().toString();
                Log.e("URL : " , url);
                if (!TextUtils.isEmpty(url)) {
                    NotificationHelper.showNotification(MonitorClipService.this , url);
                }
            }
        });
    }
}
