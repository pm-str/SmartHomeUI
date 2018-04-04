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
import android.widget.TextView;
import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.List;


class NotificationAdapterData {
    public String time;
    public String description;

    NotificationAdapterData(String time, String description) {
        this.time = time;
        this.description = description;
    }
}

class AlertsAdapterData {
    public String date;
    public List<NotificationAdapterData> details;

    AlertsAdapterData(String date, List<NotificationAdapterData> details) {
        this.date = date;
        this.details = details;
    }
}

class NotificationRecyclerViewAdapter extends RecyclerView.Adapter<NotificationRecyclerViewAdapter.ViewHolder> {
    private Context context;
    private List<NotificationAdapterData> values;

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView time;
        TextView description;

        public View layout;

        ViewHolder(View v) {
            super(v);
            layout = v;
            time = (TextView) v.findViewById(R.id.timeTextView);
            description = (TextView) v.findViewById(R.id.descriptionTextView);
        }
    }

    public void add(int position, NotificationAdapterData item) {
        values.add(position, item);
        notifyItemInserted(position);
    }

    private void remove(int position) {
        values.remove(position);
        notifyItemRemoved(position);
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    NotificationRecyclerViewAdapter(List<NotificationAdapterData> myDataset, Context context1) {
        values = myDataset;
        context = context1;
    }

    @Override
    public NotificationRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.notification_row_layout, parent, false);
        // set the view's size, margins, paddings and layout parameters
        NotificationRecyclerViewAdapter.ViewHolder vh = new NotificationRecyclerViewAdapter.ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final NotificationAdapterData item = values.get(position);
        holder.time.setText(item.time);
        holder.description.setText(item.description);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return values.size();
    }

}

class AlertsRecyclerViewAdapter extends RecyclerView.Adapter<AlertsRecyclerViewAdapter.ViewHolder> {
    private Context context;
    private List<AlertsAdapterData> values;

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView date;
        RecyclerView rowDetails;

        public View layout;

        ViewHolder(View v) {
            super(v);
            layout = v;
            date = (TextView) v.findViewById(R.id.date_text_view);
            rowDetails = (RecyclerView) v.findViewById(R.id.notifications_list_recycler_view);
        }
    }

    public void add(int position, AlertsAdapterData item) {
        values.add(position, item);
        notifyItemInserted(position);
    }

    private void remove(int position) {
        values.remove(position);
        notifyItemRemoved(position);
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    AlertsRecyclerViewAdapter(List<AlertsAdapterData> myDataset, Context context1) {
        values = myDataset;
        context = context1;
    }

    @Override
    public AlertsRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.alerts_row_layout, parent, false);
        // set the view's size, margins, paddings and layout parameters
        AlertsRecyclerViewAdapter.ViewHolder vh = new AlertsRecyclerViewAdapter.ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
//        View.OnClickListener entityDetailsView = new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(context, ParticularEntityActivity.class);
//                context.startActivity(intent);
//            }
//        };

        final AlertsAdapterData item = values.get(position);
        holder.date.setText(item.date);
//        holder.date.setOnClickListener(entityDetailsView);

        holder.rowDetails.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(context);
        holder.rowDetails.setLayoutManager(llm);

        List<NotificationAdapterData> input = item.details;

        NotificationRecyclerViewAdapter mAdapter = new NotificationRecyclerViewAdapter(input, context);

        holder.rowDetails.setAdapter(mAdapter);

//        holder.rowImage.setOnClickListener(entityDetailsView);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return values.size();
    }

}

public class AlertsActivity extends AppCompatActivity {
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    Intent intent1 = new Intent(AlertsActivity.this, HomeActivity.class);
                    startActivity(intent1);
                    return true;
                case R.id.navigation_dashboard:
                    Intent intent2 = new Intent(AlertsActivity.this, SensorsActivity.class);
                    startActivity(intent2);
                    return true;
                case R.id.navigation_settings:
                    Intent intent3 = new Intent(AlertsActivity.this, SettingsActivity.class);
                    startActivity(intent3);
                    return true;
                case R.id.navigation_notifications:
                    return true;
            }
            return false;
        }
    };

    private List<AlertsAdapterData> getNotificationsData() {
        List<AlertsAdapterData> data = new ArrayList<>();

        data.add(new AlertsAdapterData("01 января 2017", asList(
                new NotificationAdapterData("07:15", "Открыта дверь в гостинную."),
                new NotificationAdapterData("14:13", "Позвонили в дверь."))
        ));
        data.add(new AlertsAdapterData("02 февраля 2017", asList(
                new NotificationAdapterData("06:00", "Поздний рассвет, включено освещение, уютненько."),
                new NotificationAdapterData("13:40", "Открыта дверь в гостинную."))
        ));
        data.add(new AlertsAdapterData("03 марта 2018", asList(
                new NotificationAdapterData("08:00", "Что-то обнаруженно, изучаю."),
                new NotificationAdapterData("09:20", "Включен будильник."),
                new NotificationAdapterData("09:30", "Получено сообщение от Джонатона."),
                new NotificationAdapterData("16:53", "В рабочей комнате кто-то есть."))
        ));

        return data;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alerts);

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        navigation.getMenu().getItem(2).setChecked(true);

        RecyclerView alertsRecyclerView = (RecyclerView) findViewById(R.id.alerts_recycler_view);
        alertsRecyclerView.setHasFixedSize(true);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        alertsRecyclerView.setLayoutManager(llm);

        List<AlertsAdapterData> input = getNotificationsData();

        AlertsRecyclerViewAdapter mAdapter = new AlertsRecyclerViewAdapter(input, AlertsActivity.this);

        alertsRecyclerView.setAdapter(mAdapter);
    }
}
