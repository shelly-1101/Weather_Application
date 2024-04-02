package com.example.insta_weather;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    TextView cityName, temperature, weatherDescription, dateAndTime, humidity, aqi, windSpeed, feelslike, pressure, loadingTextview, maxtemp, mintemp, visibility;
    SearchView searchView;
    ImageView imageview;

    RelativeLayout relativeLayout,parentLayout;

    LottieAnimationView lottieAnimationView;
    TextView loadTV;


    String urlMetrics = "https://api.openweathermap.org/data/2.5/weather?lat=%s&lon=%s&appid=00b57cd25d3c916baef0cda450370eb3&units=metric";
    String urlForAqi = "https://api.openweathermap.org/data/2.5/air_pollution?lat=%s&lon=%s&appid=00b57cd25d3c916baef0cda450370eb3";
    String urlSearchCity = "https://api.openweathermap.org/data/2.5/weather?q=%s&appid=00b57cd25d3c916baef0cda450370eb3&units=metric";
    String tempUrl = urlSearchCity;
    double lat,lon;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //to disable night mode in the app
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        cityName = findViewById(R.id.city_tv);
        temperature = findViewById(R.id.temp_tv);
        weatherDescription = findViewById(R.id.description_tv);
        dateAndTime = findViewById(R.id.date_time_tv);
        humidity = findViewById(R.id.humidity);
        aqi = findViewById(R.id.aqi);
        windSpeed = findViewById(R.id.windSpeed);
        feelslike = findViewById(R.id.feels_like);
        pressure = findViewById(R.id.pressure);
        imageview = findViewById(R.id.icon_iv);
        mintemp = findViewById(R.id.min_tv);
        maxtemp = findViewById(R.id.max_tv);
        visibility = findViewById(R.id.visibility);
        lottieAnimationView = findViewById(R.id.lottie);
        loadTV= findViewById(R.id.loadingText);
        relativeLayout=findViewById(R.id.holdingLayout);
        parentLayout= findViewById(R.id.parent_layout);
        SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swipe_refresh);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                makeRequest(urlMetrics,0);
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        getLocation();


    }

    private void getLocation() {
        //location manager
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        //fusedLocationProvider
        FusedLocationProviderClient fusedlocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION},2);
            return;
        }
        fusedlocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location)
            {
             if(location!=null){
                 lat = location.getLatitude();
                 lon = location.getLongitude();
                 System.out.println(lat);
                 System.out.println(lon);
                 urlMetrics = String.format(urlMetrics,lat,lon);
                 urlForAqi = String.format(urlForAqi,lat,lon);
                 System.out.println(urlMetrics);
                 makeRequest(urlMetrics,0);
                 makeRequest(urlForAqi,2);
             }
             else
             {
                 if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                     alertBoxForGPS();
                 }
                 else
                     Toast.makeText(MainActivity.this, "Error in fetching location!", Toast.LENGTH_SHORT).show();
             }
            }
        });

    }
 //AlertBox
    private void alertBoxForGPS()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Alert!").setMessage("It seems your GPS is off.Turn it on?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {

                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));

            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                dialogInterface.cancel();
            }
        });
        builder.create().show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        MenuItem searchMenuItem = menu.findItem(R.id.search);
        searchView = (SearchView) searchMenuItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query){
                urlSearchCity = String.format(urlSearchCity,query);
                makeRequest(urlSearchCity,1);
                urlSearchCity = tempUrl;
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        //Switch case

        return super.onOptionsItemSelected(item);

    }
       public void makeRequest(String url,int flag){

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                extractWeatherData(response,flag);
                loadTV.setVisibility(View.GONE);
                lottieAnimationView.setVisibility(View.GONE);
                relativeLayout.setVisibility(View.VISIBLE);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Toast.makeText(MainActivity.this,"Error in getting response ",Toast.LENGTH_SHORT).show();
            }
        });
        requestQueue.add(jsonObjectRequest);
    }

    @SuppressLint({"SetTextI18n", "UseCompatLoadingForDrawables"})
    private void extractWeatherData(JSONObject response,int flag)
    {
        JSONObject jsonResponse = null;
        try {
            if(flag==2)
            {
                JSONObject jsonAqi = new JSONObject(String.valueOf(response));
                JSONArray jsonArray = jsonAqi.getJSONArray("list");
                JSONObject jsonPrep = jsonArray.getJSONObject(0);
                JSONObject jsonMain = jsonPrep.getJSONObject("main");
                System.out.println(jsonMain.getString("aqi"));
                aqi.setText(jsonMain.getString("aqi"));

            }
            else{

                jsonResponse = new JSONObject(String.valueOf(response));
                JSONArray jsonArray = jsonResponse.getJSONArray("weather");
                JSONObject jsonObjectWeather = jsonArray.getJSONObject(0);
                weatherDescription.setText(jsonObjectWeather.getString("main")+"," +jsonObjectWeather.getString("description"));

                JSONObject jsonObjectSys = jsonResponse.getJSONObject("sys");
                cityName.setText(jsonResponse.getString("name")+","+jsonObjectSys.getString("country"));
                String s = String.valueOf(cityName.getText());
                System.out.println(s);

                JSONObject jsonObjectMain = jsonResponse.getJSONObject("main");
                int temp = jsonObjectMain.getInt("temp");
                temperature.setText(String.valueOf(temp)+ "°" );
                feelslike.setText(jsonObjectMain.getString("feels_like")+"°");
                humidity.setText(jsonObjectMain.getString("humidity")+"%");
                pressure.setText(jsonObjectMain.getString("pressure")+"mB");
                mintemp.setText(jsonObjectMain.getString("temp_min")+"");
                maxtemp.setText(jsonObjectMain.getString("temp_max")+"");

                double visibility1 = response.getDouble("visibility")/1000;

                visibility.setText(String.valueOf(visibility1)+"km");

                JSONObject jasonObjectWind = jsonResponse.getJSONObject("wind");
                windSpeed.setText(jasonObjectWind.getString("speed")+"m/s");


                String currentdateAndTime = new SimpleDateFormat("h:mm a", Locale.getDefault()).format(new Date());
                dateAndTime.setText("Last update: "+currentdateAndTime);

                String iconDecider = jsonObjectWeather.getString("icon");
                String iconUrl = "https://openweathermap.org/img/wn/%s@2x.png";
                iconUrl = String.format(iconUrl,iconDecider);
                Glide.with(this ).load(iconUrl).into(imageview);

                char last_character = iconDecider.charAt(iconDecider.length()-1);

                if(flag == 0) {

                    if (last_character == 'd') {

                        parentLayout.setBackground(getDrawable(R.drawable.day_background));

                    } else if (last_character == 'n') {

                        parentLayout.setBackground(getDrawable(R.drawable.night_background));

                    }
                }

            }


        } catch (JSONException e)
        {
            e.printStackTrace() ;
        }
    }
}