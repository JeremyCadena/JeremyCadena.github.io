package com.example.shoppingassistant.controller;

import okhttp3.*;

public class FirebaseApiBuss {
    private final OkHttpClient client;

    private String ip = "10.40.5.57"; //"10.40.25.198";

    public FirebaseApiBuss() {
        this.client = new OkHttpClient();
    }

    public void getAllBuses(Callback callback) {
        String url = "http://" + ip + ":8001/urbus/buss";

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(callback);
    }
}