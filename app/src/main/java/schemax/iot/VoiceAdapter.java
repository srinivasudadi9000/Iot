package schemax.iot;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import schemax.iot.model.Weather;

/**
 * Created by srinivas on 29/11/17.
 */

public class VoiceAdapter extends RecyclerView.Adapter<VoiceAdapter.MyViewHolder>{
    List<Weather> weather;
    Context context;

    public VoiceAdapter(List<Weather> weather, MainActivity mainActivity) {
        this.weather = weather; this.context = mainActivity;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.voice,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.applicationname.setText(weather.get(position).getVoice_des());
        holder.applicationno.setText(weather.get(position).getDate());
    }

    @Override
    public int getItemCount() {
        return weather.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView applicationname, applicationno;

        public MyViewHolder(View itemView) {
            super(itemView);
            applicationname = (TextView) itemView.findViewById(R.id.voice_des);
            applicationno = (TextView) itemView.findViewById(R.id.voice_date);

        }
    }
}
