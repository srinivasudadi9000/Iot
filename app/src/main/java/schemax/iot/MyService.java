package schemax.iot;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.TimeUtils;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import schemax.iot.model.Weather;
import schemax.iot.model.WeatherData;
import schemax.iot.rest.ApiClient;
import schemax.iot.rest.ApiInterface;

/**
 * Created by srinivas on 07/12/17.
 */

public class MyService extends Service implements TextToSpeech.OnInitListener {
    List<Weather> weather;
    SQLiteDatabase db;
    TextToSpeech tts;
//108000000
    public static final int notify = 30000;  //interval between two services(Here Service run every 5 Minute)
    private Handler mHandler = new Handler();   //run on another Thread to avoid crash
    private Timer mTimer = null;    //timer handling

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("Myservice","IBINDER");
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        Log.d("Myservice","ONcreate");
        if (mTimer != null) // Cancel if already existed
            mTimer.cancel();
        else
            mTimer = new Timer();   //recreate new
        mTimer.scheduleAtFixedRate(new TimeDisplay(), 0, TimeUnit.MINUTES.toMinutes(5));   //Schedule task
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mTimer.cancel();    //For Cancel Timer
        Log.d("Myservice","onDestroy");
        Toast.makeText(this, "Service is Destroyed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {

            int result = tts.setLanguage(Locale.US);

            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                //Log.e("TTS", "This Language is not supported");
            } else {
                //speak_btn.setEnabled(true);
              //  speakOut();
            }

        } else {
            //Log.e("TTS", "Initilization Failed!");
        }
    }
    private void speakOut() {

      //  String text = spell_et.getText().toString();

        tts.speak("Hello I am jessie", TextToSpeech.QUEUE_FLUSH, null);
        tts.setLanguage(Locale.ENGLISH);
        //tts.setPitch(9);
        tts.setSpeechRate(1);
    }

    //class TimeDisplay for handling task
   public class TimeDisplay extends TimerTask {
        @Override
        public void run() {
            // run on another thread
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    clearPreferences();
                    if (isNetworkAvailable()) {
                        IOTData();
                    }else {
                         looprunning();
                    }
                    // display toast
                    Log.d("Myservice","service is running");
                    Toast.makeText(MyService.this, "Service is running", Toast.LENGTH_SHORT).show();
                }
            });
        }

        public   void IOTData() {
            tts = new TextToSpeech(MyService.this, MyService.this);
            weather = new ArrayList<Weather>();
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

                        new DBHelper(weather, MyService.this);

                        // mAdapter = ;
                  /*  RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
                    voice_recyler.setLayoutManager(mLayoutManager);
                    voice_recyler.setItemAnimator(new DefaultItemAnimator());
                    voice_recyler.setAdapter(new VoiceAdapter(weather, MainActivity.this));
*/
                        looprunning();

                        //weather_recyler.setAdapter(new WeatherAdapter(weather, R.layout.weather_single, getApplicationContext()));*/

                    } else {
                        Toast.makeText(getBaseContext(), "Server Not Found ", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<WeatherData> call, Throwable throwable) {
                    Toast.makeText(getBaseContext(), throwable.toString(), Toast.LENGTH_SHORT).show();
                    if (isNetworkAvailable()){
                        IOTData();
                    }else {
                        looprunning();
                    }
                }
            });
        }

        public void looprunning()
        {
            final String[] verification = {""};

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
            //Log.d("installoffline", String.valueOf(install_offline.size()));
            for (int i = 0; i < install_offline.size(); i++) {
                      final String flagg = install_offline.get(i).getFlag().toString();
                final String temp_date = install_offline.get(i).getDate().toString().concat(" " + install_offline.get(i).getTime() + " ");
                //Log.d("tempdate", temp_date);

                Calendar cc = Calendar.getInstance();
                cc.add(Calendar.MINUTE, 0);
                final DateFormat df = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
                final String Datetime = df.format(cc.getTime());
                final Date[] current_time = {null};
                final int finalI = i;
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //Log.d("running run","Thread Running");
                        try {
                            current_time[0] = df.parse(Datetime);

                            //Log.d("datetime", String.valueOf(current_time[0].getTime()));

                            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
                            // Date d2 = format.parse("03/12/2017 10:50 AM ");
                            Date d2 = format.parse(temp_date);
                            Double hoursDifference = Double.valueOf((current_time[0].getTime() - d2.getTime()) / 60000);
                            //int hoursDifference = (Double)((current_time.getTime() - d2.getTime()) / 3600000L);
                            //Log.d("difference in time", String.valueOf(hoursDifference));
                            if (hoursDifference < 0 ) {
                                //Log.d("date2 greater", "date2");
                                verification[0] = "verification";

                                tts.speak("MYService "+install_offline.get(finalI).getVoice_des(), TextToSpeech.QUEUE_FLUSH, null);
                                tts.setLanguage(Locale.ENGLISH);
                                //tts.setPitch(9);
                                tts.setSpeechRate(1);
                                Log.d("sppech",install_offline.get(finalI).getVoice_des().toString());
                                Thread.sleep(20000);
                                // TimeUnit.MINUTES.sleep(1);
                            } else {
                                //Log.d("date1 greater", "date1");
                            }

                        } catch (ParseException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                });
                t.start();
                if (t.isAlive()){
                    //Log.d("running alive", String.valueOf(t.isAlive()));
                    try {
                        t.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    //Log.d("running destroy", String.valueOf(t.isAlive()));
                }

            }

        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void clearPreferences() {
        try {
            // clearing app data
            Runtime runtime = Runtime.getRuntime();
            runtime.exec("pm clear YOUR_APP_PACKAGE_GOES HERE");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
