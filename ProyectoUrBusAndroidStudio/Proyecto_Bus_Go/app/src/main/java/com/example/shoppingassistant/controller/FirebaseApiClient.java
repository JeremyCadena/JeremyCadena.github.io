package com.example.shoppingassistant.controller;

import com.example.shoppingassistant.models.User;

import okhttp3.*;

import java.io.IOException;

public class FirebaseApiClient {
    private final OkHttpClient client;

    private String ip ="10.40.5.57"; //"10.40.25.198";

    public FirebaseApiClient() {
        this.client = new OkHttpClient();
    }

    public void getUser(String userId, Callback callback) {
        String url = "http://"+ip+":8000/urbus/user/" + userId;

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(callback);
    }

    public void addUser(User user, Callback callback) {
        String url = "http://"+ip+":8000/urbus/user";

        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        String json = "{\"userId\":\"" + user.getUserId() + "\",\"userName\":\"" + user.getUserName() + "\",\"userEmail\":\"" + user.getUserEmail() + "\"}";

        RequestBody requestBody = RequestBody.create(json, JSON);

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(callback);
    }

    public void editUser(String userId, User user, Callback callback) {
        String url = "http://"+ip+":8000/urbus/user/" + userId;

        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        String json = "{\"userName\":\"" + user.getUserName() + "\",\"userEmail\":\"" + user.getUserEmail() + "\"}";

        RequestBody requestBody = RequestBody.create(json, JSON);

        Request request = new Request.Builder()
                .url(url)
                .put(requestBody)
                .build();

        client.newCall(request).enqueue(callback);
    }

    public void deleteUser(String userId, Callback callback) {
        String url = "http://"+ip+":8000/urbus/user/" + userId;

        Request request = new Request.Builder()
                .url(url)
                .delete()
                .build();

        client.newCall(request).enqueue(callback);
    }
}