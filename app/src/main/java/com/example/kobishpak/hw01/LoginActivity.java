package com.example.kobishpak.hw01;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kobishpak.hw01.model.User;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.util.Arrays;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 111;

    private CallbackManager mCallbackManager;
    private static final String TAG = "FACELOG";
    private String[] m_FacebookPermissions = {"email", "public_profile"};
    private FirebaseAuth mAuth;
    private EditText m_EmailEditText;
    private EditText m_PasswordEditText;
    private Button m_LoginButton;
    private LoginButton m_FacebookLoginButton;
    private SignInButton m_GoogleLoginButton;
    private TextView m_CreateAccount;
    private GoogleSignInClient m_GoogleSignInClient;
    private TextView m_ForgotPasswordText;
    private TextView m_SignInAnonymouslyText;
    private AnalyticsManager m_AnalyticsManager = AnalyticsManager.getInstance();
    private String m_Email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        InitializeInstances();

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize Facebook Login button
        mCallbackManager = CallbackManager.Factory.create();
        m_FacebookLoginButton.setReadPermissions(m_FacebookPermissions);

        m_FacebookLoginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);

                LoginWithFacebook(loginResult.getAccessToken(), loginResult);
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
            }
        });

        m_LoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, getString(R.string.init_sign_in));
                if(!((m_EmailEditText.getText().toString().isEmpty()) || (m_PasswordEditText.getText().toString().isEmpty()))) {
                    signIn(m_EmailEditText.getText().toString(), m_PasswordEditText.getText().toString());
                }
                else
                {
                    Toast.makeText(LoginActivity.this, "Please enter Email Address and Password.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        m_CreateAccount.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, EnterUserInfoActivity.class);
                startActivity(intent);
            }
        });

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        m_GoogleSignInClient = GoogleSignIn.getClient(this, gso);

        m_GoogleLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInWithGoogle();
            }
        });

        m_ForgotPasswordText.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
            }
        });

        m_SignInAnonymouslyText.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                SignInAnonymously();
            }
        });
    }

    private void SignInAnonymously() {
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInAnonymously:success");
                    FirebaseUser user = mAuth.getCurrentUser();
                    startActivity(new Intent(LoginActivity.this, AllProductsActivity.class));

                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInAnonymously:failure", task.getException());
                    Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void signInWithGoogle() {
        Intent signInIntent = m_GoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void InitializeInstances() {
        m_EmailEditText = findViewById(R.id.emailAddress);
        m_PasswordEditText = findViewById(R.id.password);
        m_LoginButton = findViewById(R.id.buttonSubmit);
        m_FacebookLoginButton = findViewById(R.id.login_button);
        m_GoogleLoginButton = findViewById(R.id.sign_in_button);
        m_CreateAccount = findViewById(R.id.createAccountTextView);
        m_ForgotPasswordText = findViewById(R.id.forgotPasswordTextView);
        m_SignInAnonymouslyText = findViewById(R.id.signInAnonymouslyTextView);
        m_AnalyticsManager.init(this);
        m_GoogleLoginButton.setSize(SignInButton.SIZE_STANDARD);
    }

    public void signIn(String email, String password){
        Log.e(TAG, "signIn==> " + email);
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            Toast.makeText(LoginActivity.this, "Logged in successfully", Toast.LENGTH_LONG).show();
                            m_AnalyticsManager.setUserID(mAuth.getCurrentUser().getUid());
                            m_AnalyticsManager.setUserProperty("email",mAuth.getCurrentUser().getEmail());
                            m_AnalyticsManager.trackLoginEvent("email_login");
                            startActivity(new Intent(LoginActivity.this, AllProductsActivity.class));
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result back to the Facebook SDK
        mCallbackManager.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
            }
        }
    }

    private void LoginWithFacebook(AccessToken token, LoginResult loginResult) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);
        GraphRequest request = GraphRequest.newMeRequest(
                loginResult.getAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        Log.v("LoginActivity", response.toString());
                        // Application code
                        try {
                            String email = object.getString("email");
                            m_Email = email;
                        }
                        catch(Exception e)
                        {

                        }
                    }
                });
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            checkIfUserExists("facebookRegistration");
                            LoginManager.getInstance().logInWithReadPermissions(LoginActivity.this, Arrays.asList(m_FacebookPermissions));
                            m_AnalyticsManager.trackLoginEvent("facebook_login");
                            startActivity(new Intent(LoginActivity.this, AllProductsActivity.class));
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed. Please make sure you have not used the same email with several registration methods",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

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
                            checkIfUserExists("googleRegistration");
                            m_AnalyticsManager.trackLoginEvent("google_login");
                            startActivity(new Intent(LoginActivity.this, AllProductsActivity.class));
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                        }
                    }
                });
    }

    private void createNewUserFacebookAndGoogle(String signupMethod) {
        Log.e(TAG, "createNewUser() >>");

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users");

        if (user == null) {
            Log.e(TAG, "createNewUser() << Error user is null");
            return;
        }
        userRef.child(user.getUid()).setValue(new User(user.getDisplayName(),m_Email,0,
                0,null, signupMethod));

        m_AnalyticsManager.trackSignupEvent(signupMethod);

        Log.e(TAG, "createNewUser() <<");
    }

    private void checkIfUserExists(final String signupMethod)
    {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference uidRef = rootRef.child("Users").child(uid);
        Log.e(TAG, "**** uidRef" + uidRef.toString());
        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()) {
                    Log.e(TAG, "**** uidRef Not exists <<----" );
                    createNewUserFacebookAndGoogle(signupMethod);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
        uidRef.addListenerForSingleValueEvent(eventListener);
    }
}
