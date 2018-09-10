package org.simpleflatmapper.jdbc.converter;

import org.simpleflatmapper.converter.Context;
import org.simpleflatmapper.converter.Converter;

import java.sql.Timestamp;
import java.util.Calendar;

public class CalendarToTimestampConverter implements Converter<Calendar, Timestamp> {
    @Override
    public Timestamp convert(Calendar in, Context context) throws Exception {
        if (in != null) {
            return new Timestamp(in.getTimeInMillis());
        }
        return null;
    }
}
