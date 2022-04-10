package org.tensorflow.lite.examples.detection.utils;

import static org.tensorflow.lite.examples.detection.assistant_for_the_blind_activities.MainActivity.SIGN_IN_GOOGLE;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import org.tensorflow.lite.examples.detection.R;

public class GoogleLogin {

    private final FirebaseAuth mAuth;
    private GoogleApiClient googleApiClient;
    private Activity activity;

    public GoogleLogin(Activity activity){
        this.activity = activity;
        this.mAuth = FirebaseAuth.getInstance();
    }

    public void createGoogleApiClient(){

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(activity.getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleApiClient = new GoogleApiClient.Builder(activity).enableAutoManage((FragmentActivity) activity, (GoogleApiClient.OnConnectionFailedListener) activity)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso).build();
    }

    public void onGoogleButtonClicked(){
        Intent intent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        activity.startActivityForResult(intent, SIGN_IN_GOOGLE);
    }

    public void onReturnFromGoogleScreen(Intent data){
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
        try {
            // Google Sign In was successful, authenticate with Firebase
            GoogleSignInAccount account = task.getResult(ApiException.class);
            firebaseAuthWithGoogle(account.getIdToken());
        } catch (Exception e) {
            Toast.makeText(activity, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(activity, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(activity, "Logging in...", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(activity, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
