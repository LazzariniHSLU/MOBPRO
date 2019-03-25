package ch.hslu.mobpro.comandcon;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class MainActivity extends AppCompatActivity {

    private static final int MY_REQUEST_CODE = 23; // Arbitrary number

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void startHttpDemosActivity(View v) {
        Intent intent = new Intent(this, HttpDemosActivity.class);
        startActivityForResult(intent, MY_REQUEST_CODE);
    }

    public void startDemoThread(View v) {
        new Thread("DemoThread") {
            @Override
            public void run() {
                try {
                    Thread.sleep(7000);
                    MainActivity.this.runOnUiThread(() -> {
                        Button btn = (Button) findViewById(R.id.DemoThread);
                        btn.setText("DemoThread starten");
                        Toast.makeText(MainActivity.this, "Demo Thread beendet!", Toast.LENGTH_LONG).show();
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();

        Button btn = (Button) findViewById(R.id.DemoThread);
        btn.setText("Demo Thread läuft");
        Toast.makeText(MainActivity.this, "Demo Thread gestartet!", Toast.LENGTH_LONG).show();
    }

    public void onClickLoadDocument(View view) throws ExecutionException, InterruptedException, MalformedURLException {
        new MultiAsyncTask().execute(new URL("http://www.wherever.ch/hslu/title0.txt"),
                new URL("http://www.wherever.ch/hslu/title1.txt"),
                new URL("http://www.wherever.ch/hslu/title2.txt"),
                new URL("http://www.wherever.ch/hslu/title3.txt"),
                new URL("http://www.wherever.ch/hslu/title4.txt"));
    }

    private class MultiAsyncTask extends AsyncTask<URL, String, Void> {

        private ArrayList<String> films = new ArrayList<>();

        @Override
        protected Void doInBackground(URL... urls) {
            HttpLoggingInterceptor logger = new HttpLoggingInterceptor();
            logger.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient client = new OkHttpClient.Builder().addInterceptor(logger).build();


            for (URL url : urls) {
                Request request = new Request.Builder().url(url).build();
                Response response = null;
                try {
                    response = client.newCall(request).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    String filmTitle = response.body().string();
                    publishProgress(filmTitle);
                    films.add(filmTitle);
                    Thread.sleep(2000);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MainActivity.this);
            dialogBuilder.setTitle("Film Titels");
            dialogBuilder.setItems(films.toArray(new String[0]), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            dialogBuilder.show();
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(String... values) {
            Toast.makeText(MainActivity.this, "Neuer Titel: " + values[0], Toast.LENGTH_LONG).show();
        }
    }

}
