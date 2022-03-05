package com.deep.nelumbo.dynform.util;

import java.util.Calendar;

/**
 * Utility methods handling dates.
 *
 * @author X200531
 */
public class DateHelper {

    /**
     * Calculates the number of days between 2 dates. Is a bit inefficient because
     * it loops through each date so the method takes longer the more days are between.
     * The reason is to correctly calculated summer/wintertime switches.
     *
     * @param startDate
     * @param endDate
     * @return
     */
    public static int daysBetween(java.util.Date startDate, java.util.Date endDate) {
        Calendar startCal = Calendar.getInstance();
        startCal.setTime(startDate);

        startCal.set(Calendar.HOUR_OF_DAY, 1);
        startCal.set(Calendar.MINUTE, 0);
        startCal.set(Calendar.SECOND, 0);
        startCal.set(Calendar.MILLISECOND, 0);

        Calendar endCal = Calendar.getInstance();
        endCal.setTime(endDate);

        endCal.set(Calendar.HOUR_OF_DAY, 1);
        endCal.set(Calendar.MINUTE, 0);
        endCal.set(Calendar.SECOND, 0);
        endCal.set(Calendar.MILLISECOND, 0);
        // loop because of change from summer to wintertime (daylight savings time)
        int days = 0;
        while (startCal.before(endCal)) {
            days++;
            startCal.add(Calendar.DAY_OF_YEAR, days);
        }
        return days;
    }
}
