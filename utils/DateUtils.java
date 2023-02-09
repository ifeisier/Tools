package com.ifeisier.jiuwanboduan.utils;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Date;

/**
 * java8日期工具.
 *
 * @author KimZing - kimzing@163.com
 * @since 2018-08-07 02:02
 */
public final class DateUtils {

    private DateUtil() {
    }

    // 获取当前时间的LocalDateTime对象
    // LocalDateTime.now()

    // 根据年月日构建
    // LocalDateTime.of()

    // 比较日期先后
    // LocalDateTime.now().isBefore()
    // LocalDateTime.now().isAfter()


    /**
     * 创建 DateTimeFormatter
     *
     * @param pattern 格式
     * @return DateTimeFormatter
     */
    public static DateTimeFormatter createDateTimeFormatter(String pattern) {
        return DateTimeFormatter.ofPattern(pattern);
    }

    /**
     * 格式转换
     *
     * @param dateTime 日期时间
     * @param pattern  格式
     * @return 转换完后的日期时间
     */
    public static String format(LocalDateTime dateTime, String pattern) {
        return dateTime.format(createDateTimeFormatter(pattern));
    }

    public static String format(LocalDate date, String pattern) {
        return date.format(createDateTimeFormatter(pattern));
    }


    ////////////////////////// 字符串日期转 LocalDate 或 LocalDateTime //////////////////////////

    public static LocalDate convertStrToLD(String date) {
        return LocalDate.parse(date, createDateTimeFormatter("yyyy-MM-dd"));
    }

    public static LocalDateTime convertStrToLDT(String date) {
        return LocalDateTime.parse(date, createDateTimeFormatter("yyyy-MM-dd HH:mm:ss:SSS"));
    }


    ////////////////////////// Date 转 LocalDate 或 LocalDateTime //////////////////////////


    public static LocalDate convertDateToLD(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public static LocalDateTime convertDateToLDT(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }


    ////////////////////////// LocalDate 或 LocalDateTime 转 Date //////////////////////////


    public static Date convertLDToDate(LocalDate date) {
        return Date.from(date.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }

    public static Date convertLDTToDate(LocalDateTime time) {
        return Date.from(time.atZone(ZoneId.systemDefault()).toInstant());
    }


    /**
     * 格式化Unix to LocalDateTime
     *
     * @param unixTimeInMilliSecond
     * @param pattern
     * @return
     */
    public static String formatUnixTimeToLocalDateTime(long unixTimeInMilliSecond, String pattern) {
        LocalDateTime time = LocalDateTime.ofInstant(Instant.ofEpochMilli(unixTimeInMilliSecond), ZoneOffset.ofHours(8));
        return time.format(DateTimeFormatter.ofPattern(pattern));
    }


    /**
     * 获取指定日期的毫秒.
     *
     * @param time
     * @return java.lang.Long
     */
    public static Long getMilliByTime(LocalDateTime time) {
        return time.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    /**
     * 获取指定日期的秒.
     *
     * @param time
     * @return java.lang.Long
     */
    public static Long getSecondsByTime(LocalDateTime time) {
        return time.atZone(ZoneId.systemDefault()).toInstant().getEpochSecond();
    }


    /**
     * 获取当前时间的指定格式.
     *
     * @param pattern
     * @return java.lang.String
     */
    public static String formatNow(String pattern) {
        return format(LocalDateTime.now(), pattern);
    }

    /**
     * 日期加上一个数,根据field不同加不同值,field为ChronoUnit.*.
     *
     * @param time
     * @param number
     * @param field
     * @return java.time.LocalDateTime
     */
    public static LocalDateTime plus(LocalDateTime time, long number, TemporalUnit field) {
        return time.plus(number, field);
    }

    /**
     * 日期减去一个数,根据field不同减不同值,field参数为ChronoUnit.*.
     *
     * @param time
     * @param number
     * @param field
     * @return java.time.LocalDateTime
     */
    public static LocalDateTime minu(LocalDateTime time, long number, TemporalUnit field) {
        return time.minus(number, field);
    }

    /**
     * 获取两个日期的差  field参数为ChronoUnit.*.
     *
     * @param startTime
     * @param endTime
     * @param field
     * @return long
     */
    public static long betweenTwoTime(LocalDateTime startTime, LocalDateTime endTime, ChronoUnit field) {
        Period period = Period.between(LocalDate.from(startTime), LocalDate.from(endTime));
        if (field == ChronoUnit.YEARS) {
            return period.getYears();
        }
        if (field == ChronoUnit.MONTHS) {
            return period.getYears() * 12L + period.getMonths();
        }
        return field.between(startTime, endTime);
    }

    /**
     * 获取一天的开始时间，2017,7,22 00:00.
     *
     * @param time
     * @return java.time.LocalDateTime
     */
    public static LocalDateTime getDayStart(LocalDateTime time) {
        return time.withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);
    }

    /**
     * 获取一天的结束时间，2017,7,22 23:59:59.999999999.
     *
     * @param time
     * @return java.time.LocalDateTime
     */
    public static LocalDateTime getDayEnd(LocalDateTime time) {
        return time.withHour(23)
                .withMinute(59)
                .withSecond(59)
                .withNano(999999999);
    }

}