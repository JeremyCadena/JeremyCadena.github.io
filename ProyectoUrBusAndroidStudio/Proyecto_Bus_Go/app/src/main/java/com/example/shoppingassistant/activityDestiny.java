package com.example.shoppingassistant;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;


import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class activityDestiny extends AppCompatActivity {

    private MapView map;
    private MapController mapController;
    private Button btnSelectDestiny, btnSendDestiny;
    private EditText txtAddress;

    private String destinyLatitude ="", destinyLongitude= "";

    private String api_key_routing= "5b3ce3597851110001cf62482a3ee292e8e34765b386ab628215d214";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_destiny);
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));

        map = (MapView) findViewById(R.id.mapDestiny);
        btnSelectDestiny = (Button) findViewById(R.id.btnLocationDestiny);
        btnSendDestiny = (Button) findViewById(R.id.btnSend);
        txtAddress = (EditText) findViewById(R.id.txtLatitudeDestiny);

        map.setBuiltInZoomControls(true);
        mapController = (MapController) map.getController();
        GeoPoint cuenca = new GeoPoint(-2.90055,-79.00453);
        mapController.setCenter(cuenca);
        mapController.setZoom(10);
        map.setMultiTouchControls(true);

        btnSelectDestiny.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                geocodeAddress(txtAddress.getText().toString());
            }
        });

        btnSendDestiny.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent activityStartRoute = new Intent(getApplicationContext(), StartRoute.class);
                activityStartRoute.putExtra("destinyLatitude", destinyLatitude);
                activityStartRoute.putExtra("destinyLongitude", destinyLongitude);
                startActivity(activityStartRoute);
            }
        });
    }

    private void geocodeAddress(String sAddress) {
        String streetMain = sAddress;
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
                                destinyLatitude =String.valueOf(latitude);
                                double longitude = firstResult.getDouble("lon");
                                destinyLongitude = String.valueOf(longitude);
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
        map.getOverlays().clear();
        Marker originMarker = new Marker(map);
        originMarker.setPosition(new GeoPoint(Double.parseDouble(getIntent().getExtras().get("originLatitude").toString()), Double.parseDouble(getIntent().getExtras().get("originLongitude").toString())));
        originMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        originMarker.setTitle("Origen Seleccionado");
        originMarker.setSnippet(getIntent().getExtras().get("information").toString());


        Marker marker = new Marker(map);
        marker.setPosition(new GeoPoint(latitude, longitude));
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

        drawRoute(new GeoPoint(Double.parseDouble(getIntent().getExtras().get("originLatitude").toString()), Double.parseDouble(getIntent().getExtras().get("originLongitude").toString())), new GeoPoint(latitude, longitude));

        marker.setTitle("Destino Seleccionado");
        marker.setSnippet(txtAddress.getText().toString());
        map.getOverlays().add(marker);
        marker.showInfoWindow();

        map.getOverlays().add(originMarker);
        map.invalidate();
    }


    private void drawRoute(GeoPoint origin, GeoPoint destination) {
        String orsUrl = "https://api.openrouteservice.org/v2/directions/driving-car?api_key=" + api_key_routing +
                "&start=" + origin.getLongitude() + "," + origin.getLatitude() +
                "&end=" + destination.getLongitude() + "," + destination.getLatitude();

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(orsUrl)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Manejar el error de la solicitud
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();

                    // Analizar la respuesta JSON y extraer las coordenadas de la ruta
                    List<GeoPoint> waypoints = parseRouteCoordinatesFromJson(responseBody);

                    // Dibujar la ruta en el mapa
                    runOnUiThread(() -> {
                        Polyline routePolyline = new Polyline(map);
                        routePolyline.setPoints(waypoints);
                        routePolyline.setColor(Color.RED);
                        routePolyline.setWidth(5);

                        map.getOverlayManager().add(routePolyline);
                        map.invalidate();
                    });
                } else {
                    // Manejar el error de la respuesta
                }
            }
        });
    }

    private List<GeoPoint> parseRouteCoordinatesFromJson(String json) {
        List<GeoPoint> coordinates = new ArrayList<>();

        try {
            JSONObject jsonObject = new JSONObject(json);
            Log.d("TAG", "JSON completo: " + json);

            JSONArray features = jsonObject.getJSONArray("features");
            if (features.length() > 0) {
                JSONObject feature = features.getJSONObject(0);
                JSONObject geometry = feature.getJSONObject("geometry");
                JSONArray coordinatesArray = geometry.getJSONArray("coordinates");

                for (int i = 0; i < coordinatesArray.length(); i++) {
                    JSONArray coordinate = coordinatesArray.getJSONArray(i);
                    double longitude = coordinate.getDouble(0);
                    double latitude = coordinate.getDouble(1);
                    GeoPoint geoPoint = new GeoPoint(latitude, longitude);
                    coordinates.add(geoPoint);
                    Log.d("TAG", "Coordenada: " + latitude + ", " + longitude);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("TAG", "Error al analizar la respuesta JSON: " + e.getMessage());
        }

        return coordinates;
    }


}