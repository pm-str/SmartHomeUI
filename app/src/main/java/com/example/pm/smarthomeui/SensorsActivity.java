package com.example.pm.smarthomeui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class AdapterData {
    public String name;
    public String description;
    public String type;
    public Integer value;
    public Integer entity_id;
    public Integer sensor_id;
    public Boolean is_active;

    AdapterData(String name, String description, String type, Integer sensor_id,
                Integer value, Boolean is_active, Integer entity_id) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.value = value;
        this.is_active = is_active;
        this.sensor_id = sensor_id;
        this.entity_id = entity_id;

    }

    public Integer getIcon() {
        switch (this.type) {
            case "camera":
                return R.drawable.photo_camera;
            case "temp_sensor":
                return R.drawable.thermometer;
            case "door_switch":
                return R.drawable.doorway;
            default:
                return R.drawable.sata;
        }
    }
}

class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.ViewHolder> {
    private Context context;
    private List<AdapterData> values;

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView description;
        TextView name;
        ImageView icon;
        Switch switch1;

        public View layout;

        ViewHolder(View v) {
            super(v);
            layout = v;
            icon = (ImageView) v.findViewById(R.id.icon);
            description = (TextView) v.findViewById(R.id.description);
            name = (TextView) v.findViewById(R.id.name);
            switch1 = (Switch) v.findViewById(R.id.switch1);
        }
    }

    public void add(int position, AdapterData item) {
        values.add(position, item);
        notifyItemInserted(position);
    }

    private void remove(int position) {
        values.remove(position);
        notifyItemRemoved(position);
    }

    DeviceAdapter(List<AdapterData> myDataset, Context context1) {
        values = myDataset;
        context = context1;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.sensors_row_layout, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final AdapterData item = values.get(position);
        holder.description.setText(item.description);
        holder.icon.setImageResource(item.getIcon());
        holder.name.setText(item.name);
        holder.switch1.setChecked(item.value == 1);

        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ParticularSensorActivity.class);
                context.startActivity(intent);
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return values.size();
    }

}

public class SensorsActivity extends AppCompatActivity {
    public SensorAsyncTask homeTask;
    public View mProgressView;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            if (homeTask != null) {
                homeTask.cancel(false);
            }
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    Intent intent1 = new Intent(SensorsActivity.this, HomeActivity.class);
                    startActivity(intent1);
                    return true;
                case R.id.navigation_dashboard:
                    return true;
                case R.id.navigation_notifications:
                    Intent intent3 = new Intent(SensorsActivity.this, AlertsActivity.class);
                    startActivity(intent3);
                    return true;
                case R.id.navigation_settings:
                    Intent intent4 = new Intent(SensorsActivity.this, SettingsActivity.class);
                    startActivity(intent4);
                    return true;
            }
            return false;
        }
    };

    private void setVisibility(final boolean show) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mProgressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    private String getSensorsData() {
        SharedPreferences preference = getSharedPreferences("myPrefs", MODE_PRIVATE);

        String url = "sensor/";
        return HttpGETClient.main(url, preference);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensors);
        mProgressView = findViewById(R.id.login_progress);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        navigation.getMenu().getItem(1).setChecked(true);

        this.homeTask = new SensorAsyncTask(this);
        AsyncTask.Status status = this.homeTask.getStatus();
        if (status == AsyncTask.Status.RUNNING) {
            this.homeTask.cancel(true);
        }
        this.homeTask.execute((Void) null);
    }

    public class SensorAsyncTask extends AsyncTask<Void, Void, Boolean> {
        private Context context;
        private JSONArray array;
        private RecyclerView sensorRecycleView;

        SensorAsyncTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            setVisibility(true);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            this.sensorRecycleView = findViewById(R.id.sensor_recycler_view);
            this.sensorRecycleView.setHasFixedSize(true);

            LinearLayoutManager layoutManager = new LinearLayoutManager(context);
            this.sensorRecycleView.setLayoutManager(layoutManager);
            try {
                this.array = new JSONArray(getSensorsData());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            List<AdapterData> dataArray = new ArrayList<>();

            for (int i = 0; i < this.array.length(); i++) {
                if(isCancelled()){ return; }
                JSONObject entity;
                try {
                    entity = this.array.getJSONObject(i);

                    Integer id = Integer.parseInt(entity.getString("id"));
                    String name = entity.getString("name");
                    String description = entity.getString("description");
                    String s_type = entity.getString("s_type");
                    Integer s_value = Integer.parseInt(entity.getString("s_value"));
                    Boolean is_active = (Boolean.parseBoolean(entity.getString("is_active")));
                    Integer entity_id = Integer.parseInt(entity.getString("entity"));

                    dataArray.add(new AdapterData(name, description, s_type, id, s_value, is_active, entity_id));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                DeviceAdapter mAdapter = new DeviceAdapter(dataArray, SensorsActivity.this);
                this.sensorRecycleView.setAdapter(mAdapter);
            }
            setVisibility(false);
        }

        @Override
        protected void onCancelled() {
            homeTask = null;
            setVisibility(false);
        }
    }
}
