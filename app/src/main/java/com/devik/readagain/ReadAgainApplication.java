package com.devik.readagain;

import android.app.Application;
import android.content.Intent;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by naver on 16. 1. 10..
 */
public class ReadAgainApplication extends Application {

    private static final long DAY = 1000 * 60 * 60 * 24;
    public static final int ACTION_FREQUENCY_DAY = 0;
    public static final int ACTION_FREQUENCY_HOUR = 1;
    private static ReadAgainApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        startService(new Intent(this, MonitorClipService.class));
        initDB();
    }

    private void initDB() {
        // Create a RealmConfiguration that saves the Realm file in the app's "files" directory.
        RealmConfiguration realmConfig = new RealmConfiguration.Builder(this).deleteRealmIfMigrationNeeded().build();
        Realm.setDefaultConfiguration(realmConfig);

        // RealmConfiguration은 빌더 패턴에 의해 생성됩니다.
        // Realm 파일은 Context.getFilesDir()에 위치하면 이름은 "myrealm.realm"입니다.
//        RealmConfiguration config = new RealmConfiguration.Builder(this)
//                .name("readAgain.realm")
//                .schemaVersion(1)
//                .build();
//        // 설정을 사용합니다.
//        Realm.setDefaultConfiguration(config);
//        Realm realm = Realm.getDefaultInstance();
    }

//    public static int[] getFrequentlyTime(Context context, ActionType actionType) {
//        Realm realm = Realm.getInstance(context);
//        RealmResults<Time> times = realm.where(Time.class).equalTo("actionType", actionType.ordinal()).findAllSorted("date", Sort.DESCENDING);
//
//
//        int prevActionTime = (actionType == ActionType.READ) ? ApplicationPreferences.getInstance().getReadTime() : ApplicationPreferences.getInstance().getSaveTime();
//        ArrayList<Integer> hours = new ArrayList<Integer>();
//        ArrayList<Integer> dayFrequencies = new ArrayList<Integer>();
//        Date nowDate = new Date();
//        // 최신순 정렬이므로, 오늘과의 차이부터 구해간다.
//        for (Time time : times) {
//            Date timeDate = time.getDate();
//            Calendar calendar = Calendar.getInstance();
//            calendar.setTime(timeDate);
//            Calendar preCalendar = Calendar.getInstance();
//            preCalendar.setTime(nowDate);
//            long diff = (nowDate.getTime() - timeDate.getTime()) / DAY;
//            int dayFrequency = (int) Math.ceil(diff);
//            dayFrequencies.add(dayFrequency);
//            int saveHour = calendar.get(Calendar.HOUR_OF_DAY);
//            hours.add(saveHour);
//        }
//
//        if (dayFrequencies.size()== 0) {
//            return new int[]{0,0};
//        }
//        int sumFrequency = 0 ;
//        for (Integer dayFrequency : dayFrequencies) {
//            sumFrequency += dayFrequency;
//        }
//        int avgFrequency = sumFrequency/dayFrequencies.size();
//
//        int[] timeDistribution = new int[24];
//        for (Integer hour : hours) {
//            timeDistribution[hour]++;
//        }
//        int maxCount = 0;
//        int freqHour = 0;
//        for (int hour = 0; hour < timeDistribution.length; hour++) {
//            if (timeDistribution[hour] > maxCount) {
//                maxCount = timeDistribution[hour];
//                freqHour = hour;
//            }
//        }
//        int diff = prevActionTime - freqHour;
//        /**
//         * diff < 0
//         * 1)  -3 = 12 - 15
//         * freqHour += diff/2
//         * diff > 0
//         * 2) 3 = 22 - 19;
//         * freqHoyr += diff/2
//         * 3 -23 = 0 - 23;
//         * -23 + 0
//          */
//        freqHour += diff/2;
//        int[] frequentHours = {avgFrequency, Math.abs(freqHour)};
//        return frequentHours;
//    }
}
