package ch.hslu.mobpro.servicesandreceiver;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.math.MathUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private MusicPlayer player;
    private boolean isServiceBoundToThisActivity = false;
    private MusicPlayerConnection connection = new MusicPlayerConnection();
    private int messageCounter = 0;
    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            messageCounter += 1;
            TextView lbl = findViewById(R.id.lblReceiverMessage);
            lbl.setText("Broadcast #" + messageCounter + " erhalten!");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createNotificationChannel();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public void start_player(View view) {
        startService(new Intent(this, MusicPlayer.class));
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(getString(R.string.channel_name), name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void stop_player(View view) {
        stopService(new Intent(this, MusicPlayer.class));
    }

    public void playNextClicked(View view) {
        if(isServiceBoundToThisActivity){
            DemoServiceApi api = connection.getMusicPlayerApi();
            Context context = getApplicationContext();
            CharSequence text = api.playNextItem();
            Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
            toast.show();
        }
    }
    public void showHistoryClicked(View view) {
        if(isServiceBoundToThisActivity){
            DemoServiceApi api = connection.getMusicPlayerApi();
            List<String> history = api.getHistory();
            String historyDescription = "";
            for (String song : history){
                historyDescription += song +"\n";
            }
            AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(this);
            dlgAlert.setMessage(historyDescription);
            dlgAlert.setTitle("History");
            dlgAlert.setPositiveButton("OK", null);
            dlgAlert.setCancelable(true);
            dlgAlert.create().show();
        }
    }

    public void broadcastCheckboxClicked(View view) {
        boolean checked = ((CheckBox) view).isChecked();
        if (checked) {
            IntentFilter filter = new IntentFilter("ACTION_MY_BROADCAST");
            LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);
            ((CheckBox) view).setText("Broadcast Receiver registriert");
            ((TextView) findViewById(R.id.lblReceiverMessage)).setText("");
        }
        else {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
            messageCounter = 0;
            ((TextView) findViewById(R.id.lblReceiverMessage)).setText("Broadcast Empfang deaktiviert!");
            ((CheckBox) view).setText("Kein Broadcast Receiver registriert");
        }
    }


    //#region ServiceBinding

    private void bindServiceToThisActivity() {
//        if (!isServiceBoundToThisActivity) {
            Intent intent = new Intent(this, MusicPlayer.class);
            connection = new MusicPlayerConnection();
            bindService(intent, connection, BIND_AUTO_CREATE);
//        }
    }

    private void unbindServiceFromThisActivity() {
//        if (isServiceBoundToThisActivity) {
            unbindService(connection);
            connection = null;
//        }
    }

    public void sendBroadcast(View view) {
        Intent BCIntent = new Intent("ACTION_MY_BROADCAST");
        LocalBroadcastManager.getInstance(this).sendBroadcast(BCIntent);
    }

    public void connectToServiceClicked(View view) {
        boolean checked = ((CheckBox) view).isChecked();
        if (checked) {
            //Bind to Service
            bindServiceToThisActivity();
        }
        else {
            unbindServiceFromThisActivity();
        }
    }
    //#endregion

    private class MusicPlayerConnection implements ServiceConnection {

        private MusicPlayer.MusicplayerApiBinder binder;
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
             binder = (MusicPlayer.MusicplayerApiBinder) service;
            player = binder.getService();
            isServiceBoundToThisActivity = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            isServiceBoundToThisActivity = false;
        }

        public DemoServiceApi getMusicPlayerApi() {
            return binder;
        }
    }
}
