package com.example.shoppingassistant;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

public class Register extends AppCompatActivity {

    private EditText email,username,password;
    private Button btn_sing_up;
    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sing_up);

        mAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);

        email = findViewById(R.id.txt_price_EProduct);
        username = findViewById(R.id.txt_desc_EProduct);
        password = findViewById(R.id.lblPassword_sing);
        btn_sing_up = findViewById(R.id.btn_update_Product);

        btn_sing_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String s_email = email.getText().toString();
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
                progressDialog.setMessage("Registrando usuario en linea...");
                progressDialog.show();

                mAuth.createUserWithEmailAndPassword(s_email,s_password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful())
                                {
                                    Toast.makeText(getApplicationContext(), "Se ha registrado el usuario correctamente", Toast.LENGTH_SHORT).show();
                                    Intent activity_home = new Intent(getApplicationContext(), Home.class);
                                    startActivity(activity_home);
                                }
                                else
                                {
                                    if(task.getException() instanceof FirebaseAuthUserCollisionException)
                                    {
                                        Toast.makeText(getApplicationContext(), "Usuario ya existe!!!", Toast.LENGTH_SHORT).show();

                                    }
                                    Toast.makeText(getApplicationContext(), "No se pudo regisrar el Usuario!!!", Toast.LENGTH_SHORT).show();
                                }
                                progressDialog.dismiss();
                            }
                        });
            }
        });

    }

}
