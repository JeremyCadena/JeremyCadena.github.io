package com.example.shoppingassistant;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.shoppingassistant.controller.FirebaseApiClient;
import com.example.shoppingassistant.models.User;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class UpdateUser extends AppCompatActivity {

    private FirebaseApiClient firebaseApiClient;
    private EditText etUserName, etUserEmail, etUserPassword, etUserPasswordOld, etUserPasswordNew;
    private Button btnUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_user);

        firebaseApiClient = new FirebaseApiClient();
        etUserName = findViewById(R.id.txt_name_user_update);
        etUserEmail = findViewById(R.id.txt_email_User_update);
        etUserEmail.setEnabled(false);
        etUserPassword = findViewById(R.id.txt_password_user_update_new);
        etUserPasswordOld = findViewById(R.id.txt_password_user_update);
        etUserPasswordNew = findViewById(R.id.txt_password_user_update_new_2);
        btnUpdate = findViewById(R.id.btn_update_user);

        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey("id_user")) {
            String userId = extras.get("id_user").toString();

            // Llamar a la API para obtener los datos del usuario
            firebaseApiClient.getUser(userId, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.w("UpdateUser", "Error al realizar la consulta", e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String responseBody = response.body().string();
                        // Procesar la respuesta de la API y obtener los datos del usuario
                        try {
                            JSONObject userJson = new JSONObject(responseBody);
                            String userName = userJson.getString("userName");
                            String userEmail = userJson.getString("userEmail");

                            // Establecer los valores en los EditText correspondientes
                            etUserName.setText(userName);
                            etUserEmail.setText(userEmail);
                        } catch (JSONException e) {
                            Log.e("UpdateUser", "Error al procesar la respuesta JSON", e);
                        }
                    } else {
                        Log.w("UpdateUser", "Error al realizar la consulta. Código de respuesta: " + response.code());
                    }
                }
            });
        } else {
            Toast.makeText(this, "ID de usuario no encontrado", Toast.LENGTH_SHORT).show();
        }

        btnUpdate.setOnClickListener(view -> {
            String newUserName = etUserName.getText().toString();
            String newUserEmail = etUserEmail.getText().toString();
            String newUserPassword = etUserPassword.getText().toString();
            String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            User user = new User(currentUserId, newUserName, newUserEmail);

            firebaseApiClient.editUser(user.getUserId(), user, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> Toast.makeText(UpdateUser.this, "Error al actualizar los datos de usuario", Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        runOnUiThread(() -> Toast.makeText(UpdateUser.this, "Datos de usuario actualizados", Toast.LENGTH_SHORT).show());

                        if (!newUserPassword.isEmpty() && etUserPassword.getText().toString().equals(etUserPasswordNew.getText().toString())) {
                            // Actualizar contraseña si se proporcionó una nueva
                            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                            AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), etUserPasswordOld.getText().toString());
                            currentUser.reauthenticate(credential)
                                    .addOnCompleteListener(reauthTask -> {
                                        if (reauthTask.isSuccessful()) {
                                            currentUser.updatePassword(newUserPassword)
                                                    .addOnCompleteListener(passwordTask -> {
                                                        if (passwordTask.isSuccessful()) {
                                                            runOnUiThread(() -> Toast.makeText(UpdateUser.this, "Contraseña cambiada exitosamente", Toast.LENGTH_SHORT).show());
                                                            // Actualizar nombre de usuario en Firebase Authentication
                                                            updateFirebaseUserName(newUserName);
                                                        } else {
                                                            runOnUiThread(() -> Toast.makeText(UpdateUser.this, "Error al cambiar la contraseña", Toast.LENGTH_SHORT).show());
                                                        }
                                                    });
                                        } else {
                                            runOnUiThread(() -> Toast.makeText(UpdateUser.this, "Error al reautenticar el usuario", Toast.LENGTH_SHORT).show());
                                        }
                                    });
                        } else {
                            runOnUiThread(() -> {
                                Toast.makeText(UpdateUser.this, "Nombre de usuario actualizado", Toast.LENGTH_SHORT).show();
                                // Actualizar nombre de usuario en Firebase Authentication
                                updateFirebaseUserName(newUserName);
                            });
                        }
                    } else {
                        runOnUiThread(() -> Toast.makeText(UpdateUser.this, "Error al actualizar los datos de usuario", Toast.LENGTH_SHORT).show());
                    }
                }
            });
        });


    }
    private void updateFirebaseUserName(String newUserName) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(newUserName)
                .build();

        currentUser.updateProfile(profileUpdates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        runOnUiThread(() -> Toast.makeText(UpdateUser.this, "Nombre de usuario actualizado exitosamente en Firebase Authentication", Toast.LENGTH_SHORT).show());
                        Intent activityHome = new Intent(getApplicationContext(), Home.class);
                        startActivity(activityHome);
                    } else {
                        runOnUiThread(() -> Toast.makeText(UpdateUser.this, "Error al actualizar el nombre de usuario en Firebase Authentication", Toast.LENGTH_SHORT).show());
                    }
                });
    }
}