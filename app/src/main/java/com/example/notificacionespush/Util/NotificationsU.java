package com.example.notificacionespush.Util;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.text.Html;
import android.text.TextUtils;
import android.util.Patterns;

import androidx.core.app.NotificationCompat;

import com.example.notificacionespush.App.AppConfig;
import com.example.notificacionespush.R;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class NotificationsU {
    private static String TAG  = NotificationsU.class.getSimpleName();
    private Context mContext;
    private static final int REQUEST_NOTIFICATION = 0;
    private Bitmap bitmap;

    public NotificationsU(Context context){
        this.mContext = mContext;
    }

    public void showNotificacionMessage(final String titulo, final String mensaje, final String timeStamp,
                                        Intent intent){
        showNotificacionMessage(titulo, mensaje, timeStamp, intent,null);
    }

    public void showNotificacionMessage(final String titulo, final String mensaje, final String timeStamp,
                                        Intent intent, String imageUrl){
        if(TextUtils.isEmpty(mensaje)) return;

        final int icon = R.mipmap.ic_launcher_round;
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        final PendingIntent resultPendingIntent =
                PendingIntent.getActivity(mContext,
                        REQUEST_NOTIFICATION,
                        intent,
                        PendingIntent.FLAG_CANCEL_CURRENT);

        final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext,
                AppConfig.CHANNEL_ID);

        final Uri alarms = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE
                + "://"+mContext.getPackageName()+"raw/tono_mensaje");

        if(TextUtils.isEmpty(imageUrl)){
            if (imageUrl != null && imageUrl.length() > 4
            && Patterns.WEB_URL.matcher(imageUrl).matches()){
                try {
                    URL url = new URL(imageUrl);
                    bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (bitmap != null){
                    showBigNotification(bitmap,mBuilder,icon,titulo,mensaje,timeStamp,resultPendingIntent,alarms);
                } else {
                    showSmallNotification(mBuilder,icon,titulo,mensaje,timeStamp,resultPendingIntent,alarms);
                }
            } else {
                showSmallNotification(mBuilder,icon,titulo,mensaje,timeStamp,resultPendingIntent,alarms);
            }
        }
    }

    private void showSmallNotification(NotificationCompat.Builder mBuilder, int icon, String titulo, String mensaje,
                                       String timestamp, PendingIntent resultPendingIntent, Uri alarma){
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        inboxStyle.addLine(mensaje);
        Notification notification = mBuilder.setSmallIcon(icon).setTicker(titulo).setWhen(0)
                .setAutoCancel(true)
                .setContentTitle(titulo)
                .setContentIntent(resultPendingIntent)
                .setSound(alarma)
                .setStyle(inboxStyle)
                .setWhen(getTimeMillisec(timestamp))
                .setSmallIcon(icon)
                .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), icon))
                .setContentText(mensaje)
                .build();

        NotificationManager notificationManager = (NotificationManager)mContext
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(AppConfig.NOTIFICATION_ID, notification);

    }

    public static long getTimeMillisec(String timeStamp){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date date = (format.parse(timeStamp));
            return date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void showBigNotification(Bitmap bitmap, NotificationCompat.Builder mBuilder, int icon, String titulo,
                                     String mensaje, String timestamp, PendingIntent resultPendingIntent, Uri alarma){
        NotificationCompat.BigPictureStyle bigPictureStyle = new NotificationCompat.BigPictureStyle();
        bigPictureStyle.setBigContentTitle(titulo);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            bigPictureStyle.setSummaryText(Html.fromHtml(mensaje, Html.FROM_HTML_MODE_LEGACY).toString());
        } else {
            bigPictureStyle.setSummaryText(Html.fromHtml(mensaje).toString());
        }
        bigPictureStyle.bigPicture(bitmap);
        Notification notification = mBuilder.setSmallIcon(icon).setTicker(titulo).setWhen(0)
                .setAutoCancel(true)
                .setContentTitle(titulo)
                .setContentIntent(resultPendingIntent)
                .setSound(alarma)
                .setStyle(bigPictureStyle)
                .setWhen(getTimeMillisec(timestamp))
                .setSmallIcon(icon)
                .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), icon))
                .setContentText(mensaje)
                .build();

        NotificationManager notificationManager = (NotificationManager)mContext
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(AppConfig.NOTIFICATION_ID_BIG_IMAGE, notification);
    }

    public void  playNotificationAlarm(){
        try{
            Uri alarma = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://"
                    + mContext.getPackageName() + "/raw/tono_mensaje");
            Ringtone ringtone = RingtoneManager.getRingtone(mContext, alarma);
            ringtone.play();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static  boolean isAppIsInBackground(Context context){
        boolean isInBackground = true;
        ActivityManager activityManager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);

        List<ActivityManager.RunningAppProcessInfo> runningAppProcessInfos
                = activityManager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo processInfo : runningAppProcessInfos){
            if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND){
                for (String activeProcess : processInfo.pkgList){
                    if (activeProcess.equals(context.getPackageName())){
                        isInBackground = false;
                    }
                }
            }
        }
        return isInBackground;
    }
}
