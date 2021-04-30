package com.xuabux.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.PatternMatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private EditText usuario , contraseña,contraseña2,email;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        email = findViewById(R.id.Email);
        usuario = findViewById(R.id.Usuario);
        contraseña = findViewById(R.id.Contraseña);
        contraseña2 = findViewById(R.id.Contraseña2);

    }


    public void onClick(View v){
        switch (v.getId()) {
            case R.id.BotonRegistro:
             registrar();
            case R.id.ButtonMap:
                Intent mapaI = new Intent(this  , MAPA.class);
                startActivity(mapaI);
        }

    }
    void registrar() {
        String NUsuario = usuario.getText().toString().trim();
        String Contraseña = contraseña.getText().toString().trim();
        String Contraseña2 = contraseña2.getText().toString().trim();
        String Email = email.getText().toString().trim();
        if (Email.isEmpty()){
            email.setError("No hay Email");
            email.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(Email).matches()){
            email.setError("Ingrese un email valido");
            email.requestFocus();
            return;
        }
        if (NUsuario.isEmpty()){
            usuario.setError("No hay usuario");
            usuario.requestFocus();
            return;
        }
        if (Contraseña.isEmpty()){
            contraseña.setError("No hay contraseña");
            contraseña.requestFocus();
            return;
        }
        if (Contraseña.length()<6){
            contraseña.setError("La contraseña es muy corta");
            contraseña.requestFocus();
            return;
        }
        if (Contraseña2.isEmpty()){
            contraseña2.setError("No hay contraseña");
            contraseña2.requestFocus();
            return;
        }
        if (!Contraseña2.equals(Contraseña)){
            contraseña2.setError("Las contraseñas no coinciden");
            contraseña2.requestFocus();
            return;
        }
        mAuth.createUserWithEmailAndPassword(Email,Contraseña)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            Usuario usuario = new Usuario(NUsuario,Email);
                            FirebaseDatabase.getInstance().getReference("Usuarios")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(NUsuario).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        Toast.makeText(MainActivity.this,"Registrado Correctamente",Toast.LENGTH_LONG);

                                    }
                                    else {
                                        Toast.makeText(MainActivity.this,"Error de registro",Toast.LENGTH_LONG);
                                    }
                                }


                         });

                        }
                        else {
                            Toast.makeText(MainActivity.this,"Error de registro",Toast.LENGTH_LONG);
                        }
                    }
                });
        }


}


