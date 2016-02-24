package kim.harry.com.readfav;

import android.app.Application;
import android.content.Intent;

import com.koushikdutta.ion.Ion;

/**
 * Created by naver on 16. 1. 10..
 */
public class ReadAgainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        startService(new Intent(this, MonitorClipService.class));
        initLibrary();
    }

    private void initLibrary() {
    }

}
