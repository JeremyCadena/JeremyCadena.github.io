package com.example.shoppingassistant;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class StartRoute extends AppCompatActivity implements LocationListener {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private MapView map;
    private MapController mapController;
    private Button btnStartRoute;

    private LocationManager locationManager;
    private boolean isGPSEnabled;
    private boolean isNetworkEnabled;
    private boolean canGetLocation;
    private GeoPoint destinationFinal;
    private Marker trackingMarker;

    private String api_key_routing= "5b3ce3597851110001cf62482a3ee292e8e34765b386ab628215d214";

    private static final long MIN_TIME_BETWEEN_UPDATES = 5000; // Intervalo mínimo entre actualizaciones de ubicación en milisegundos
    private static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // Distancia mínima para la actualización de ubicación en metros

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_route);
        Configuration.getInstance().load(getApplicationContext(), PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));

        map = findViewById(R.id.mapStartRoute);
        btnStartRoute = findViewById(R.id.btnStartRoute);

        map.setBuiltInZoomControls(true);
        mapController = (MapController) map.getController();
        GeoPoint cuenca = new GeoPoint(-2.90055, -79.00453);
        mapController.setCenter(cuenca);
        mapController.setZoom(10);
        map.setMultiTouchControls(true);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        btnStartRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestLocationPermission();
            }
        });
    }

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getLocation();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation();
            } else {
                Toast.makeText(this, "Se requieren permisos de ubicación para mostrar la ubicación actual", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getLocation() {
        try {
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                Toast.makeText(this, "No se pudo obtener la ubicación actual. Asegúrate de tener activado el GPS o la red.", Toast.LENGTH_SHORT).show();
            } else {
                canGetLocation = true;
                if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BETWEEN_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    if (locationManager != null) {
                        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();

                            GeoPoint currentLocation = new GeoPoint(latitude, longitude);
                            mapController.setCenter(currentLocation);

                            GeoPoint destination = new GeoPoint(Double.parseDouble(getIntent().getExtras().get("destinyLatitude").toString()), Double.parseDouble(getIntent().getExtras().get("destinyLongitude").toString()));

                            destinationFinal = destination;

                            addTrackingMarker(currentLocation,destination);
                            addMarker(destination, "Destino");

                            drawRoute(currentLocation, destination);
                        }
                    }
                }
                if (isGPSEnabled) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BETWEEN_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    if (locationManager != null) {
                        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (location != null) {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();

                            GeoPoint currentLocation = new GeoPoint(latitude, longitude);
                            mapController.setCenter(currentLocation);

                            GeoPoint destination = new GeoPoint(Double.parseDouble(getIntent().getExtras().get("destinyLatitude").toString()), Double.parseDouble(getIntent().getExtras().get("destinyLongitude").toString()));

                            destinationFinal = destination;

                            addMarker(destination, "Destino");
                            addTrackingMarker(currentLocation,destination);

                            drawRoute(currentLocation, destination);
                        }
                    }
                }
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void addMarker(GeoPoint geoPoint, String title) {
        Marker marker = new Marker(map);
        marker.setPosition(geoPoint);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setTitle(title);
        map.getOverlays().add(marker);
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

    private void addTrackingMarker(GeoPoint position, GeoPoint destination) {
        if (trackingMarker != null) {
            map.getOverlays().remove(trackingMarker);
            trackingMarker = new Marker(map);
            trackingMarker.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_tracking_marker));
            map.getOverlays().add(trackingMarker);
        }
        if (trackingMarker == null) {
            trackingMarker = new Marker(map);
            trackingMarker.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_tracking_marker));
            map.getOverlays().add(trackingMarker);
        }
        trackingMarker.setPosition(position);

        drawRoute(position,destination);
        map.invalidate();
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        map.getOverlays().clear();
        GeoPoint currentLocation = new GeoPoint(latitude, longitude);
        mapController.setCenter(currentLocation);
        addTrackingMarker(currentLocation,destinationFinal);
        addMarker(destinationFinal, "Destino Seleccionado");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
    }
}
