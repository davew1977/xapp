/*
 *
 * Date: 2010-nov-17
 * Author: davidw
 *
 */
package net.sf.xapp.marshalling.stringserializers;

import net.sf.xapp.marshalling.api.StringSerializer;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * serializes dates as a long where 0 is midnight jan 1 1970 or something
 */
public class DateStringSerializer implements StringSerializer<Date> {

    @Override
    public Date read(String str) {
        if (str == null) {
            return null;
        }
        if (str.matches("\\d*")) {
            return new Date(Long.parseLong(str));
        } else {
            try {
                return SimpleDateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.UK).parse(str);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public String write(Date obj) {
        if (obj == null) {
            return null;
        }
        return SimpleDateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.UK).format(obj);
    }

    @Override
    public String validate(String text) {
        return null;
    }
}
