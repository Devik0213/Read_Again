package com.devik.readagain;

import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.URLUtil;

import java.net.URL;

public class MonitorClipService extends Service {
    private ClipboardManager clipboardManager;
    private ClipboardListener readAgainClipboardManagerListener;

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
        readAgainClipboardManagerListener = new ClipboardListener() {
            @Override
            public void onPrimaryClipChanged() {
                if (clipboardManager == null) {
                    Log.e("URL : ", " error");
                }
                ClipData clipData = clipboardManager.getPrimaryClip();
                ClipData.Item item = clipData.getItemAt(0);

                if (item == null) {
                    return;
                }
                String rawData = String.valueOf(item.getText());
                String url = Extractor.getUrl(rawData);
                if (!TextUtils.isEmpty(url)) {
                    NotificationHelper.onClipboardChangeNotification(MonitorClipService.this, url);
                }
            }
        };
        clipboardManager.addPrimaryClipChangedListener(readAgainClipboardManagerListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        clipboardManager.removePrimaryClipChangedListener(readAgainClipboardManagerListener);
    }
}
