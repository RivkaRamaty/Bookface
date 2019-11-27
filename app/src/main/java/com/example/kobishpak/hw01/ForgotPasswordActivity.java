package com.example.kobishpak.hw01;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText m_EmailEditText;
    private Button m_SubmitButton;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        m_EmailEditText = findViewById(R.id.emailAddress);
        m_SubmitButton = findViewById(R.id.buttonSubmit);

        m_SubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!((m_EmailEditText.getText().toString().isEmpty()))){
                    resetPassword();
                }
                else
                {
                    Toast.makeText(ForgotPasswordActivity.this, "Please enter Email Address.", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void resetPassword() {
        if (!(m_EmailEditText.getText().toString().isEmpty())) {
            mAuth.sendPasswordResetEmail(m_EmailEditText.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(ForgotPasswordActivity.this, R.string.password_reset_succeded, Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        Toast.makeText(ForgotPasswordActivity.this, R.string.email_not_found, Toast.LENGTH_LONG).show();
                    }
                }
            });
        }else {
            Toast.makeText(ForgotPasswordActivity.this, R.string.please_enter_email, Toast.LENGTH_LONG).show();
        }
    }
}
