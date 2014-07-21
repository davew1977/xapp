package net.sf.xapp.net.common.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class TimeUtils
{
    static long HOUR_IN_MILLIS = TimeUnit.HOURS.toMillis(1);
    static long DAY_IN_MILLIS = TimeUnit.DAYS.toMillis(1);

    public static float hoursTillNextHour(Calendar time)
    {
        long remainder = time.getTimeInMillis() % HOUR_IN_MILLIS;
        return 1 - remainder / (float)HOUR_IN_MILLIS;
    }

    public static float daysTillNextDay(Calendar time)
    {
        long elapsedInDay = time.getTimeInMillis() - timeAtStartOfDay(time);
        long remainder = DAY_IN_MILLIS - elapsedInDay;
        return remainder / (float)DAY_IN_MILLIS;
    }

    public static long timeAtStartOfDay(Calendar time)
    {
        Calendar copy = (Calendar) time.clone();
        copy.set(Calendar.HOUR_OF_DAY, 0);
        copy.set(Calendar.MILLISECOND, 0);
        copy.set(Calendar.SECOND, 0);
        copy.set(Calendar.MINUTE, 0);
        return copy.getTimeInMillis();
    }

    public static String toString(long timeInMillis)
    {
        return SimpleDateFormat.getDateTimeInstance().format(new Date(timeInMillis));
    }
}
