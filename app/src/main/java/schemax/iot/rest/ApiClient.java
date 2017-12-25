package schemax.iot.rest;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by USER on 25-07-2017.
 */

public class ApiClient {
    public static final String BASE_URL = "http://sams.mmos.in/iot/getvoice.php/";
    private static Retrofit retrofit = null;

    public static Retrofit getWeatherDetails(){
        if (retrofit==null){
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

 }
