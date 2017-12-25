package schemax.iot.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by USER on 25-07-2017.
 */

public class Weather {
    @SerializedName("id")
    String id;

    @SerializedName("voice_des")
    String voice_des;

    @SerializedName("date")
    String date;

    @SerializedName("time")
    String time;

    @SerializedName("team_members")
    String team_members;

    @SerializedName("flag")
    String flag;

    public Weather(String id, String voice_des, String date,String time,String team_members,String flag){
        this.id= id;this.voice_des = voice_des;this.date = date;this.time = time ;this.team_members = team_members;
        this.flag = flag;
    }

    public String getFlag() {
        return flag;
    }

    public String getId() {
        return id;
    }

    public String getVoice_des() {
        return voice_des;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getTeam_members() {
        return team_members;
    }

    public void setVoice_des(String voice_des) {
        this.voice_des = voice_des;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setTeam_members(String team_members) {
        this.team_members = team_members;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
