package com.gribanskij.miserplus.utils;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;

import com.gribanskij.miserplus.R;
import com.gribanskij.miserplus.dashboard_screen.DashboardActivity;

import java.util.Calendar;

/**
 * Created by SESA175711 on 21.11.2017.
 */

public class NotificationUtils {


    private final static int MISER_REMINDER_NOTIFICATION_ID = 1138;
    private final static int MISER_REMINDEER_PENDING_INTENT_ID = 3417;
    private final static String MISER_REMINDER_NOTIFICATION_CHANNEL_ID = "miser_notification";


    public static void clearAllNotifications(Context context) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancelAll();
    }


    public static void remindUserAddExpenses(Context context) {


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel mChannel = new NotificationChannel(MISER_REMINDER_NOTIFICATION_CHANNEL_ID,
                    context.getString(R.string.main_notification_channel), NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(mChannel);
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(
                context, MISER_REMINDER_NOTIFICATION_CHANNEL_ID)
                .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setSmallIcon(R.mipmap.ic_launcher_miser_plus)
                .setLargeIcon(largeIcon(context))
                .setContentTitle(context.getString(R.string.addExpenses_notification_title))
                .setContentText(context.getString(R.string.addExpenses_notification_main_text))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(contentIntent(context))
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(MISER_REMINDER_NOTIFICATION_ID, notificationBuilder.build());
    }

    private static PendingIntent contentIntent(Context context) {
        Intent startActivityIntent = new Intent(context, DashboardActivity.class);
        return PendingIntent.getActivity(context, MISER_REMINDEER_PENDING_INTENT_ID,
                startActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static Bitmap largeIcon(Context context) {
        Resources resource = context.getResources();
        return BitmapFactory.decodeResource(resource, R.mipmap.ic_launcher_miser_plus);
    }

    public static void setAlarm(Context context) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 21);
        calendar.set(Calendar.MINUTE, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, getPendingIntent(context));

    }

    private static PendingIntent getPendingIntent(Context context) {
        Intent intent = new Intent();
        intent.setAction(AddExpensesReceiver.ACTION_ADD_EXPENSES);
        intent.setClass(context, AddExpensesReceiver.class);

        return PendingIntent.getBroadcast(context,
                178, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }


    public static void disableAlarm(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(getPendingIntent(context));
    }

}
