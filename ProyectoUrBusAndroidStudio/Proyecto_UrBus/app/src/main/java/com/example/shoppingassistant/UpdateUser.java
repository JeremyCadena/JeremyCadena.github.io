package com.example.shoppingassistant;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class UpdateUser extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private EditText etUserName, etUserEmail, etUserPassword, etUserPasswordOld, etUserPasswordNew;
    private Button btnUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_user);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        etUserName = findViewById(R.id.txt_name_user_update);
        etUserEmail = findViewById(R.id.txt_email_User_update);
        etUserEmail.setEnabled(false);
        etUserPassword = findViewById(R.id.txt_password_user_update_new);
        etUserPasswordOld = findViewById(R.id.txt_password_user_update);
        etUserPasswordNew = findViewById(R.id.txt_password_user_update_new_2);
        btnUpdate = findViewById(R.id.btn_update_user);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        CollectionReference usersCollection = db.collection("users");

        if (currentUser != null) {
            String userId = currentUser.getUid();
            getUserData(userId);
        } else {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
        }

        btnUpdate.setOnClickListener(view -> {
            // Obtener los nuevos valores de los campos de edición
            String newUserName = etUserName.getText().toString();
            String newUserEmail = etUserEmail.getText().toString();
            String newUserPassword = etUserPassword.getText().toString();

            Query query = usersCollection.whereEqualTo("userId", currentUser.getUid());
            query.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot document : task.getResult()) {
                        DocumentReference userRef = usersCollection.document(document.getId());
                        // Actualizar los campos individuales en Firestore
                        userRef.update("userName", newUserName, "userEmail", newUserEmail)
                                .addOnSuccessListener(aVoid -> {
                                    // Los campos se actualizaron correctamente en Firestore
                                    Toast.makeText(UpdateUser.this, "Datos de usuario actualizados en Firestore", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    // Ocurrió un error al actualizar los campos en Firestore
                                    Toast.makeText(UpdateUser.this, "Error al actualizar los datos de usuario en Firestore", Toast.LENGTH_SHORT).show();
                                });
                    }
                } else {
                    // Ocurrió un error al obtener los documentos de usuario
                    Toast.makeText(UpdateUser.this, "Error al obtener los datos de usuario de Firestore", Toast.LENGTH_SHORT).show();
                }
            });
            if (!newUserPassword.isEmpty() && etUserPassword.getText().toString().equals(etUserPasswordNew.getText().toString())) {
                // Reautenticar al usuario
                AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), etUserPasswordOld.getText().toString());
                currentUser.reauthenticate(credential)
                        .addOnCompleteListener(reauthTask -> {
                            if (reauthTask.isSuccessful()) {
                                // La reautenticación fue exitosa, cambiar la contraseña
                                currentUser.updatePassword(newUserPassword)
                                        .addOnCompleteListener(passwordTask -> {
                                            if (passwordTask.isSuccessful()) {
                                                // La contraseña se cambió exitosamente en Firebase Authentication
                                                Toast.makeText(UpdateUser.this, "Contraseña cambiada exitosamente en Firebase Authentication", Toast.LENGTH_SHORT).show();

                                                // Actualizar el nombre de usuario en Firebase Authentication
                                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                                        .setDisplayName(newUserName)
                                                        .build();

                                                currentUser.updateProfile(profileUpdates)
                                                        .addOnCompleteListener(profileTask -> {
                                                            if (profileTask.isSuccessful()) {
                                                                // El nombre de usuario se actualizó correctamente en Firebase Authentication
                                                                Toast.makeText(UpdateUser.this, "Nombre de usuario actualizado exitosamente en Firebase Authentication", Toast.LENGTH_SHORT).show();
                                                            } else {
                                                                // Ocurrió un error al actualizar el nombre de usuario en Firebase Authentication
                                                                Toast.makeText(UpdateUser.this, "Error al actualizar el nombre de usuario en Firebase Authentication", Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                            } else {
                                                // Ocurrió un error al cambiar la contraseña en Firebase Authentication
                                                Toast.makeText(UpdateUser.this, "Error al cambiar la contraseña en Firebase Authentication", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            } else {
                                // La reautenticación falló
                                Toast.makeText(UpdateUser.this, "Error al reautenticar el usuario", Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                // No se proporcionó una nueva contraseña, solo actualizar el nombre de usuario en Firebase Authentication
                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                        .setDisplayName(newUserName)
                        .build();

                currentUser.updateProfile(profileUpdates)
                        .addOnCompleteListener(profileTask -> {
                            if (profileTask.isSuccessful()) {
                                // El nombre de usuario se actualizó correctamente en Firebase Authentication
                                Toast.makeText(UpdateUser.this, "Nombre de usuario actualizado exitosamente en Firebase Authentication", Toast.LENGTH_SHORT).show();
                            } else {
                                // Ocurrió un error al actualizar el nombre de usuario en Firebase Authentication
                                Toast.makeText(UpdateUser.this, "Error al actualizar el nombre de usuario en Firebase Authentication", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    private void getUserData(String userId) {
        String paramName = "userId";
        CollectionReference usersCollection = db.collection("users");
        Query query = usersCollection.whereEqualTo(paramName, userId);

        query.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                        String userName = documentSnapshot.getString("userName");
                        String userEmail = documentSnapshot.getString("userEmail");
                        String userPassword = "";

                        etUserName.setText(userName);
                        etUserEmail.setText(userEmail);
                        etUserPassword.setText(userPassword);

                    } else {
                        Toast.makeText(UpdateUser.this, "No se encontró el usuario en la base de datos", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("UpdateUser", "Error al obtener los datos de usuario", e);
                    Toast.makeText(UpdateUser.this, "Error al obtener los datos de usuario", Toast.LENGTH_SHORT).show();
                });
    }
}