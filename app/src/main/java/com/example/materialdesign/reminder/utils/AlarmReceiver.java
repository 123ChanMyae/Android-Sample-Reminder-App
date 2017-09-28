package com.example.materialdesign.reminder.utils;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.os.SystemClock;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.example.materialdesign.reminder.activity.MainActivity;
import com.example.materialdesign.reminder.model.Event;
import com.example.materialdesign.reminder.model.ReminderDatabase;
import com.example.materialdesign.reminder.R;

import java.util.Calendar;


public class AlarmReceiver extends WakefulBroadcastReceiver {

    private AlarmManager alarmManager;
    private ReminderDatabase dbHandler;
    @Override
    public void onReceive(Context context, Intent intent) {

        int id = Integer.parseInt(intent.getStringExtra("ID"));
        dbHandler = new ReminderDatabase(context);
        Event event = dbHandler.getEventId(id);

        Intent i = new Intent(context,MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP| Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,id,i,PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(context)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),R.mipmap.ic_launcher))
                .setSmallIcon(R.drawable.ic_alarm_small)
                .setContentTitle("Reminder")
                .setContentText(event.getTitle())
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setOnlyAlertOnce(true)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(id,builder.build());

        Log.i("Reciver","called");

    }



    public void setAlarm(Context context, Calendar calendar,int id){
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context,AlarmReceiver.class);
        intent.putExtra("ID",Integer.toString(id));
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,id,intent,PendingIntent.FLAG_CANCEL_CURRENT);
        Calendar now = Calendar.getInstance();
        long currentTime = now.getTimeInMillis();
        long differentTime = calendar.getTimeInMillis() - currentTime;
        alarmManager.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + differentTime,pendingIntent);

        // Restart alarm if device is rebooted
        ComponentName receiver = new ComponentName(context, BootReceiver.class);
        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);

    }



    public void cancelAlarm(Context context,int id){
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,id,new Intent(context,AlarmReceiver.class),0);
        alarmManager.cancel(pendingIntent);

        // Disable alarm
        ComponentName receiver = new ComponentName(context, BootReceiver.class);
        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }



    public void setRepeatAlarm(Context context, Calendar calendar,int id,long repeatTime){
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Put Reminder ID in Intent Extra
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("ID", Integer.toString(id));
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        // Calculate notification timein
        Calendar c = Calendar.getInstance();
        long currentTime = c.getTimeInMillis();
        long diffTime = calendar.getTimeInMillis() - currentTime;

        // Start alarm using initial notification time and repeat interval time
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + diffTime,
                repeatTime , pendingIntent);

        // Restart alarm if device is rebooted
        ComponentName receiver = new ComponentName(context, BootReceiver.class);
        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }
}
