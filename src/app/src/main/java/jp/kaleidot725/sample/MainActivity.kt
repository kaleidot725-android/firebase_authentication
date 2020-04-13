package jp.kaleidot725.sample

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private var currentUser: FirebaseUser? = null
    private var authorized: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sigh_in_button.setOnClickListener { sighIn() }
    }

    override fun onStart() {
        super.onStart()

        auth  = FirebaseAuth.getInstance()
        currentUser = auth.currentUser
        authorized = (currentUser != null)
        sigh_in_status_text_view.text = getStatusStr(authorized)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN && resultCode == Activity.RESULT_OK) {
            // TODO
        }
    }

    private fun sighIn() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.PhoneBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

        val intent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .setLogo(R.drawable.app_icon)
            .setTheme(R.style.AppTheme)
            .build()

        startActivityForResult(intent, RC_SIGN_IN)
    }

    private fun getStatusStr(authorized: Boolean): String = if (authorized) "Authorized" else "Unauthorized"

    companion object {
        private const val RC_SIGN_IN = 100
    }
}
