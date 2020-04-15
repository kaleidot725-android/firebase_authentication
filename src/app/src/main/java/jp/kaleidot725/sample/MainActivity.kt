package jp.kaleidot725.sample

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 認証状態を取得する
        val auth  = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        val authorized = (currentUser != null)

        // 認証状態に応じて、サインインするかログアウトするか変化させる
        if (!authorized) {
            sigh_in_button.text = "Sigh In"
            sigh_in_button.setOnClickListener { sighIn() }
        } else {
            sigh_in_button.text = "Log Out"
            sigh_in_button.setOnClickListener { logout() }
        }
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

    private fun logout() {

    }

    companion object {
        private const val RC_SIGN_IN = 100
    }
}
