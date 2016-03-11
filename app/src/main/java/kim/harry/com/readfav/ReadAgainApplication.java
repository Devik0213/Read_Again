package kim.harry.com.readfav;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import kim.harry.com.readfav.model.Time;

/**
 * Created by naver on 16. 1. 10..
 */
public class ReadAgainApplication extends Application {

    private static final long DAY = 1000 * 60 * 60 * 24;
    public static final int ACTION_FREQUENCY_DAY = 0;
    public static final int ACTION_FREQUENCY_HOUR = 1;

    @Override
    public void onCreate() {
        super.onCreate();
        startService(new Intent(this, MonitorClipService.class));
        initPreference();
        setFreqTime(this);
    }

    private void initPreference() {
        ApplicationPreferences.initialize(this);
    }

    public static void setFreqTime(Context context) {
        int[] saveTime = getFrequentlyTime(context, ActionType.SAVE);
        int[] readTime = getFrequentlyTime(context, ActionType.READ);
        Log.d("TAG", saveTime[ACTION_FREQUENCY_HOUR] + ":" +readTime[ACTION_FREQUENCY_HOUR]);
        ApplicationPreferences.getInstance().setDaySaveFreq(saveTime[ACTION_FREQUENCY_DAY]);
        ApplicationPreferences.getInstance().setSaveTime(saveTime[ACTION_FREQUENCY_HOUR]);
        ApplicationPreferences.getInstance().setDatReadFreq(readTime[ACTION_FREQUENCY_DAY]);
        ApplicationPreferences.getInstance().setReadTime(readTime[ACTION_FREQUENCY_HOUR]);
    }

    public static int[] getFrequentlyTime(Context context, ActionType actionType) {
        Realm realm = Realm.getInstance(context);
        RealmResults<Time> times = realm.where(Time.class).equalTo("actionType", actionType.ordinal()).findAllSorted("date", Sort.DESCENDING);


        int prevActionTime = (actionType == ActionType.READ) ? ApplicationPreferences.getInstance().getReadTime() : ApplicationPreferences.getInstance().getSaveTime();
        ArrayList<Integer> hours = new ArrayList<Integer>();
        ArrayList<Integer> dayFrequencies = new ArrayList<Integer>();
        Date nowDate = new Date();
        // 최신순 정렬이므로, 오늘과의 차이부터 구해간다.
        for (Time time : times) {
            Date timeDate = time.getDate();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(timeDate);
            Calendar preCalendar = Calendar.getInstance();
            preCalendar.setTime(nowDate);
            long diff = (nowDate.getTime() - timeDate.getTime()) / DAY;
            int dayFrequency = (int) Math.ceil(diff);
            dayFrequencies.add(dayFrequency);
            int saveHour = calendar.get(Calendar.HOUR_OF_DAY);
            hours.add(saveHour);
        }

        if (dayFrequencies.size()== 0) {
            return new int[]{0,0};
        }
        int sumFrequency = 0 ;
        for (Integer dayFrequency : dayFrequencies) {
            sumFrequency += dayFrequency;
        }
        int avgFrequency = sumFrequency/dayFrequencies.size();

        int[] timeDistribution = new int[24];
        for (Integer hour : hours) {
            timeDistribution[hour]++;
        }
        int maxCount = 0;
        int freqHour = 0;
        for (int hour = 0; hour < timeDistribution.length; hour++) {
            if (timeDistribution[hour] > maxCount) {
                maxCount = timeDistribution[hour];
                freqHour = hour;
            }
        }
        int diff = prevActionTime - freqHour;
        /**
         * diff < 0
         * 1)  -3 = 12 - 15
         * freqHour += diff/2
         * diff > 0
         * 2) 3 = 22 - 19;
         * freqHoyr += diff/2
         * 3 -23 = 0 - 23;
         * -23 + 0
          */
        freqHour += diff/2;
        int[] frequentHours = {avgFrequency, Math.abs(freqHour)};
        return frequentHours;
    }
}
