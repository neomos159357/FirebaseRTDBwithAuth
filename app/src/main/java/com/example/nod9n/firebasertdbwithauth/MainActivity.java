package com.example.nod9n.firebasertdbwithauth;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private EditText etMessage;
    private EditText etUser;
    private TextView googleButton;
    private SignInButton signInButton;
    private FirebaseUser user;

    private DatabaseReference mRootRef;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    private String user_id;

    private boolean checkLogIn = false;
    private boolean checkSave = false;

    private static final int RC_SIGN_IN = 45;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        signInButton = findViewById(R.id.sign_in_button);
        googleButton = (TextView) signInButton.getChildAt(0);

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        mRootRef = FirebaseDatabase.getInstance().getReference();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(MainActivity.this, gso);
        mGoogleSignInClient.signOut();

        mAuth = FirebaseAuth.getInstance();

//        if (user != null) {
//            googleButton.setText("Sign out");
//            checkLogIn = true;
//        }

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkLogIn) {
                    Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                    startActivityForResult(signInIntent, RC_SIGN_IN);
                }
                else {
                    checkLogIn = false;
                    googleButton.setText("Sign in");
                    mGoogleSignInClient.signOut();
                }
            }
        });
//        signIn();
    }

//    public void signIn() {
//
//    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w("eiei", "Google sign in failed", e);
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d("eiei", "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("eiei", "signInWithCredential:success");
                            Toast.makeText(MainActivity.this, "Authentication success.",
                                    Toast.LENGTH_SHORT).show();
                            user = mAuth.getCurrentUser();
                            user_id = user.getUid();

                            checkLogIn = true;
                            googleButton.setText("Sign out");
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("eiei", "signInWithCredential:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void saveRecord(View view) {
        if (checkLogIn) {
            checkSave = true;
            etMessage = findViewById(R.id.et_message);
            etUser = findViewById(R.id.et_user);

            String stringMessage = etMessage.getText().toString();
            String stringUser = etUser.getText().toString();

            if (stringMessage.equals("") || stringUser.equals("")) {
                return;
            }

            DatabaseReference mUserRef = mRootRef
                    .child("records").child(user_id).child("name");
            DatabaseReference mMessageRef = mRootRef
                    .child("records").child(user_id).child("message");
            mUserRef.setValue(stringUser);
            mMessageRef.setValue(stringMessage);
        }
        else
            Toast.makeText(MainActivity.this, "Sign In First!",
                    Toast.LENGTH_SHORT).show();
    }

    public void fetchQuote(View view) {
        if (checkSave) {
            if (checkLogIn) {
                mRootRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String stringUser = dataSnapshot.child("records").child(user_id).child("name").getValue().toString();
                        String stringMessage = dataSnapshot.child("records").child(user_id).child("message").getValue().toString();

                        TextView tv_message = findViewById(R.id.tv_message);
                        tv_message.setText("Message : " + stringMessage + "\nUser : " + stringUser);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
            else
                Toast.makeText(MainActivity.this, "Sign In First!",
                        Toast.LENGTH_SHORT).show();
        }
        else
            Toast.makeText(MainActivity.this, "Save Message First!",
                    Toast.LENGTH_SHORT).show();
    }
}