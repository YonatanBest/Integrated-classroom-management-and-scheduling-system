package com.classroom.util;

import java.time.LocalTime;
import java.time.Duration;

public class TimeSlotUtil {
    public static final int PERIOD_DURATION_MINUTES = 50;
    
    public static boolean isValidPeriodDuration(String startTime, String endTime) {
        LocalTime start = LocalTime.parse(startTime);
        LocalTime end = LocalTime.parse(endTime);
        
        long minutes = Duration.between(start, end).toMinutes();
        return minutes == PERIOD_DURATION_MINUTES;
    }
    
    public static boolean isValidRegularProgramTime(String day, String startTime, String endTime) {
        if (day.equals("Saturday") || day.equals("Sunday")) {
            return false;
        }
        
        LocalTime start = LocalTime.parse(startTime);
        LocalTime end = LocalTime.parse(endTime);
        LocalTime programStart = LocalTime.parse("08:00");
        LocalTime programEnd = LocalTime.parse("17:00");
        LocalTime lunchStart = LocalTime.parse("12:00");
        LocalTime lunchEnd = LocalTime.parse("13:00");
        
        return !start.isBefore(programStart) && 
               !end.isAfter(programEnd) && 
               !(start.isBefore(lunchEnd) && end.isAfter(lunchStart));
    }
    
    public static boolean isValidEveningProgramTime(String day, String startTime, String endTime) {
        LocalTime start = LocalTime.parse(startTime);
        LocalTime end = LocalTime.parse(endTime);
        
        switch (day) {
            case "Monday":
            case "Tuesday":
            case "Wednesday":
            case "Thursday":
            case "Friday":
                return !start.isBefore(LocalTime.parse("18:00")) && 
                       !end.isAfter(LocalTime.parse("20:00"));
            case "Saturday":
                return !start.isBefore(LocalTime.parse("13:00")) && 
                       !end.isAfter(LocalTime.parse("17:00"));
            case "Sunday":
                return !start.isBefore(LocalTime.parse("08:00")) && 
                       !end.isAfter(LocalTime.parse("12:00"));
            default:
                return false;
        }
    }
}