package com.example.materialdesign.reminder.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.materialdesign.reminder.model.Item;
import com.example.materialdesign.reminder.model.ReminderDatabase;
import com.example.materialdesign.reminder.R;

import java.util.Calendar;
import java.util.List;


public class BootReceiver extends BroadcastReceiver {



    private AlarmReceiver alarmReceiver;

    private static final int REPEAT_MODE_ON = 0;
    private static final int REPEAT_MODE_OFF = 1;
    private static final long milMinute = 60000L;
    private static final long milHour = 3600000L;
    private int id;
    private int year;
    private int month;
    private int day;
    private int hour;
    private int minute;
    private long repeatTime;
    private String repeatCount;
    private String[] time;
    private String[] date;



    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            ReminderDatabase dbHandler = new ReminderDatabase(context);
            alarmReceiver = new AlarmReceiver();
            List<Item> allevents = dbHandler.getAllEvents();

            for(int i=0;i<allevents.size();i++){

                Item item = allevents.get(i);
                id = item.getId();
                //get hour and minute
                time = item.getTime().trim().split(":");
                hour = Integer.parseInt(time[0]);
                minute = Integer.parseInt(time[1]);
                //get year,month,day
                date = item.getDate().trim().split("/");
                month = Integer.parseInt(date[0]);
                day = Integer.parseInt(date[1]);
                year = Integer.parseInt(date[2]);

                repeatCount = item.getRepeatCount();

                if(item.getRepeatType().equals(context.getResources().getString(R.string.Min))){
                    repeatTime = Integer.parseInt(repeatCount) * milMinute;
                }else if(item.getRepeatType().equals(context.getResources().getString(R.string.Hour))){
                    repeatTime = Integer.parseInt(repeatCount) * milHour;
                }

                if(item.getNotify().equals("on")) {
                    if(item.getRepeatMode().equals("on")){
                        callBroadcastReceiver(id,REPEAT_MODE_ON,context);
                    }else{
                        callBroadcastReceiver(id,REPEAT_MODE_OFF,context);
                    }
                }
            }
        }
    }

    public void callBroadcastReceiver(int row,int mode,Context context){
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR,year);
        calendar.set(Calendar.MONTH,month);
        calendar.set(Calendar.DAY_OF_MONTH,day);
        calendar.set(Calendar.HOUR_OF_DAY,hour);
        calendar.set(Calendar.MINUTE,minute);
        calendar.set(Calendar.SECOND,0);
        switch (mode){
            case REPEAT_MODE_ON:
                alarmReceiver.setRepeatAlarm(context,calendar,row,repeatTime);
                break;
            case REPEAT_MODE_OFF:
                alarmReceiver.setAlarm(context,calendar,row);
                break;
        }
    }
}
