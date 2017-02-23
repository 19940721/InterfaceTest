package com.gionee.autotest.interfacetest.Util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by siqiliu on 2017/2/22.
 */
public class Util {

    public static String getTime() {
        SimpleDateFormat formatter = new SimpleDateFormat(
                "yyyy-MM-dd-HH:mm:ss:SSS");
        Date curDate = new Date(System.currentTimeMillis());
        String time = formatter.format(curDate);
        return time;
    }



}
