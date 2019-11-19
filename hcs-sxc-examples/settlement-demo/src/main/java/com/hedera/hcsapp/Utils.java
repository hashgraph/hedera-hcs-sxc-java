package com.hedera.hcsapp;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class Utils {
    public static String TimestampToDate(long seconds, int nanos) {
        
        LocalDateTime dateTime = LocalDateTime.ofEpochSecond(seconds, nanos, ZoneOffset.UTC);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMM", Locale.getDefault());
        String formattedDate = dateTime.format(formatter);
        return formattedDate;
        
    }
    
    public static String TimestampToTime(long seconds, int nanos) {
        
        LocalDateTime dateTime = LocalDateTime.ofEpochSecond(seconds, nanos, ZoneOffset.UTC);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("H:mm", Locale.getDefault());
        String formattedDate = dateTime.format(formatter);
        return formattedDate;
    }
}
