package ch.hslu.mobpro.servicesandreceiver;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import java.util.ArrayList;
import java.util.List;

public class MusicPlayer extends Service {

    private final int NOTIFICATION_ID = 1;
    private Thread playThread = null;
    private List<String> songs = new ArrayList<>();
    private int currentSong = 0;
    private List<String> history = new ArrayList<>();
    private MusicPlayerRunnable runnable = null;
    private NotificationCompat.Builder builder = null;

    public MusicPlayer() {
        songs.add("Britney Spears - one more time");
        songs.add("Bethoven - Für Elise");
        songs.add("Jetta - I'd Love to change the world");
        songs.add("San Holo - We Rise");
        songs.add("Lukas Graham - 7 Years");
        songs.add("Gioni - Trigger");
        songs.add("Dua Lipa - New Rules");
        songs.add("Galantis - Runaway");
        songs.add("Mickey Valen - Meet Me");
        songs.add("Miley Cyrus - Wrecking Ball");
        songs.add("Camilla Cabello - Havana");
        songs.add("Calvin Harris - Outside");
        songs.add("Kygo - Firestone");
        songs.add("Martin Garrix - Animals");
        songs.add("Marshmello - Happier");
        songs.add("Imagine Dragons - Believer");

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startPlayer();
        return Service.START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        stopPlayThread();
        stopForeground(true);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new MusicplayerApiBinder();
    }

    private void startPlayer() {
        if (playThread != null && playThread.isAlive())
            return;
        startPlayThread();
        startForeground(NOTIFICATION_ID, createNotification("Playing " + songs.get(currentSong)));
    }

    private void startPlayThread() {
        runnable = new MusicPlayerRunnable();
        playThread = new Thread(runnable);
        playThread.start();
    }

    private void stopPlayThread() {
        System.out.println("MusicService onStop()");
        runnable.terminate();
        try {
            playThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private Notification createNotification(String msg) {
        builder = new NotificationCompat.Builder(this, getString(R.string.channel_name))
                .setContentTitle("HSLU Music Player")
                .setTicker("HSLU Music Player")
                .setContentText(msg)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setOngoing(true)
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setWhen(System.currentTimeMillis());
        return builder.build();

    }

    public List<String> queryHistory() {
        return history;
    }

    public String playnext() {
        history.add(songs.get(currentSong));
        currentSong += 1;
        builder.setContentText("Playing " + songs.get(currentSong));
        return "Play next song " + songs.get(currentSong);
    }

    public class MusicplayerApiBinder extends Binder implements DemoServiceApi {


        @Override
        public String playNextItem() {
            return MusicPlayer.this.playnext();
        }

        @Override
        public List<String> getHistory() {
            return MusicPlayer.this.queryHistory();
        }

        public MusicPlayer getService() {
            return MusicPlayer.this;
        }
    }

    public class MusicPlayerRunnable implements Runnable {

        private volatile boolean running = true;

        @Override
        public void run() {
            while (running) {
                System.out.println("Playing " + MusicPlayer.this.songs.get(currentSong));
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        public void terminate() {
            running = false;
        }
    }
}
