package com.era.onlinesignature.util;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class Utils {

    public static long getDateDifference(Date date1, Date date2, TimeUnit timeUnit) {
        long differenceInMiles = date2.getTime() - date1.getTime();
        return timeUnit.convert(differenceInMiles, TimeUnit.MILLISECONDS);
    }
}
