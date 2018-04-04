package com.example.pm.smarthomeui;

import android.content.Context;
import android.content.Intent;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class AdapterData {
    public String name;
    public String description;
    public String type;
    public Integer sensorState;

    AdapterData(String name, String description, String type, Integer sensorState) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.sensorState = sensorState;
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

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
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

    // Provide a suitable constructor (depends on the kind of dataset)
    DeviceAdapter(List<AdapterData> myDataset, Context context1) {
        values = myDataset;
        context = context1;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.sensors_row_layout, parent, false);
        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        final AdapterData item = values.get(position);
        holder.description.setText(item.description);
        holder.icon.setImageResource(item.getIcon());
        holder.name.setText(item.name);
        holder.switch1.setChecked(item.sensorState == 1);

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

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
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

    public static int getRandom(Integer length) {
        return new Random().nextInt(length);
    }

    private List<AdapterData> getSensorsData() {
        List<AdapterData> data = new ArrayList<>();

        String[] types = new String[] {"camera", "temp_sensor", "door_switch", "another"};

        // TODO: here will be rest api request in future
        for (int i=0; i<12; i++) {
            data.add(new AdapterData(
                    "Тестовый датчик " + i,
                    "Здесь идет описание",
                    types[getRandom(types.length)],
                    getRandom(2)));
        }

        return data;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensors);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        navigation.getMenu().getItem(1).setChecked(true);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.sensor_recycler_view);
        recyclerView.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        List<AdapterData> input = getSensorsData();

        DeviceAdapter mAdapter = new DeviceAdapter(input, SensorsActivity.this);

        recyclerView.setAdapter(mAdapter);
    }
}
