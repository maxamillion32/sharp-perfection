package com.sergeyloginov.sharpperfection.auth;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.sergeyloginov.sharpperfection.R;
import com.sergeyloginov.sharpperfection.controller.MainActivity;

public class LoginActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 10;
    private FirebaseAuth auth;
    private EditText etEmail;
    private EditText etPassword;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == REQUEST_CODE) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();

        etEmail = (EditText) findViewById(R.id.et_email);
        etPassword = (EditText) findViewById(R.id.et_password);

        TextView tvSignUp = (TextView) findViewById(R.id.tv_sign_up);
        tvSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivityForResult(intent, REQUEST_CODE);
            }
        });

        Button btnSignIn = (Button) findViewById(R.id.btn_sign_in);
        assert btnSignIn != null;
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = etEmail.getText().toString();
                final String password = etPassword.getText().toString();

                if (TextUtils.isEmpty(etEmail.getText().toString())) {
                    Toast.makeText(LoginActivity.this,
                            R.string.enter_email,
                            Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(etPassword.getText().toString())) {
                    Toast.makeText(LoginActivity.this,
                            R.string.enter_password,
                            Toast.LENGTH_SHORT).show();
                } else {
                    auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(
                                    LoginActivity.this,
                                    new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (!task.isSuccessful()) {
                                if (password.length() < 6) {
                                    Toast.makeText(LoginActivity.this,
                                            R.string.password_too_short,
                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(LoginActivity.this,
                                            R.string.check_email,
                                            Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        }
                    });
                }
            }
        });
    }
}
