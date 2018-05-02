package com.example.pm.smarthomeui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.provider.ContactsContract;
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
    String image;

    HomeAdapterData(String description, String image) {
        this.image = image;
        this.description = description;
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
        return new HomeDeviceAdapter.ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
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
        if (holder.rowImage.getHeight() == 0) {
            ThumbnailTask imgTask = new ThumbnailTask(holder.rowImage, item.image);
            imgTask.execute();
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return values.size();
    }

}

class ThumbnailTask extends AsyncTask<Integer, Void, Bitmap> {
    @SuppressLint("StaticFieldLeak")
    private ImageView imageView;
    private Bitmap file;
    private String url;

    ThumbnailTask(ImageView imageView, String url) {
        this.url = url;
        this.imageView = imageView;
    }

    @Override
    protected Bitmap doInBackground(Integer... params) {

        Bitmap image = null;
        try {
            image = BitmapFactory.decodeStream((InputStream) new URL(this.url).getContent());
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.file = image;

        return image;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (isCancelled()) {
            bitmap = null;
        }
        if (this.file != null) {
            this.imageView.setImageBitmap(this.file);
        } else {
            this.imageView.setImageResource(R.drawable.work_space);
        }
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
        for (int i = 0; i < array.length(); i++) {
            JSONObject entity = array.getJSONObject(i);

            String desc = entity.getString("description");
            String img = entity.getString("icon");

            dataArray.add(new HomeAdapterData(desc, img));
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

        HomeData homeTask = new HomeData(this);
        homeTask.execute((Void) null);
    }

    public class HomeData extends AsyncTask<Void, Void, Boolean> {

        private Context context;

        HomeData(Context context) {
            this.context = context;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            RecyclerView homeRecyclerView = (RecyclerView) findViewById(R.id.home_recycler_view);
            homeRecyclerView.setHasFixedSize(true);

            LinearLayoutManager llm = new LinearLayoutManager(this.context);
            homeRecyclerView.setLayoutManager(llm);

            List<HomeAdapterData> input = null;
            try {
                input = getSensorsData();
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                return false;
            }

            HomeDeviceAdapter mAdapter = new HomeDeviceAdapter(input, HomeActivity.this);
            homeRecyclerView.setAdapter(mAdapter);

            return true;
        }
    }
}
