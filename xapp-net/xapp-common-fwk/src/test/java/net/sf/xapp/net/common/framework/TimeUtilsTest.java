package net.sf.xapp.net.common.framework;

import junit.framework.TestCase;
import net.sf.xapp.net.common.util.TimeUtils;

import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class TimeUtilsTest extends TestCase
{
    public void testHoursTillNextHour() throws Exception {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(1800000);

        assertEquals(0.5f, TimeUtils.hoursTillNextHour(c));
        c.setTimeInMillis(0);
        assertEquals(1.0f, TimeUtils.hoursTillNextHour(c));
        c.setTimeInMillis(4500000);
        assertEquals(0.75f, TimeUtils.hoursTillNextHour(c));
    }

    public void testDaysTillNextDay() throws Exception {
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        c.setTimeInMillis(TimeUnit.DAYS.toMillis(1)/2);
        assertEquals(0.5f, TimeUtils.daysTillNextDay(c));
        c.setTimeInMillis(0);
        assertEquals(1.0f, TimeUtils.daysTillNextDay(c));
        c.setTimeInMillis(TimeUnit.DAYS.toMillis(1) + TimeUnit.DAYS.toMillis(1) / 4);
        assertEquals(0.75f, TimeUtils.daysTillNextDay(c));
    }
}
