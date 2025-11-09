package com.example.lab6_20220270.ui.auth;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.lab6_20220270.MainActivity;
import com.example.lab6_20220270.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;
    private FirebaseAuth auth;
    private GoogleSignInClient googleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        auth = FirebaseAuth.getInstance();
        ImageView logo = findViewById(R.id.logoImage);
        EditText email = findViewById(R.id.editTextEmail);
        EditText password = findViewById(R.id.editTextPassword);
        Button btnLogin = findViewById(R.id.buttonLogin);
        Button btnRegister = findViewById(R.id.buttonRegister);
        Button btnGoogle = findViewById(R.id.buttonGoogle);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);
        btnLogin.setOnClickListener(v -> {
            String e = email.getText().toString().trim();
            String p = password.getText().toString().trim();
            if(e.isEmpty() || p.isEmpty()){
                Toast.makeText(this, "Complete email y contraseña", Toast.LENGTH_SHORT).show();
                return;
            }
            auth.signInWithEmailAndPassword(e,p).addOnCompleteListener(this, task -> {
                if(task.isSuccessful()){
                    navigateToMain();
                } else {
                    Toast.makeText(this, "Error al iniciar sesión", Toast.LENGTH_SHORT).show();
                }
            });
        });
        btnRegister.setOnClickListener(v -> {
            String e = email.getText().toString().trim();
            String p = password.getText().toString().trim();
            if(e.isEmpty() || p.isEmpty()){
                Toast.makeText(this, "Complete email y contraseña", Toast.LENGTH_SHORT).show();
                return;
            }
            auth.createUserWithEmailAndPassword(e,p).addOnCompleteListener(this, task -> {
                if(task.isSuccessful()){
                    navigateToMain();
                } else {
                    Toast.makeText(this, "Error al registrar usuario", Toast.LENGTH_SHORT).show();
                }
            });
        });
        btnGoogle.setOnClickListener(v -> startGoogleSignIn());
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = auth.getCurrentUser();
        if(currentUser != null){
            navigateToMain();
        }
    }

    private void startGoogleSignIn(){
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_SIGN_IN){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try{
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if(account != null){
                    AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                    auth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<com.google.firebase.auth.AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<com.google.firebase.auth.AuthResult> task) {
                            if(task.isSuccessful()){
                                navigateToMain();
                            } else {
                                Toast.makeText(LoginActivity.this, "Error Google Sign-In", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            } catch (ApiException e){
                Log.w("LoginActivity","Google sign in failed", e);
                Toast.makeText(this, "Error Google Sign-In", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void navigateToMain(){
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        finish();
    }

}
