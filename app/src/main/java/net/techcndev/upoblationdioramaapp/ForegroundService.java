package net.techcndev.upoblationdioramaapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class ForegroundService extends Service {

    private static final String LOG_TAG = ForegroundService.class.getSimpleName();

    public static final String CHANNEL_1_ID = "channel1";
    public static final String CHANNEL_2_ID = "channel2";
    public static final String CHANNEL_3_ID = "channel3";

    private NotificationManagerCompat notificationManager;

    GlobalObject globalObject;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        globalObject = new GlobalObject(getApplicationContext());
        notificationManager = NotificationManagerCompat.from(this);

        sharedPreferences = getSharedPreferences("PREFS_NAME", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        createNotificationChannels();
        startForeground(3, serviceNotification());

        globalObject.batteryPercentRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    try {
                        String userDevice = sharedPreferences.getString("user_device", "");
                        if (snapshot.getValue(Integer.class) < 70) {
                            setWarningNotification("Device Batttery is LOW", "Please charge your device \"" + userDevice + "\"");
                        }
                    } catch (Exception e) {
                        Log.d(LOG_TAG, "Error: " + e);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(LOG_TAG, "Error: " + error.getMessage());
            }
        });

        globalObject.powerSourceRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    try {
                        setReminderNotification("Power Source", "Power source changed to " + snapshot.getValue(String.class).toUpperCase());
                    } catch (Exception e) {
                        Log.d(LOG_TAG, "Error: " + e);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(LOG_TAG, "Error: " + error.getMessage());
            }
        });

        globalObject.waterLevelRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    try {
                        String userDevice = sharedPreferences.getString("user_device", "");
                        if (snapshot.getValue(Integer.class) < 70) {
                            setWarningNotification("Fountain Water Level", "\"" + userDevice + "\"" + "fountain water level is " + snapshot.getValue(String.class).toUpperCase());
                        }
                    } catch (Exception e) {
                        Log.d(LOG_TAG, "Error: " + e);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(LOG_TAG, "Error: " + error.getMessage());
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void setReminderNotification(String title, String message) {
        Bitmap img = BitmapFactory.decodeResource(getResources(), R.drawable.information);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_2_ID)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .build();

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

    }

    public void setWarningNotification(String title, String message) {
        Bitmap img = BitmapFactory.decodeResource(getResources(), R.drawable.warning);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_1_ID)
                .setLargeIcon(img)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .build();

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        notificationManager.notify(2, notification);
    }

    private void createNotificationChannels() {
        NotificationChannel channel1 = new NotificationChannel(
                CHANNEL_1_ID,
                "System Alerts",
                NotificationManager.IMPORTANCE_HIGH
        );
        channel1.setDescription("For app system alerts");
        channel1.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        NotificationChannel channel2 = new NotificationChannel(
                CHANNEL_2_ID,
                "Device Alerts",
                NotificationManager.IMPORTANCE_HIGH
        );
        channel2.setDescription("For linked device alerts");
        channel2.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        NotificationChannel channel3 = new NotificationChannel(
                CHANNEL_3_ID,
                "Device Alerts",
                NotificationManager.IMPORTANCE_HIGH
        );
        channel3.setDescription("For linked device alerts");
        channel3.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel1);
        manager.createNotificationChannel(channel2);
        manager.createNotificationChannel(channel3);
    }

    private Notification serviceNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_3_ID)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle("UP Oblation Diorama App")
                .setContentText("Background Service of UP Oblation Diorama App App is running...")
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .build();
    }

}