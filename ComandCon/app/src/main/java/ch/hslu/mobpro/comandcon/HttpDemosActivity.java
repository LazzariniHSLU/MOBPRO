package ch.hslu.mobpro.comandcon;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.ExecutionException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HttpDemosActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_http_demos);
    }

    public void onClickLoadDocument(View view) throws ExecutionException, InterruptedException {
        AsyncTask task = new LoadContentHttp().execute("http://www.wherever.ch/hslu/loremIpsum.txt");
        Response response = (Response) task.get();
        if (response.isSuccessful()) {
            TextView textarea = (TextView) findViewById(R.id.ResultText);
            textarea.setVisibility(View.VISIBLE);

            try {
                textarea.setText(response.body().string());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println(response);
        }
    }

    public void onClickLoadImage(View view) throws ExecutionException, InterruptedException {
        AsyncTask task = new LoadContentHttp().execute("http://wherever.ch/hslu/homer.jpg");
        Response response = (Response) task.get();
        ImageView imageView = findViewById(R.id.imageView);
        InputStream in = response.body().byteStream();
        Bitmap image = BitmapFactory.decodeStream(in);
        response.close();
        imageView.setVisibility(View.VISIBLE);
        imageView.setImageBitmap(image);

    }

    public void onClickJsonWebService(View view) throws ExecutionException, InterruptedException {
        AsyncTask task = new LoadContentWebService().execute("http://www.nactem.ac.uk/software/acromine/");
        retrofit2.Response<List<AcronymDef>> response = (retrofit2.Response<List<AcronymDef>>) task.get();

        if (response.isSuccessful()) {
            String result = response.body().get(0).sf
                    + " "
                    + response.body().get(0).lfs.get(0).lf
                    + " "
                    + response.body().get(0).lfs.get(0).since;
            TextView textarea = (TextView) findViewById(R.id.WebServiceResult);
            textarea.setVisibility(View.VISIBLE);
            textarea.setText(result);
        } else {
            System.out.println(response);
        }
    }

    private class LoadContentHttp extends AsyncTask<String, Void, Response> {
        @Override
        protected Response doInBackground(String... urls) {
            HttpLoggingInterceptor logger = new HttpLoggingInterceptor();
            logger.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient client = new OkHttpClient.Builder().build();

            Request request = new Request.Builder().url(urls[0]).build();
            Response response = null;
            try {
                response = client.newCall(request).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return response;
        }

        @Override
        protected void onPostExecute(Response response) {
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }

    private class LoadContentWebService extends AsyncTask<String, Void, retrofit2.Response<List<AcronymDef>>> {
        @Override
        protected retrofit2.Response<List<AcronymDef>> doInBackground(String... urls) {
            Retrofit retrofit = new Retrofit.Builder().client(new OkHttpClient()).addConverterFactory(GsonConverterFactory.create()).baseUrl(urls[0]).build();

            AcronymService service = retrofit.create(AcronymService.class);

            retrofit2.Response<List<AcronymDef>> response = null;
            try {
                response = service.getDefinitionsOf("http").execute();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return response;
        }

        @Override
        protected void onPostExecute(retrofit2.Response<List<AcronymDef>> response) {
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }
}


