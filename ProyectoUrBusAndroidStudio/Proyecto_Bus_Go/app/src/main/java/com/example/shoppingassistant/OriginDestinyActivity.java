package com.example.shoppingassistant;

import androidx.appcompat.app.AppCompatActivity;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.config.Configuration;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.infowindow.InfoWindow;

import java.io.IOException;
import java.net.URLEncoder;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class OriginDestinyActivity extends AppCompatActivity {
    private MapView map;
    private MapController mapController;
    private EditText txtAddress;
    private Button btnLocation, btnSend;

    private String sLatitude, sLongitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_origin_destiny);
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));


        map = (MapView) findViewById(R.id.map);
        txtAddress = (EditText) findViewById(R.id.txtLatitud);
        btnLocation = (Button) findViewById(R.id.btnLocation);
        btnSend = (Button) findViewById(R.id.btnSend);

        map.setBuiltInZoomControls(true);
        mapController = (MapController) map.getController();
        GeoPoint cuenca = new GeoPoint(-2.90055,-79.00453);
        mapController.setCenter(cuenca);
        mapController.setZoom(10);
        map.setMultiTouchControls(true);

        /*MapEventsReceiver mapEventsReceiver = new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                Toast.makeText(getApplicationContext(),
                        "Latitud: " + p.getLatitude() +
                        " "+
                        "Longitud: "+ p.getLongitude(),Toast.LENGTH_SHORT).show();

                Marker marker = new Marker(map);
                marker.setPosition(p);
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                map.getOverlays().add(marker);
                return false;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                return false;
            }
        };

        MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(this, mapEventsReceiver);
        map.getOverlays().add(mapEventsOverlay);
        */
        btnLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                geocodeAddress();
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent activityDestiny = new Intent(getApplicationContext(), activityDestiny.class);
                activityDestiny.putExtra("originLatitude",sLatitude);
                activityDestiny.putExtra("originLongitude",sLongitude);
                activityDestiny.putExtra("information", txtAddress.getText().toString());
                startActivity(activityDestiny);
            }
        });

    }

    private void geocodeAddress() {
        String streetMain = txtAddress.getText().toString();
        String address = streetMain + ", Cuenca, Ecuador";
        String nominatimUrl;
        try {
            nominatimUrl = "https://nominatim.openstreetmap.org/search?format=json&q=" +
                    URLEncoder.encode(address, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(nominatimUrl).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseBody = response.body().string();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONArray jsonArray = new JSONArray(responseBody);
                            if (jsonArray.length() > 0) {
                                JSONObject firstResult = jsonArray.getJSONObject(0);
                                double latitude = firstResult.getDouble("lat");
                                double longitude = firstResult.getDouble("lon");
                                sLatitude = String.valueOf(latitude);
                                sLongitude = String.valueOf(longitude);
                                mapController.setCenter(new GeoPoint(latitude, longitude));
                                updateLocationOnMap(latitude, longitude);
                            } else {
                                Toast.makeText(getApplicationContext(), "No se encontraron resultados", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), "Error al analizar la respuesta", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    private void updateLocationOnMap(double latitude, double longitude) {
        IGeoPoint mapCenter = map.getMapCenter();
        GeoPoint geoPoint = new GeoPoint(latitude, longitude);
        map.getOverlays().clear();
        Marker marker = new Marker(map);
        marker.setPosition(geoPoint);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setTitle("Origen Seleccionado");
        marker.setSnippet(txtAddress.getText().toString());
        map.getOverlays().add(marker);

        marker.showInfoWindow();

        map.invalidate();
    }

}

