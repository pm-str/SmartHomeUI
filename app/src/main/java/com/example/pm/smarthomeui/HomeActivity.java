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
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

class HomeAdapterData {
    String description;
    Bitmap image;

    HomeAdapterData(String description, Bitmap image) {
        this.image = image;
        this.description = description;
    }
}

class HomeDeviceAdapter extends RecyclerView.Adapter<HomeDeviceAdapter.ViewHolder> {
    private Context context;
    private List<HomeAdapterData> values;

    public class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        TextView description;
        ImageView rowImage;

        public View layout;

        ViewHolder(View v) {
            super(v);
            layout = v;
            rowImage = (ImageView) v.findViewById(R.id.row_image);
            description = (TextView) v.findViewById(R.id.home_description);
        }
    }

    public void add(int position, HomeAdapterData item) {
        values.add(position, item);
        notifyItemInserted(position);
    }

    private void remove(int position) {
        values.remove(position);
        notifyItemRemoved(position);
    }

    HomeDeviceAdapter(List<HomeAdapterData> myDataset, Context context1) {
        values = myDataset;
        context = context1;
    }

    @Override
    public HomeDeviceAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.home_row_layout, parent, false);
        return new HomeDeviceAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        View.OnClickListener entityDetailsView = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ParticularEntityActivity.class);
                context.startActivity(intent);
            }
        };

        final HomeAdapterData item = values.get(position);
        holder.description.setText(item.description);
        holder.layout.setOnClickListener(entityDetailsView);
        if (item.image != null) {
            holder.rowImage.setImageBitmap(item.image);
        } else {
            holder.rowImage.setImageResource(R.drawable.work_space);
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return values.size();
    }

}

public class HomeActivity extends AppCompatActivity {
    public HomeData homeTask;
    public View mProgressView;

        private BottomNavigationView.OnNavigationItemSelectedListener onNavItemSL = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            if (homeTask != null) {
                homeTask.cancel(false);
            }
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    return true;
                case R.id.navigation_dashboard:
                    Intent intent2 = new Intent(HomeActivity.this, SensorsActivity.class);
                    startActivity(intent2);
                    return true;
                case R.id.navigation_notifications:
                    Intent intent3 = new Intent(HomeActivity.this, AlertsActivity.class);
                    startActivity(intent3);
                    return true;
                case R.id.navigation_settings:
                    Intent intent4 = new Intent(HomeActivity.this, SettingsActivity.class);
                    startActivity(intent4);
                    return true;
            }
            return false;
        }
    };

    private String getSensorsData() throws IOException, JSONException {
        SharedPreferences preference = getSharedPreferences("myPrefs", MODE_PRIVATE);

        String url = "entity/";
        return HttpGETClient.main(url, preference);
    }

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_home);

        mProgressView = findViewById(R.id.login_progress);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(onNavItemSL);

        navigation.getMenu().getItem(0).setChecked(true);

        this.homeTask = new HomeData(this);
        AsyncTask.Status status = this.homeTask.getStatus();
        if (status == AsyncTask.Status.RUNNING) {
            this.homeTask.cancel(true);
        }
        this.homeTask.execute((Void) null);
    }

    public class HomeData extends AsyncTask<Void, Void, Boolean> {

        private Context context;
        private JSONArray array;
        private List<Bitmap> images = new ArrayList<>();
        private RecyclerView homeRecyclerView;

        HomeData(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            setVisibility(true);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            this.homeRecyclerView = (RecyclerView) findViewById(R.id.home_recycler_view);
            homeRecyclerView.setHasFixedSize(true);

            LinearLayoutManager llm = new LinearLayoutManager(this.context);
            homeRecyclerView.setLayoutManager(llm);

            try {
                this.array = new JSONArray(getSensorsData());

                for (int i = 0; i < this.array.length(); i++) {
                    if(isCancelled()){ return false; }
                    JSONObject entity = this.array.getJSONObject(i);
                    String img = entity.getString("icon");
                    try {
                        this.images.add(BitmapFactory.decodeStream((InputStream) new URL(img).getContent()));
                    } catch (IOException e) {
                        Bitmap loading = BitmapFactory.decodeResource(context.getResources(), R.drawable.no);
                        this.images.add(loading);
                    }
                }
            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            List<HomeAdapterData> dataArray = new ArrayList<>();

            for (int i = 0; i < this.array.length(); i++) {
                if(isCancelled()){ return; }
                JSONObject entity = null;
                try {
                    entity = this.array.getJSONObject(i);
                    String name = entity.getString("name");

                    dataArray.add(new HomeAdapterData(name, this.images.get(i)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                HomeDeviceAdapter mAdapter = new HomeDeviceAdapter(dataArray, HomeActivity.this);
                homeRecyclerView.setAdapter(mAdapter);
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
