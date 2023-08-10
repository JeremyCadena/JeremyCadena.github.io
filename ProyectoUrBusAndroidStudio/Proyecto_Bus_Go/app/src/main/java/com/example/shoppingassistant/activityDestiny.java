package com.example.shoppingassistant;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.shoppingassistant.controller.FirebaseApiBuss;
import com.example.shoppingassistant.models.BusS;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;


import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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

        FirebaseApiBuss firebaseApiBuss = new FirebaseApiBuss();
        firebaseApiBuss.getAllBuses(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Error al obtener la lista de buses", Toast.LENGTH_SHORT).show();
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
                            // Parsear la respuesta JSON de la lista de buses
                            JSONArray jsonArray = new JSONArray(responseBody);

                            // Coordenadas del origen
                            double originLatitude = Double.parseDouble(getIntent().getExtras().get("originLatitude").toString());
                            double originLongitude = Double.parseDouble(getIntent().getExtras().get("originLongitude").toString());

                            // Variables para guardar la parada más cercana
                            String nearestBusStopLatitude = "";
                            String nearestBusStopLongitude = "";
                            int nearestBusStopNode = 0;
                            double nearestDistance = Double.MAX_VALUE;

                            // Iterar sobre la lista de buses para encontrar la parada más cercana
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject busObject = jsonArray.getJSONObject(i).getJSONObject("data");
                                double busLatitude = busObject.getDouble("BUSS_MAINSTREET");
                                double busLongitude = busObject.getDouble("BUSS_SECONDARYSTREET");

                                // Calcular la distancia entre el origen y la parada actual
                                double distance = calculateDistance(originLatitude, originLongitude, busLatitude, busLongitude);

                                // Actualizar la parada más cercana si se encuentra una más cercana
                                if (distance < nearestDistance) {
                                    nearestDistance = distance;
                                    nearestBusStopLatitude = busObject.getString("BUSS_MAINSTREET");
                                    nearestBusStopLongitude = busObject.getString("BUSS_SECONDARYSTREET");
                                    nearestBusStopNode = busObject.getInt("BUSS_NODE");
                                }
                            }

                            // Coordenadas del destino (sustituye las coordenadas reales del destino)

                            double destinationLatitude = 0.0;
                            double destinationLongitude = 0.0;
                            if(!destinyLongitude.equals("") || !destinyLatitude.equals("")){
                                destinationLatitude = Double.parseDouble(destinyLatitude);
                                destinationLongitude=Double.parseDouble(destinyLongitude);
                            }

                            String nearestBusStopLatitudeDestination = "";
                            String nearestBusStopLongitudeDestination = "";
                            int nearestBusStopNodeDestination = 0;
                            double nearestDistanceDestination = Double.MAX_VALUE;


                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject busObject = jsonArray.getJSONObject(i).getJSONObject("data");
                                double busLatitude = busObject.getDouble("BUSS_MAINSTREET");
                                double busLongitude = busObject.getDouble("BUSS_SECONDARYSTREET");

                                // Calcular la distancia entre el destino y la parada actual
                                double distance = calculateDistance(destinationLatitude, destinationLongitude, busLatitude, busLongitude);

                                // Actualizar la parada más cercana al destino si se encuentra una más cercana
                                if (distance < nearestDistanceDestination) {
                                    nearestDistanceDestination = distance;
                                    nearestBusStopLatitudeDestination = busObject.getString("BUSS_MAINSTREET");
                                    nearestBusStopLongitudeDestination = busObject.getString("BUSS_SECONDARYSTREET");
                                    nearestBusStopNodeDestination = busObject.getInt("BUSS_NODE");
                                }
                            }

                            List<BusS> remainingBusStops = new ArrayList<>();

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject busObject = jsonArray.getJSONObject(i).getJSONObject("data");
                                double busLatitude = busObject.getDouble("BUSS_MAINSTREET");
                                double busLongitude = busObject.getDouble("BUSS_SECONDARYSTREET");
                                int busNode = busObject.getInt("BUSS_NODE");
                                String busSense = busObject.getString("BUSS_SENSE");

                                // Agregar las paradas que cumplan con las condiciones requeridas
                                if (busNode > nearestBusStopNode && busSense.equals("regreso") && busNode < nearestBusStopNodeDestination) {
                                    BusS busStop = new BusS();
                                    busStop.setBussMainStreet(String.valueOf(busLatitude));
                                    busStop.setBussSecondaryStreet(String.valueOf(busLongitude));
                                    busStop.setBussNode(busNode);
                                    remainingBusStops.add(busStop);
                                }
                            }

                            Collections.sort(remainingBusStops, new Comparator<BusS>() {
                                @Override
                                public int compare(BusS busStop1, BusS busStop2) {
                                    return Integer.compare(busStop1.getBussNode(), busStop2.getBussNode());
                                }
                            });


                            for (int i = 0; i < remainingBusStops.size() - 1; i++) {
                                BusS currentBusStop = remainingBusStops.get(i);
                                BusS nextBusStop = remainingBusStops.get(i + 1);

                                double currentLatitude = Double.parseDouble(currentBusStop.getBussMainStreet());
                                double currentLongitude = Double.parseDouble(currentBusStop.getBussSecondaryStreet());
                                double nextLatitude = Double.parseDouble(nextBusStop.getBussMainStreet());
                                double nextLongitude = Double.parseDouble(nextBusStop.getBussSecondaryStreet());

                                updateBusStopOnMap(currentLatitude, currentLongitude, nextLatitude, nextLongitude);
                                if(i==0){
                                    updateBusStopOnMap(currentLatitude, currentLongitude, Double.parseDouble(nearestBusStopLatitude), Double.parseDouble(nearestBusStopLongitude));
                                }
                                if(i==remainingBusStops.size()-2){
                                    updateBusStopOnMap(currentLatitude, currentLongitude, Double.parseDouble(nearestBusStopLatitudeDestination), Double.parseDouble(nearestBusStopLongitudeDestination));
                                }
                            }

                            updateBusStopOnMapBlue(Double.parseDouble(getIntent().getExtras().get("originLatitude").toString()),Double.parseDouble(getIntent().getExtras().get("originLongitude").toString()),Double.parseDouble(nearestBusStopLatitude), Double.parseDouble(nearestBusStopLongitude));
                            updateBusStopOnMapBlue(destinationLatitude,destinationLongitude,Double.parseDouble(nearestBusStopLatitudeDestination), Double.parseDouble(nearestBusStopLongitudeDestination));
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), "Error al analizar la respuesta de la lista de buses", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radio de la Tierra en kilómetros

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }

    private void updateBusStopOnMapBlue(double latitude1,double longitude1, double latitude2, double longitude2) {
        Marker marker = new Marker(map);
        marker.setPosition(new GeoPoint(latitude2, longitude2));
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

        drawRouteBlue(new GeoPoint(latitude1,longitude1), new GeoPoint(latitude2, longitude2));
        marker.setTitle("Parada de Bus");
        map.getOverlays().add(marker);
        marker.showInfoWindow();

        map.invalidate();
    }

    private void updateBusStopOnMap(double latitude1,double longitude1, double latitude2, double longitude2) {
        Marker marker = new Marker(map);
        marker.setPosition(new GeoPoint(latitude2, longitude2));
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

        drawRoute(new GeoPoint(latitude1,longitude1), new GeoPoint(latitude2, longitude2));
        marker.setTitle("Parada de Bus");
        map.getOverlays().add(marker);
        marker.showInfoWindow();

        map.invalidate();
    }
    private void updateLocationOnMap(double latitude, double longitude) {
        map.getOverlays().clear();

        Drawable blueMarkerIcon = getResources().getDrawable(R.drawable.blue_marker_icon);


        Marker originMarker = new Marker(map);
        originMarker.setPosition(new GeoPoint(Double.parseDouble(getIntent().getExtras().get("originLatitude").toString()), Double.parseDouble(getIntent().getExtras().get("originLongitude").toString())));
        originMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        originMarker.setTitle("Origen Seleccionado");
        originMarker.setSnippet(getIntent().getExtras().get("information").toString());
        originMarker.setIcon(blueMarkerIcon);

        Marker marker = new Marker(map);
        marker.setPosition(new GeoPoint(latitude, longitude));
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

        marker.setTitle("Destino Seleccionado");
        marker.setSnippet(txtAddress.getText().toString());
        marker.setIcon(blueMarkerIcon);
        map.getOverlays().add(marker);
        marker.showInfoWindow();

        map.getOverlays().add(originMarker);
        map.invalidate();
    }

    private void drawRouteBlue(GeoPoint origin, GeoPoint destination) {
        String orsUrl = "https://api.openrouteservice.org/v2/directions/foot-walking?api_key=" + api_key_routing +
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
                        routePolyline.setColor(Color.BLUE);
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
