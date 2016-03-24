package com.judopay.model;

import java.util.Calendar;
import java.util.Date;

import static java.util.Calendar.DATE;
import static java.util.Calendar.MONTH;
import static java.util.Calendar.YEAR;

public class CardDate {

    private final int month;
    private final int year;

    public CardDate(String cardDate) {
        String splitCardDate = cardDate.replaceAll("/", "");
        this.month = getMonth(splitCardDate);
        this.year = getYear(splitCardDate);
    }

    private int getYear(String year) {
        if (!isValidDate(year)) {
            return 0;
        }

        return 2000 + Integer.parseInt(year.substring(2, 4));
    }

    private int getMonth(String month) {
        if (!isValidDate(month)) {
            return 0;
        }

        return Integer.parseInt(month.substring(0, 2));
    }

    public boolean isBeforeToday() {
        if (year == 0 || month == 0) {
            return false;
        }

        Calendar cardDate = Calendar.getInstance();
        cardDate.set(year, month - 1, 1);

        Calendar now = Calendar.getInstance();
        now.setTime(new Date());

        return cardDate.before(now);
    }

    public boolean isAfterToday() {
        if (year == 0 || month == 0) {
            return false;
        }

        Calendar cardDate = Calendar.getInstance();
        cardDate.set(YEAR, year);
        cardDate.set(MONTH, month - 1);
        cardDate.set(DATE, cardDate.getActualMaximum(Calendar.DAY_OF_MONTH));

        Calendar now = Calendar.getInstance();
        now.setTime(new Date());

        return cardDate.after(now);
    }

    public boolean isInsideAllowedDateRange() {
        Calendar now = Calendar.getInstance();
        now.setTime(new Date());

        Calendar minDate = (Calendar) now.clone();
        minDate.set(YEAR, minDate.get(YEAR) - 10);

        Calendar maxDate = (Calendar) now.clone();
        minDate.set(YEAR, maxDate.get(YEAR) + 10);

        return now.after(minDate) && now.before(maxDate);
    }

    private boolean isValidDate(String date) {
        return date.matches("(?:0[1-9]|1[0-2])[0-9]{2}");
    }

}