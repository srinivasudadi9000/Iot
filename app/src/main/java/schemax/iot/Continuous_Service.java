package schemax.iot;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.io.IOException;
import java.net.URI;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import schemax.iot.model.Weather;
import schemax.iot.model.WeatherData;
import schemax.iot.rest.ApiClient;
import schemax.iot.rest.ApiInterface;

public class Continuous_Service extends Activity implements TextToSpeech.OnInitListener {
    int i = 0, j = 0;
    List<Weather> weather;
    SQLiteDatabase db;
    TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.continuous__service);
        weather = new ArrayList<Weather>();
        new DBHelper(weather, Continuous_Service.this);
        tts = new TextToSpeech(this, this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        ScheduledExecutorService first = Executors.newScheduledThreadPool(100);
        first.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if (isNetworkAvailable()) {
                    // do some work
                    IOTData();
                }
            }
        }, 0, 1, TimeUnit.MINUTES);  // execute every x seconds

        ScheduledExecutorService speak_every_30_minutes = Executors.newScheduledThreadPool(100);
        speak_every_30_minutes.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                myvoice_from_localdb_after_30_minutes();
            }
        }, 2, 30, TimeUnit.MINUTES);  // execute every x seconds

        ScheduledExecutorService emergency = Executors.newScheduledThreadPool(100);
        emergency.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                myvoice_from_localdb_every_2_minutes();
            }
        }, 1, 2, TimeUnit.MINUTES);  // execute every x seconds



    }
    public void myvoice_from_localdb_after_30_minutes() {
        final ArrayList<Weather> install_offline = new ArrayList<Weather>();
        db = openOrCreateDatabase("IOT_schemax", Context.MODE_PRIVATE, null);
        Cursor c = db.rawQuery("SELECT * FROM myvoice ", null);

        if (c.moveToFirst()) {
            while (!c.isAfterLast()) {
                String voice_id = c.getString(c.getColumnIndex("voice_id"));
                String voice_des = c.getString(c.getColumnIndex("voice_des"));
                String voice_date = c.getString(c.getColumnIndex("voice_date"));
                String voice_time = c.getString(c.getColumnIndex("voice_time"));
                String team = c.getString(c.getColumnIndex("team"));
                String flag = c.getString(c.getColumnIndex("flag"));

                install_offline.add(new Weather(voice_id, voice_des, voice_date, voice_time, team, flag));

                c.moveToNext();
            }
        }
        db.close();

        for (int i = 0; i < install_offline.size(); i++) {

            String temp_date = install_offline.get(i).getDate().toString().concat(" " + install_offline.get(i).getTime() + " ");
            Log.d("tempdate", temp_date);

            Calendar cc = Calendar.getInstance();
            cc.add(Calendar.MINUTE, 0);
            DateFormat df = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
            String Datetime = df.format(cc.getTime());
            Date current_time = null;
            try {
                current_time = df.parse(Datetime);

                Log.d("datetime", String.valueOf(current_time.getTime()));

                SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
                // Date d2 = format.parse("03/12/2017 10:50 AM ");
                Date d2 = format.parse(temp_date);
                Double hoursDifference = Double.valueOf((current_time.getTime() - d2.getTime()) / 60000);
                //int hoursDifference = (Double)((current_time.getTime() - d2.getTime()) / 3600000L);
                Log.d("difference in time", String.valueOf(hoursDifference));
                if (hoursDifference < 0 ) {
                    Log.d("date2 greater", "date2");

                    tts.speak(install_offline.get(i).getVoice_des(), TextToSpeech.QUEUE_FLUSH, null);
                    tts.setLanguage(Locale.ENGLISH);
                    //tts.setPitch(9);
                    tts.setSpeechRate(1);
                    Thread.sleep(20000);
                    // TimeUnit.MINUTES.sleep(1);
                } else {
                    Log.d("date1 greater", "date1");
                }

            } catch (ParseException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


        }
    }

    public void myvoice_from_localdb_every_2_minutes() {
        final ArrayList<Weather> install_offline = new ArrayList<Weather>();
        db = openOrCreateDatabase("IOT_schemax", Context.MODE_PRIVATE, null);
        Cursor c = db.rawQuery("SELECT * FROM myvoice ", null);

        if (c.moveToFirst()) {
            while (!c.isAfterLast()) {
                String voice_id = c.getString(c.getColumnIndex("voice_id"));
                String voice_des = c.getString(c.getColumnIndex("voice_des"));
                String voice_date = c.getString(c.getColumnIndex("voice_date"));
                String voice_time = c.getString(c.getColumnIndex("voice_time"));
                String team = c.getString(c.getColumnIndex("team"));
                String flag = c.getString(c.getColumnIndex("flag"));

                install_offline.add(new Weather(voice_id, voice_des, voice_date, voice_time, team, flag));

                c.moveToNext();
            }
        }
        db.close();

        for (int i = 0; i < install_offline.size(); i++) {

            String temp_date = install_offline.get(i).getDate().toString().concat(" " + install_offline.get(i).getTime() + " ");
            Log.d("tempdate", temp_date);

            Calendar cc = Calendar.getInstance();
            cc.add(Calendar.MINUTE, 0);
            DateFormat df = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
            String Datetime = df.format(cc.getTime());
            Date current_time = null;
            try {
                current_time = df.parse(Datetime);

                Log.d("datetime", String.valueOf(current_time.getTime()));

                SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
                // Date d2 = format.parse("03/12/2017 10:50 AM ");
                Date d2 = format.parse(temp_date);
                Double hoursDifference = Double.valueOf((current_time.getTime() - d2.getTime()) / 60000);
                //int hoursDifference = (Double)((current_time.getTime() - d2.getTime()) / 3600000L);
                Log.d("difference in time", String.valueOf(hoursDifference));
                 if (hoursDifference < 0 && install_offline.get(i).getFlag().toString().equals("true")) {
                    Log.d("date2 greater", "date2");

                    tts.speak(install_offline.get(i).getVoice_des(), TextToSpeech.QUEUE_FLUSH, null);
                    tts.setLanguage(Locale.ENGLISH);
                    //tts.setPitch(9);
                    tts.setSpeechRate(1);
                    Thread.sleep(20000);
                    // TimeUnit.MINUTES.sleep(1);
                } else {
                    Log.d("date1 greater", "date1");
                }

            } catch (ParseException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


         }
    }

    public void IOTData() {
        ApiInterface apiService = ApiClient.getWeatherDetails().create(ApiInterface.class);
        Call<WeatherData> call = apiService.getWeatherDetails();
        call.enqueue(new Callback<WeatherData>() {
            @Override
            public void onResponse(Call<WeatherData> call, Response<WeatherData> response) {
                String result = String.valueOf(response.body().getresult());

                String res = String.valueOf(response.code());
                if (res.equals("200")) {
                    Toast.makeText(getBaseContext(), result.toString(), Toast.LENGTH_SHORT).show();
                    weather = response.body().getresult();
                    if (weather.size()>0){
                        db = openOrCreateDatabase("IOT_schemax", Context.MODE_PRIVATE, null);
                        db.delete("myvoice", null,null);
                        db.close();
                        new DBHelper(weather, Continuous_Service.this);
                    }

                } else {
                    Toast.makeText(getBaseContext(), "Server Not Found ", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<WeatherData> call, Throwable throwable) {
                // Toast.makeText(getBaseContext(), throwable.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {

            int result = tts.setLanguage(Locale.US);

            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                //Log.e("TTS", "This Language is not supported");
            } else {
                speakOut();
            }

        } else {
            //Log.e("TTS", "Initilization Failed!");
        }
    }

    private void speakOut() {
        tts.speak("Srinivas I am Jessy ", TextToSpeech.QUEUE_FLUSH, null);
        tts.setLanguage(Locale.ENGLISH);
        //tts.setPitch(9);
        tts.setSpeechRate(1);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}
