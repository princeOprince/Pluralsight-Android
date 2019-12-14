package courses.pluralsight.com.tabianconsulting;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    private static final String DOMAIN_NAME = "gmail.com";

    //widgets
    private EditText mEmail, mPassword, mConfirmPassword;
    private Button mRegister;
    private ProgressBar mProgressBar;
    public FirebaseAuth mAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mEmail = (EditText) findViewById(R.id.input_email);
        mPassword = (EditText) findViewById(R.id.input_password);
        mConfirmPassword = (EditText) findViewById(R.id.input_confirm_password);
        mRegister = (Button) findViewById(R.id.btn_register);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mAuth = FirebaseAuth.getInstance();

        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: attempting to register.");

                //check for null valued EditText fields
                if(!isEmpty(mEmail.getText().toString())
                        && !isEmpty(mPassword.getText().toString())
                        && !isEmpty(mConfirmPassword.getText().toString())){

                    //check if user has a company email address
                    if(isValidDomain(mEmail.getText().toString())){

                        //check if passwords match
                        if(doStringsMatch(mPassword.getText().toString(), mConfirmPassword.getText().toString())){
                            registerNewEmail(mEmail.getText().toString(), mPassword.getText().toString());
                        }else{
                            Toast.makeText(RegisterActivity.this, "Passwords do not Match", Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Toast.makeText(RegisterActivity.this, "Please Register with Company Email", Toast.LENGTH_SHORT).show();
                    }

                }else{
                    Toast.makeText(RegisterActivity.this, "You must fill out all the fields", Toast.LENGTH_SHORT).show();
                }
            }
        });

        hideSoftKeyboard();

    }

    private void registerNewEmail(String email, String password) {
        showDialog();
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "onComplete: " + task.isSuccessful());

                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete - AuthState: " + mAuth.getCurrentUser().getUid());
                            sendVerificationEmail();
                            mAuth.signOut();
                        }
                        else {
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(RegisterActivity.this, "Unable to register: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                            mPassword.setError("Unable to register");
                            mConfirmPassword.setError("Unable to register");
                            final TextInputLayout floatLabel = findViewById(R.id.text_layout_password);

                            TextInputLayout floatLabel2 = findViewById(R.id.text_layout_confirm_password);
                            floatLabel2 = floatLabel;
                            floatLabel.getEditText().addTextChangedListener(new TextWatcher() {
                                @Override
                                public void beforeTextChanged(CharSequence text, int start, int count, int after) {

                                }

                                @Override
                                public void onTextChanged(CharSequence text, int start, int count, int after) {
                                    if (text.length() > 0 && text.length() < 7) {
                                        floatLabel.setError("Unable to register");
                                        floatLabel.setErrorEnabled(true);
                                    }
                                    else {
                                        floatLabel.setErrorEnabled(false);
                                    }
                                }

                                @Override
                                public void afterTextChanged(Editable editable) {

                                }
                            });
                        }
                        hideDialog();
                    }

                    private void sendVerificationEmail() {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(RegisterActivity.this, "Sent Verification Email",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                    else {
                                        Toast.makeText(RegisterActivity.this, "Couldn't Send Verification Email",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    }
                });
    }


    /**
     * Returns True if the user's email contains '@tabian.ca'
     * @param email
     * @return
     */
    private boolean isValidDomain(String email){
        Log.d(TAG, "isValidDomain: verifying email has correct domain: " + email);
        String domain = email.substring(email.indexOf("@") + 1).toLowerCase();
        Log.d(TAG, "isValidDomain: users domain: " + domain);
        return domain.equals(DOMAIN_NAME);
    }

    /**
     * Redirects the user to the login screen
     */
    private void redirectLoginScreen(){
        Log.d(TAG, "redirectLoginScreen: redirecting to login screen.");

        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
    /**
     * Return true if @param 's1' matches @param 's2'
     * @param s1
     * @param s2
     * @return
     */
    private boolean doStringsMatch(String s1, String s2){
        return s1.equals(s2);
    }

    /**
     * Return true if the @param is null
     * @param string
     * @return
     */
    private boolean isEmpty(String string){
        return string.equals("");
    }


    private void showDialog(){
        mProgressBar.setVisibility(View.VISIBLE);

    }

    private void hideDialog(){
        if(mProgressBar.getVisibility() == View.VISIBLE){
            mProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void hideSoftKeyboard(){
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    private void setupFloatingLabelError(@NonNull final Task<AuthResult> task) {
        final TextInputLayout floatLabel = findViewById(R.id.text_layout_password);
        floatLabel.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence text, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence text, int start, int count, int after) {
                if (text.length() > 0 && text.length() < 7) {
                    floatLabel.setError(task.getException().getMessage());
                    floatLabel.setErrorEnabled(true);
                }
                else {
                    floatLabel.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

}
















