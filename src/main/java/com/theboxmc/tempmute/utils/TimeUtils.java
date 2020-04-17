package com.theboxmc.tempmute.utils;

import com.theboxmc.tempmute.exceptions.InvalidTimeUnitException;

import java.util.concurrent.TimeUnit;

public class TimeUtils {

    //Used for accepting multiple timeunits (days, hours, minutes, and seconds)
    public static TimeUnit getTimeUnit(char timeUnitChar) throws InvalidTimeUnitException {
        switch(timeUnitChar) {
            case 'd':
                return TimeUnit.DAYS;
            case 'h':
                return TimeUnit.HOURS;
            case 'm':
                return TimeUnit.MINUTES;
            case 's':
                return TimeUnit.SECONDS;
            default:
                throw new InvalidTimeUnitException();
        }
    }
}
