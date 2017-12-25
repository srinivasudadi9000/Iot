package schemax.iot.rest;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import schemax.iot.model.WeatherData;

/**
 * Created by USER on 25-07-2017.
 */

public interface ApiInterface {
    @GET("weather")
    Call<WeatherData> getWeatherDetails();

}
