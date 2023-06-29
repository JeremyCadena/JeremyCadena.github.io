package com.example.shoppingassistant;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
    private Button btnSelectDestiny;
    private EditText txtAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_destiny);
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));

        map = (MapView) findViewById(R.id.mapDestiny);
        btnSelectDestiny = (Button) findViewById(R.id.btnLocationDestiny);
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
                                double longitude = firstResult.getDouble("lon");
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


        Marker marker = new Marker(map);
        marker.setPosition(new GeoPoint(latitude, longitude));
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

        drawRoute(new GeoPoint(Double.parseDouble(getIntent().getExtras().get("originLatitude").toString()), Double.parseDouble(getIntent().getExtras().get("originLongitude").toString())), new GeoPoint(latitude, longitude));

        map.getOverlays().add(marker);
        map.getOverlays().add(originMarker);
        map.invalidate();
    }

    private void drawRoute(GeoPoint origin, GeoPoint destination) {
        String osrmUrl = "http://your-osrm-server/route/v1/driving/" +
                origin.getLongitude() + "," + origin.getLatitude() + ";" +
                destination.getLongitude() + "," + destination.getLatitude() + "?steps=true";

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(osrmUrl)
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

    public List<GeoPoint> parseRouteCoordinatesFromJson(String json) {
        List<GeoPoint> coordinates = new ArrayList<>();

        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray paths = jsonObject.getJSONArray("paths");
            JSONObject path = paths.getJSONObject(0);
            JSONArray points = path.getJSONArray("points");

            for (int i = 0; i < points.length(); i++) {
                JSONArray point = points.getJSONArray(i);
                double latitude = point.getDouble(0);
                double longitude = point.getDouble(1);
                GeoPoint coordinate = new GeoPoint(latitude, longitude);
                coordinates.add(coordinate);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return coordinates;
    }
}