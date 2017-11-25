package com.example.sleepeanuty.bouncingnumber;

import android.app.Activity;

import java.lang.reflect.InvocationTargetException;

/**
 * Created by sleepeanuty on 2017/11/11.
 */

public class Matcher {
    public static void bind(Activity activity){
        Class<?> clz = activity.getClass();
        String name = clz.getName()+"_bindView1";

        try {
            Class<?> clz1 = clz.getClassLoader().loadClass(name);
            try {
                clz1.getConstructor(clz).newInstance(activity);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
