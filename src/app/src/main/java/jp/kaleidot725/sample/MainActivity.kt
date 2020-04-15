package jp.kaleidot725.sample

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        firebaseAuth = FirebaseAuth.getInstance()
        googleSignInClient = GoogleSignIn.getClient(
            this, GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
        )
    }

    override fun onStart() {
        super.onStart()
        updateUI()
    }

    private fun signInGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun signOutGoogle() {
        googleSignInClient.signOut()
    }

    private fun signInFirebaseAuth(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    updateUI()
                }
            }
    }

    private fun signOutFirebaseAuth() {
        firebaseAuth.signOut()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            try {
                val account = GoogleSignIn.getSignedInAccountFromIntent(data).getResult(ApiException::class.java)
                if (account != null) {
                    signInFirebaseAuth(account)
                }
            } catch (e: ApiException) {
                Log.e("MainActivity", "Sign In Google Error")
            }
        }
    }

    private fun updateUI() {
        val authorized = (firebaseAuth.currentUser != null)
        if (!authorized) {
            // SignInGoogle ➔ SignInFirebaseAuth ➔ UpdateUI()
            sigh_in_button.text = "Sign In"
            sigh_in_button.setOnClickListener { signInGoogle() }
        } else {
            // SignOutFirebaseAuth ➔ SignOutGoogle ➔ Update()
            sigh_in_button.text = "Sign Out"
            sigh_in_button.setOnClickListener {
                signOutFirebaseAuth()
                signOutGoogle()
                updateUI()
            }
        }
    }

    companion object {
        private const val RC_SIGN_IN = 100
    }
}
