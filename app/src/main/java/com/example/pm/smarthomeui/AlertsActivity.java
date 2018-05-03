package com.example.pm.smarthomeui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


class NotificationAdapterData {
    public String time;
    public String description;
    public Integer sensor_id;
    public Integer entity_id;

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

    NotificationRecyclerViewAdapter(List<NotificationAdapterData> myDataset, Context context1) {
        values = myDataset;
        context = context1;
    }

    @Override
    public NotificationRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.notification_row_layout, parent, false);
        NotificationRecyclerViewAdapter.ViewHolder vh = new NotificationRecyclerViewAdapter.ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final NotificationAdapterData item = values.get(position);
        holder.time.setText(item.time);
        holder.description.setText(item.description);
    }

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

    AlertsRecyclerViewAdapter(List<AlertsAdapterData> myDataset, Context context1) {
        values = myDataset;
        context = context1;
    }

    @Override
    public AlertsRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.alerts_row_layout, parent, false);
        AlertsRecyclerViewAdapter.ViewHolder vh = new AlertsRecyclerViewAdapter.ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final AlertsAdapterData item = values.get(position);
        holder.date.setText(item.date);

        holder.rowDetails.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(context);
        holder.rowDetails.setLayoutManager(llm);

        List<NotificationAdapterData> input = item.details;

        NotificationRecyclerViewAdapter mAdapter = new NotificationRecyclerViewAdapter(input, context);

        holder.rowDetails.setAdapter(mAdapter);
    }

    @Override
    public int getItemCount() {
        return values.size();
    }

}

public class AlertsActivity extends AppCompatActivity {
    public AlertsAsyncData alertAsyncTask;
    public View mProgressView;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            if (alertAsyncTask != null) {
                alertAsyncTask.cancel(false);
            }

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

    private JSONArray getNotificationsData() throws JSONException {
        SharedPreferences preference = getSharedPreferences("myPrefs", MODE_PRIVATE);

        String url = "event/";

        return new JSONArray(HttpGETClient.main(url, preference));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alerts);

        mProgressView = findViewById(R.id.login_progress);
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        navigation.getMenu().getItem(2).setChecked(true);

        this.alertAsyncTask = new AlertsAsyncData(this);
        AsyncTask.Status status = this.alertAsyncTask.getStatus();
        if (status == AsyncTask.Status.RUNNING) {
            this.alertAsyncTask.cancel(true);
        }
        this.alertAsyncTask.execute((Void) null);
    }

    public class AlertsAsyncData extends AsyncTask<Void, Void, Boolean> {

        private Context context;
        private JSONArray array;
        private RecyclerView alertsRecyclerView;
        private List<AlertsAdapterData> data;

        AlertsAsyncData(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            setVisibility(true);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            alertsRecyclerView = findViewById(R.id.alerts_recycler_view);
            alertsRecyclerView.setHasFixedSize(true);

            LinearLayoutManager llm = new LinearLayoutManager(context);
            alertsRecyclerView.setLayoutManager(llm);
            try {
                if (isCancelled()) { return false; }
                array = getNotificationsData();
                HashMap<String, List<NotificationAdapterData>> hm = new HashMap<>();

                data = new ArrayList<>();

                for (int i = 0; i < array.length(); i++) {
                    JSONObject entity = array.getJSONObject(i);

                    String date = entity.getString("date");
                    String time = entity.getString("time");
                    String desc = entity.getString("description");

                    if (!hm.containsKey(date)) {
                        hm.put(date, new ArrayList<NotificationAdapterData>());
                    }
                    hm.get(date).add(new NotificationAdapterData(time, desc));
                }

                for (HashMap.Entry<String, List<NotificationAdapterData>> aSet: hm.entrySet()) {
                    String key = aSet.getKey();
                    List<NotificationAdapterData> value = hm.get(key);

                    data.add(new AlertsAdapterData(key, value));
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if (isCancelled()) { return; }
            AlertsRecyclerViewAdapter mAdapter = new AlertsRecyclerViewAdapter(this.data, AlertsActivity.this);
            alertsRecyclerView.setAdapter(mAdapter);

            setVisibility(false);
        }

        @Override
        protected void onCancelled() {
            alertAsyncTask = null;
            setVisibility(false);
        }
    }

}
