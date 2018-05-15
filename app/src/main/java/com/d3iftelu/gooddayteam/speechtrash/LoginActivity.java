package com.d3iftelu.gooddayteam.speechtrash;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    private static final String TAG = "LoginActivity";
    private static final int RC_SIGN_IN = 9001;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private GoogleApiClient mGoogleApiClient;

    private EditText editTextEmailAddress;
    private EditText editTextPassword;
    private TextInputLayout inputLayoutEmailAddress;
    private TextInputLayout inputLayoutPassword;
    private Button btnLoginWithEmail;
    private String email;
    private ProgressBar progressBarLogin;
    static boolean calledAlready = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, (GoogleApiClient.OnConnectionFailedListener) this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        mAuth = FirebaseAuth.getInstance();

        SignInButton startLogin = (SignInButton) findViewById(R.id.sign_in_google);
        startLogin.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                progressBarLogin.setVisibility(View.VISIBLE);
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });

        editTextEmailAddress = (EditText) findViewById(R.id.input_email);
        editTextPassword = (EditText) findViewById(R.id.input_password);
        btnLoginWithEmail = (Button) findViewById(R.id.btn_login_email);

        progressBarLogin =(ProgressBar) findViewById(R.id.check_login);
        inputLayoutEmailAddress = (TextInputLayout) findViewById(R.id.input_layout_email);
        inputLayoutPassword = (TextInputLayout) findViewById(R.id.input_layout_password);

        if(!calledAlready){
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
            calledAlready = true;
        }

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    updateUI(user);
                }
            }
        };
        progressBarLogin.setVisibility(View.GONE);
        actionLogin();
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    /**
     * to handle when button clicked
     */

    private void actionLogin(){
        btnLoginWithEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBarLogin.setVisibility(View.VISIBLE);
                String emailAddress = editTextEmailAddress.getText().toString().trim();
                String password = editTextPassword.getText().toString();
                if(validationEmailAndEmail(emailAddress, password))
                    firebaseAuthWithEmail(emailAddress, password);
            }
        });
    }

    /**
     * validating input from user
     * @param emailAddress user email addres
     * @param password user password
     * @return valid or not email and password
     */
    private boolean validationEmailAndEmail(String emailAddress, String password) {
        if(!emailAddress.contains("@") || emailAddress.equals("")){
            progressBarLogin.setVisibility(View.GONE);
            if(emailAddress.equals("")){
                inputLayoutEmailAddress.setError("Email address can't empty!");
            }
            else if(!emailAddress.contains("@") || !emailAddress.contains("@admin.com")){
                inputLayoutEmailAddress.setError("Not a valid email address! (@admin.com)");
            }
            return false;
        }
        else if(password.length() <6 || password.equals("")){
            progressBarLogin.setVisibility(View.GONE);
            inputLayoutEmailAddress.setErrorEnabled(false);
            if (password.equals("")){
                inputLayoutPassword.setError("Password can't empty!");
            }
            else if(password.length() <6){
                inputLayoutPassword.setError("Not a valid password!");
            }
            return false;
        }
        else {
            inputLayoutEmailAddress.setErrorEnabled(false);
            inputLayoutPassword.setErrorEnabled(false);
            return true;
        }
    }

    /**
     * for manual login
     * @param email String email addres that user input
     * @param password String password that user input
     */

    private void firebaseAuthWithEmail(String email, String password){
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());
                        if (!task.isSuccessful()) {
                            progressBarLogin.setVisibility(View.GONE);
                            Log.w(TAG, "signInWithEmail:failed", task.getException());
                            Toast.makeText(LoginActivity.this, R.string.auth_failed,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        progressBarLogin.setVisibility(View.GONE);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                // Google Sign In failed, update UI appropriately
                // ...
            }
        }
    }

    /**
     * for login with google
     * @param acct google sign account
     */
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            progressBarLogin.setVisibility(View.GONE);
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        // ...
                    }
                });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    /**
     * for chance activity if user is login
     * @param user firebase user
     */
    private void updateUI(FirebaseUser user) {
        if (user != null) {
            email = user.getEmail();
            if (email.contains("@admin")) {
                Intent goToNextActivity = new Intent(this, AdminActivity.class);
                startActivity(goToNextActivity);
            } else {
                Intent goToNextActivity = new Intent(this, PetugasActivity.class);
                startActivity(goToNextActivity);
            }
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
