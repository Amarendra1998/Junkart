package com.example.admin.junkart;

import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class AuthActivity extends AppCompatActivity {
private TextInputLayout mphone,mverification;
private ProgressBar phoneprogress,codeprogress;
private Button button;
private TextView merror;
    private FirebaseAuth mAuth;
    private String mVerificationId;
    private int btntype = 0;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mcallbacks;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        mAuth = FirebaseAuth.getInstance();
        merror = (TextView)findViewById(R.id.textView2);
        mphone = (TextInputLayout)findViewById(R.id.text);
        mverification = (TextInputLayout)findViewById(R.id.text2);
        phoneprogress  = (ProgressBar)findViewById(R.id.progressBar);
        codeprogress = (ProgressBar)findViewById(R.id.progressBar2);
        button = (Button)findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                if (btntype==0){
                mphone.setVisibility(View.VISIBLE);
                mphone.setEnabled(false);
                button.setEnabled(false);
                String phonenumber = Objects.requireNonNull(mphone.getEditText()).getText().toString();
                PhoneAuthProvider.getInstance().verifyPhoneNumber(
                        phonenumber,
                        60,
                        TimeUnit.SECONDS,
                        AuthActivity.this,
                        mcallbacks
                );
           }else{
             button.setEnabled(false);
             codeprogress.setVisibility(View.VISIBLE);
             String verificationcode = mverification.getEditText().getText().toString();
             PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId,verificationcode);
             signInWithPhoneAuthCredential(credential);
                }
            }
        });
        mcallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
               merror.setText("There occurred some error in verification");
               merror.setVisibility(View.VISIBLE);
            }
            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
               // Log.d(TAG, "onCodeSent:" + verificationId);

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;
                btntype = 1;
                phoneprogress.setVisibility(View.INVISIBLE);
                codeprogress.setVisibility(View.VISIBLE);
                mverification.setVisibility(View.VISIBLE);
                button.setText("Verify code");
                button.setEnabled(true);

                // ...
            }

        };
    }
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Intent mainintent = new Intent(AuthActivity.this,MainActivity.class);
                            startActivity(mainintent);
                            finish();
                            FirebaseUser user = task.getResult().getUser();
                            // ...
                        } else {
                            // Sign in failed, display a message and update the UI
                            //Log.w(TAG, "signInWithCredential:failure", task.getException());
                            merror.setText("There occurred some error here");
                            merror.setVisibility(View.VISIBLE);
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                            }
                        }
                    }
                });
    }
}
