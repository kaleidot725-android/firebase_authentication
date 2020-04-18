package jp.kaleidot725.sample

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import jp.kaleidot725.sample.databinding.ActivityMainBinding
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {
    private val viewModel: MainViewModel = MainViewModel()
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // データバインディング
        DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
            .also { binding ->
                binding.viewModel = viewModel
                binding.lifecycleOwner = this
            }

        // Google認証クライアント 作成
        googleSignInClient = GoogleSignIn.getClient(
            this,
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
        )

        // Firebase認証クライアント 作成
        firebaseAuth = FirebaseAuth.getInstance()

        // ボタンを押すと発行される Event を Observe して、 SignIn や SignOut を実行できるようにする。
        viewModel.event.observe(this, Observer {
            when (it) {
                MainViewModel.Event.SIGN_IN -> {
                    signInGoogle()
                }
                MainViewModel.Event.SIGN_OUT -> {
                    signOutGoogle()
                    signOutFirebaseAuth()
                    viewModel.reset()
                }
            }
        })

        coroutineScope.launch {
            val token = loadGetIdToken(firebaseAuth.currentUser)
            withContext(Dispatchers.Main) {
                viewModel.update(token)
            }
        }
    }

    private fun signInGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun signInFirebaseAuthWithGoogle(
        firebaseAuth: FirebaseAuth,
        account: GoogleSignInAccount,
        completed: (FirebaseUser) -> (Unit)
    ) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
            if (!task.isSuccessful) {
                return@addOnCompleteListener
            }

            if (firebaseAuth.currentUser == null) {
                return@addOnCompleteListener
            }

            completed(firebaseAuth.currentUser!!)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != RC_SIGN_IN) {
            return
        }

        val account = GoogleSignIn.getSignedInAccountFromIntent(data).getResult(ApiException::class.java)
        if (account == null) {
            return
        }

        signInFirebaseAuthWithGoogle(firebaseAuth, account) { firebaseUser ->
            coroutineScope.launch {
                val token = loadGetIdToken(firebaseAuth.currentUser)
                withContext(Dispatchers.Main) {
                    viewModel.update(token)
                }
            }
        }
    }

    private fun loadGetIdToken(firebaseUser: FirebaseUser?): String? {
        var userToken: String? = null
        runBlocking {
            if (firebaseUser != null) {
                userToken = Tasks.await(firebaseUser.getIdToken(true)).token
            }
        }
        return userToken
    }

    private fun signOutGoogle() {
        googleSignInClient.signOut()
    }

    private fun signOutFirebaseAuth() {
        firebaseAuth.signOut()
    }

    companion object {
        private const val RC_SIGN_IN = 100
    }
}
