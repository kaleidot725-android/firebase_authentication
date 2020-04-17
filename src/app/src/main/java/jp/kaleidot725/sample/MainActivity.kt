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
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private val ioScope = CoroutineScope(Dispatchers.IO)
    private var userToken : String? = null
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            try {
                GoogleSignIn.getSignedInAccountFromIntent(data).getResult(ApiException::class.java)
                    ?.let {
                        signInFirebaseAuth(it)
                    }
            } catch (e: ApiException) {
                Log.e("MainActivity", "Sign In Google Error")
            }
        }
    }

    private fun signInFirebaseAuth(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                updateUI()
            }
        }
    }

    private fun signOutFirebaseAuth() {
        firebaseAuth.signOut()
    }

    private fun updateUI() {
        ioScope.launch {
            if (firebaseAuth.currentUser != null) {
                userToken = Tasks.await(firebaseAuth.currentUser!!.getIdToken(true)).token
            }

            withContext(Dispatchers.Main) {
                if (userToken != null) {
                    user_id_text_view.text = userToken
                    sigh_in_button.text = "Sign Out"
                    sigh_in_button.setOnClickListener {
                        signOutFirebaseAuth()
                        userToken = null
                        signOutGoogle()
                        updateUI()
                    }
                } else {
                    user_id_text_view.text = userToken
                    sigh_in_button.text = "Sign In"
                    sigh_in_button.setOnClickListener { signInGoogle() }
                }
            }
        }
    }


    companion object {
        private const val RC_SIGN_IN = 100
    }
}
