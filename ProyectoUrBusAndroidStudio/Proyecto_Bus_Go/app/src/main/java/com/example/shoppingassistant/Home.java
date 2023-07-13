package com.example.shoppingassistant;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.example.shoppingassistant.controller.FirebaseApiClient;
import com.example.shoppingassistant.models.User;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class Home extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        DrawerLayout.DrawerListener {

    private FirebaseAuth mAuth;
    private TextView txtId, txtName, txtEmail;
    private ImageView imgUser;
    private DrawerLayout drawerLayout;
    private Button btnLogOut, btnDeleteCta, btn_add_product, btn_list_products, btn_gallery;
    private GoogleSignInClient mGoogleSingInClient;
    private GoogleSignInOptions gso;

    private FirebaseApiClient firebaseApiClient;

    private User userF = new User();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        txtId = findViewById(R.id.lblId);
        txtName = findViewById(R.id.lblName);
        txtEmail = findViewById(R.id.lblEmailLI);
        imgUser = findViewById(R.id.imgUser);


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);

        drawerLayout.addDrawerListener(this);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference usersRef = db.collection("users");
        userF = new User(currentUser.getUid(), currentUser.getDisplayName(), currentUser.getEmail());


        if (mAuth.getCurrentUser() != null) {
            txtId.setText(currentUser.getUid());
            txtName.setText(currentUser.getDisplayName());
            txtEmail.setText(currentUser.getEmail());

            Glide.with(this).load(currentUser.getPhotoUrl()).into(imgUser);
        }

        if (txtName.getText().toString().equals("")) {
            int position = currentUser.getEmail().indexOf("@");
            String user = currentUser.getEmail().substring(0, position);
            userF.setUserName(user);
            txtName.setText(user);
        }

        // Creamos una instancia de FirebaseApiClient
        firebaseApiClient = new FirebaseApiClient();

        String userId = currentUser.getUid();
        firebaseApiClient.getUser(userId, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.w("Home", "Error al realizar la consulta", e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    // La solicitud GET se realizó con éxito
                    String responseBody = response.body().string();

                    if (responseBody.equals("User exists")) {
                        // El usuario ya existe en la base de datos
                        Log.d("Home", "El usuario ya existe en la base de datos");
                    }
                } else if (response.code() == 404) {
                    // El recurso no existe, podemos agregarlo
                    User user = new User(currentUser.getUid(), txtName.getText().toString(), currentUser.getEmail());

                    firebaseApiClient.addUser(user, new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            // Manejar la falla de la solicitud
                            Log.w("Home", "Error al agregar el usuario", e);
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            if (response.isSuccessful()) {
                                // La solicitud POST se realizó con éxito
                                Log.d("Home", "Usuario agregado con ID: " + response.body().string());
                            } else {
                                // La solicitud POST falló
                                Log.w("Home", "Error al agregar el usuario. Código de respuesta: " + response.code());
                            }
                        }
                    });
                } else {
                    // La solicitud GET falló
                    Log.w("Home", "Error al realizar la consulta. Código de respuesta: " + response.code());
                }
            }
        });

        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSingInClient = GoogleSignIn.getClient(this, gso);


    }

    private void signOut() {
        FirebaseAuth.getInstance().signOut();
        mGoogleSingInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Intent mainLoginActivity = new Intent(getApplicationContext(), LogIn.class);
                mainLoginActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(mainLoginActivity);
                Home.this.finish();
            }
        });
    }


    @Override
    public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

    }

    @Override
    public void onDrawerOpened(@NonNull View drawerView) {

    }

    @Override
    public void onDrawerClosed(@NonNull View drawerView) {

    }

    @Override
    public void onDrawerStateChanged(int newState) {

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Intent intent = null;
        if (item.getItemId() == R.id.nav_deleteAccount) {
        /*    drawerLayout.closeDrawer(GravityCompat.START);
            final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
                if (signInAccount != null) {
                    AuthCredential credential = GoogleAuthProvider.getCredential(signInAccount.getIdToken(), null);
                    user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                deleteCurrentUser(user);
                            } else {
                                Toast.makeText(getApplicationContext(), "Error al reautenticar el usuario", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    deleteCurrentUser(user);
                }
            } else {
                Toast.makeText(getApplicationContext(), "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            }
            return true;

         */
            mAuth = FirebaseAuth.getInstance();
            deleteCurrentUser(userF, mAuth.getCurrentUser());
            mAuth.signOut();
            Intent loginActivity = new Intent(getApplicationContext(), LogIn.class);
            startActivity(loginActivity);
            Home.this.finish();
        }

        if (item.getItemId() == R.id.nav_edit_user) {
            drawerLayout.closeDrawer(GravityCompat.START);
            FirebaseUser currentUser = mAuth.getCurrentUser();
            Intent edit_activity = new Intent(getApplicationContext(), UpdateUser.class);
            edit_activity.putExtra("id_user", currentUser.getUid());
            startActivity(edit_activity);
            Home.this.finish();
        }

        if (item.getItemId() == R.id.nav_logOut) {
            drawerLayout.closeDrawer(GravityCompat.START);
            mAuth.signOut();
            Intent loginActivity = new Intent(getApplicationContext(), LogIn.class);
            startActivity(loginActivity);
            Home.this.finish();
            return true;
        }

        if (item.getItemId() == R.id.nav_insert_origin) {
            drawerLayout.closeDrawer(GravityCompat.START);
            Intent activity_map = new Intent(getApplicationContext(), OriginDestinyActivity.class);
            startActivity(activity_map);
            Home.this.finish();
            return true;
        }

        return false;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void deleteCurrentUser(User user, FirebaseUser currentUser) {

        firebaseApiClient.deleteUser(user.getUserId(), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Manejar la falla de la solicitud
                Log.w("Home", "Error al eliminar el usuario", e);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Error al eliminar el usuario de la base de datos", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    // La solicitud DELETE se realizó con éxito
                    Log.d("Home", "Usuario eliminado de la base de datos");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Usuario eliminado de la base de datos", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    // La solicitud DELETE falló
                    Log.w("Home", "Error al eliminar el usuario. Código de respuesta: " + response.code());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Error al eliminar el usuario de la base de datos", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

        if (currentUser != null) {
            currentUser.delete()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // La cuenta se eliminó exitosamente
                            Toast.makeText(getApplicationContext(), "Cuenta eliminada exitosamente", Toast.LENGTH_SHORT).show();
                        } else {
                            // Ocurrió un error al eliminar la cuenta
                            Toast.makeText(getApplicationContext(), "Error al eliminar la cuenta", Toast.LENGTH_SHORT).show();
                        }
                    });
        }


    }
}