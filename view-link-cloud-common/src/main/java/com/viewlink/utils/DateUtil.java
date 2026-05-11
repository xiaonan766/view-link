package com.viewlink.utils;


import com.viewlink.entity.enums.DateTimePatternEnum;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class DateUtil {

    private static final Object lockObj = new Object();
    private static Map<String, ThreadLocal<SimpleDateFormat>> sdfMap = new HashMap<String, ThreadLocal<SimpleDateFormat>>();

    private static SimpleDateFormat getSdf(final String pattern) {
        ThreadLocal<SimpleDateFormat> tl = sdfMap.get(pattern);
        if (tl == null) {
            synchronized (lockObj) {
                tl = sdfMap.get(pattern);
                if (tl == null) {
                    tl = new ThreadLocal<SimpleDateFormat>() {
                        @Override
                        protected SimpleDateFormat initialValue() {
                            return new SimpleDateFormat(pattern);
                        }
                    };
                    sdfMap.put(pattern, tl);
                }
            }
        }

        return tl.get();
    }

    public static String format(Date date, String pattern) {
        return getSdf(pattern).format(date);
    }

    public static Date parse(String dateStr, String pattern) {
        try {
            return getSdf(pattern).parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return new Date();
    }

    public static String getBeforeDay(Integer day) {
        ////返回一个Calendar对象，该对象的初始时间是当前系统时间
        Calendar calendar = Calendar.getInstance();
        //对日期进行加减操作
        calendar.add(Calendar.DAY_OF_YEAR, -day);
        //格式化日期,getTime返回一个Date对象
        return format(calendar.getTime(), DateTimePatternEnum.YYYY_MM_DD.getPattern());
    }

    /**
     * 获取之前多少天
     */
    public static List<String> getBeforeDates(Integer beforeDays) {
        //获取当前日期
        LocalDate now = LocalDate.now();
        //将格式化后的日期，保存到List中，根据索引可以获取到日期
        List<String> dateList = new ArrayList();
        //格式化当前日期
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        //根据传过来的之前多少天的参数
        for (int i = beforeDays; i > 0; i--) {
            dateList.add(now.minusDays(i).format(dateTimeFormatter));
        }
        return dateList;
    }
}
