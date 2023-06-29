package com.example.shoppingassistant;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LogIn extends AppCompatActivity {
    private EditText username, password;
    private Button btnSingUp,btnLogin;
    private ImageButton btnGoogleL;
    private final int RC_SIGN_IN=1;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleClient;
    private String TAG= "GoogleSignInLoginActivity";
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        username = findViewById(R.id.lblUsernameLog);
        password = findViewById(R.id.lblPasswordLog);
        btnLogin = findViewById(R.id.btnLogIn);
        btnSingUp = findViewById(R.id.btnSingUp);
        btnGoogleL =  findViewById(R.id.btnGoogle);
        progressDialog = new ProgressDialog(this);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String s_email = username.getText().toString();
                String s_password = password.getText().toString();
                if(TextUtils.isEmpty(s_email))
                {
                    Toast.makeText(getApplicationContext(), "Debe ingresar un email porfavor!!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(s_password))
                {
                    Toast.makeText(getApplicationContext(), "Debe ingresar una contraseña porfavor!!", Toast.LENGTH_SHORT).show();
                    return;
                }
                progressDialog.setMessage("Iniciando Sesion...");
                progressDialog.show();

                mAuth.signInWithEmailAndPassword(s_email,s_password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful())
                                {
                                    Toast.makeText(getApplicationContext(), "Bienvenido a Shopping Assistent", Toast.LENGTH_SHORT).show();
                                    Intent activity_home = new Intent(getApplicationContext(), Home.class);
                                    startActivity(activity_home);
                                }
                                else
                                {
                                    Toast.makeText(getApplicationContext(), "No se pudo iniciar sesion!!!", Toast.LENGTH_SHORT).show();
                                }
                                progressDialog.dismiss();
                            }
                        });
            }
        });

        btnSingUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Register.class);
                startActivity(intent);
            }
        });


        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleClient = GoogleSignIn.getClient(this,gso);
        mAuth = FirebaseAuth.getInstance();


        btnGoogleL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInWithGoogle();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode == RC_SIGN_IN)
        {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            if(task.isSuccessful())
            {
                try
                {
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    Log.d(TAG, "firebaseAuthWithGoogle: "+ account.getId());
                    firebaseAuthWithGoogle(account.getIdToken());
                }catch (ApiException e)
                {
                    Log.w(TAG, "Google sign in failed ",e);
                }
            }
            else
            {
                Log.d(TAG, "Error, Login no exitoso: "+ task.getException().toString());
                Toast.makeText(LogIn.this, "Ocurrio un error. "+task.getException().toString(),Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken){
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken,null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful())
                        {
                            Log.d(TAG, "signInWithCredential: success");
                            Intent homeActivity = new Intent(LogIn.this,Home.class);
                            startActivity(homeActivity);
                            LogIn.this.finish();
                        }
                        else{
                            Log.w(TAG, "signInWithCredential: failure", task.getException());
                        }
                    }
                });

    }
    private void signInWithGoogle(){
        Intent homeIntent = mGoogleClient.getSignInIntent();
        startActivityForResult(homeIntent,RC_SIGN_IN);
    }

    @Override
    protected void onStart(){
        FirebaseUser user = mAuth.getCurrentUser();
        if(user != null)
        {
            Intent homeActivity = new Intent(getApplicationContext(), Home.class);
            startActivity(homeActivity);
        }
        super.onStart();
    }

}