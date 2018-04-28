package com.example.pm.smarthomeui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
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
import java.util.ArrayList;
import java.util.List;

class HomeAdapterData {
    public String description;
    public String image;

    HomeAdapterData(String description, String image) {
        this.image = image;
        this.description = description;
    }

    public Integer getImage() {
        switch (this.image) {
            case "1":
                return R.drawable.home_banner;
            case "2":
                return R.drawable.toyota_prius;
            default:
                return R.drawable.work_space;
        }
    }
}

class HomeDeviceAdapter extends RecyclerView.Adapter<HomeDeviceAdapter.ViewHolder> {
    private Context context;
    private List<HomeAdapterData> values;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
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

    // Provide a suitable constructor (depends on the kind of dataset)
    HomeDeviceAdapter(List<HomeAdapterData> myDataset, Context context1) {
        values = myDataset;
        context = context1;
    }

    @Override
    public HomeDeviceAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.home_row_layout, parent, false);
        // set the view's size, margins, paddings and layout parameters
        HomeDeviceAdapter.ViewHolder vh = new HomeDeviceAdapter.ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        View.OnClickListener entityDetailsView = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ParticularEntityActivity.class);
                context.startActivity(intent);
            }
        };

        final HomeAdapterData item = values.get(position);
        holder.description.setText(item.description);
        holder.rowImage.setImageResource(item.getImage());
        holder.layout.setOnClickListener(entityDetailsView);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return values.size();
    }

}

public class HomeActivity extends AppCompatActivity {
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
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

    private List<HomeAdapterData> getSensorsData() throws IOException, JSONException {
        List<HomeAdapterData> dataArray = new ArrayList<>();
        SharedPreferences preference = getSharedPreferences("myPrefs", MODE_PRIVATE);

        String url = "entity/";
        String dataString = HttpGETClient.main(url, preference);

        JSONArray array = new JSONArray(dataString);
        for (int i = 0; i < array.length(); i++)
        {
            JSONObject entity = array.getJSONObject(i);

            dataArray.add(new HomeAdapterData(
                    entity.getString("description"),
                    entity.getString("icon")));
        }

        return dataArray;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        navigation.getMenu().getItem(0).setChecked(true);

        RecyclerView homeRecyclerView = (RecyclerView) findViewById(R.id.home_recycler_view);
        homeRecyclerView.setHasFixedSize(true);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        homeRecyclerView.setLayoutManager(llm);

        HomeData homeTask = new HomeData(homeRecyclerView);
        homeTask.execute((Void) null);
    }

    public class HomeData extends AsyncTask<Void, Void, Boolean> {

        private RecyclerView view;

        HomeData(RecyclerView recyclerView) {
            this.view = recyclerView;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            List<HomeAdapterData> input = null;
            try {
                input = getSensorsData();
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                return false;
            }

            HomeDeviceAdapter mAdapter = new HomeDeviceAdapter(input, HomeActivity.this);
            this.view.setAdapter(mAdapter);

            return true;
        }
    }
}
