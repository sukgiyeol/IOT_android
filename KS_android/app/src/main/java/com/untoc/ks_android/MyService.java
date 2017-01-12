package com.untoc.ks_android;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

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
        public void handleMessage(Message msg) {
            Intent intent = new Intent(MyService.this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(MyService.this, 0, intent,PendingIntent.FLAG_UPDATE_CURRENT);
            switch (msg.what)
            {
                case 1:
                    // 메시지를 처리할 코드들을 여기에 작성하세요.
                    break;
                case 2:
                    // 메시지를 처리할 코드들을 여기에 작성하세요.
                    break;
                case 3:
                    // 메시지를 처리할 코드들을 여기에 작성하세요.
                    break;
                default:    // 정의되지 않은 메시지들의 경우 여기로 분기
                    break;
            }

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
//            Toast.makeText(MyService.this, msg + "", Toast.LENGTH_SHORT).show();
        }
    };
}
