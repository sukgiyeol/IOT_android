package com.untoc.ks_android;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

public class MyService extends Service {
    NotificationManager notificationManager;
    ServiceThread serviceThread;
    Notification notification;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        myServiceHandler handler = new myServiceHandler();
        serviceThread = new ServiceThread(handler);
        serviceThread.start();

        return START_STICKY;
    }

    //서비스가 종료될 때 할 작업
    public void onDestroy() {
        serviceThread.stopForever();
        serviceThread = null;
    }

    class myServiceHandler extends Handler {
        @Override
        public void handleMessage(android.os.Message msg) {
            Intent intent = new Intent(MyService.this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(MyService.this, 0, intent,PendingIntent.FLAG_UPDATE_CURRENT);

            notification = new Notification.Builder(getApplicationContext())
                    .setContentTitle("Content Title")
                    .setContentText("Content Text")
                    .setSmallIcon(R.drawable.notification_template_icon_bg)
                    .setTicker("알림!!!")
                    .setContentIntent(pendingIntent)
                    .setDefaults(Notification.DEFAULT_VIBRATE)
                    .build();
            notificationManager.notify( 777 , notification);

            //토스트 띄우기
            Toast.makeText(MyService.this, "뜸?", Toast.LENGTH_SHORT).show();
        }
    };
}
