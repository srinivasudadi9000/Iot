package schemax.iot.model;

import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;

import org.json.JSONObject;

import java.util.List;

/**
 * Created by USER on 25-07-2017.
 */

public class WeatherData {
    @SerializedName("result")
    List<Weather> result;

    @SerializedName("status")
    String status;



    public List<Weather> getresult() {
        return result;
    }

    public String getStatus() {
        return status;
    }

}
