package kim.harry.com.readfav;

import android.app.Application;

import com.koushikdutta.ion.Ion;

/**
 * Created by naver on 16. 1. 10..
 */
public class ReadFavApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        initLibrary();
    }

    private void initLibrary() {
    }
}
