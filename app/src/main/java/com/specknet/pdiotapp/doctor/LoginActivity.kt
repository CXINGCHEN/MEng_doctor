package com.specknet.pdiotapp.doctor

import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.identity.SignInCredential
import com.google.android.gms.common.api.ApiException
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

/**
 * 登录页
 */
class LoginActivity : AppCompatActivity() {

    companion object {
        const val TAG = "LoginActivity"
    }

    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest

    private lateinit var auth: FirebaseAuth
    private var currentUser: FirebaseUser? = null

    private val loginResultHandler = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result: ActivityResult ->

        if (result.resultCode == RESULT_OK) {
            var credential: SignInCredential? = null
            try {
                credential = oneTapClient.getSignInCredentialFromIntent(result.data)
                val idToken = credential.googleIdToken
                val username = credential.id
                val password = credential.password
                if (idToken != null) {
                    // Got an ID token from Google. Use it to authenticate
                    // with your backend.
                    Log.d(TAG, "Got ID token.")
                    // Got an ID token from Google. Use it to authenticate
                    // with Firebase.
                    val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                    auth.signInWithCredential(firebaseCredential)
                        .addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "signInWithCredential:success")
                                val user = auth.currentUser

                                this@LoginActivity.onLoginSuccess()

                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "signInWithCredential:failure", task.exception)
                            }
                        }
                } else if (password != null) {
                    // Got a saved username and password. Use them to authenticate
                    // with your backend.
                    Log.d(TAG, "Got password.")
                }
            } catch (e: ApiException) {
                e.printStackTrace()
            }
        }

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize Firebase Auth
        auth = Firebase.auth

        currentUser = auth.currentUser
        if (currentUser != null) {
            Log.i(TAG, "onCreate: currentUser = ${currentUser?.email}")
            onLoginSuccess()
        }

        oneTapClient = Identity.getSignInClient(this)
        signInRequest = BeginSignInRequest.builder().setPasswordRequestOptions(
            BeginSignInRequest.PasswordRequestOptions.builder().setSupported(true).build()
        ).setGoogleIdTokenRequestOptions(
            BeginSignInRequest.GoogleIdTokenRequestOptions.builder().setSupported(true)
                // Your server's client ID, not your Android client ID.
                .setServerClientId("139441038322-cd2pdj2dff35dkgl6lp2c32i3uijk1ll.apps.googleusercontent.com")
                // Only show accounts previously used to sign in.
                .setFilterByAuthorizedAccounts(false).build()
        )
            // Automatically sign in when exactly one credential is retrieved.
            .setAutoSelectEnabled(false).build()

    }

    /**
     * 当登录成功的时候
     */
    fun onLoginSuccess() {

        val userInfoBean = UserInfoBean().apply {
            age = 25
            email = auth.currentUser?.email
        }

        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    fun googleLogin(view: View) {

        // 调出谷歌登录弹窗
        oneTapClient.beginSignIn(signInRequest).addOnSuccessListener(this) { signResult ->
            try {
                loginResultHandler.launch(
                    IntentSenderRequest.Builder(signResult.pendingIntent.intentSender).build()
                )

            } catch (e: IntentSender.SendIntentException) {
                e.printStackTrace()
                Log.e(TAG, "Couldn't start One Tap UI: ${e.localizedMessage}")
            }
        }.addOnFailureListener(this) { e ->
            // No saved credentials found. Launch the One Tap sign-up flow, or
            // do nothing and continue presenting the signed-out UI.
            e.printStackTrace()
            Log.d(TAG, e.localizedMessage)
        }

    }

    fun login(view: View) {

        val emailEditText = findViewById<TextInputEditText>(R.id.email)
        val passwordEditText = findViewById<TextInputEditText>(R.id.password)

//

        auth.signInWithEmailAndPassword(
            emailEditText.text.toString(), passwordEditText.text.toString()
        ).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                // Sign in success, update UI with the signed-in user's information
                Log.d(TAG, "login: signInWithEmail:success")
                val user = auth.currentUser
                Toast.makeText(
                    baseContext,
                    "login success.",
                    Toast.LENGTH_SHORT,
                ).show()

                this@LoginActivity.onLoginSuccess()

            } else {
                // If sign in fails, display a message to the user.
                Log.w(TAG, "login: signInWithEmail:failure", task.exception)
                Toast.makeText(
                    baseContext,
                    "login: Authentication failed.",
                    Toast.LENGTH_SHORT,
                ).show()
            }
        }

    }

    fun logout(view: View) {
        auth.signOut()
    }


    fun register(view: View) {

        val emailEditText = findViewById<TextInputEditText>(R.id.email)
        val passwordEditText = findViewById<TextInputEditText>(R.id.password)

        Log.i(TAG, "register: email = ${emailEditText.text.toString()}")
        Log.i(TAG, "register: password = ${passwordEditText.text.toString()}")

        auth.createUserWithEmailAndPassword(
            emailEditText.text.toString(), passwordEditText.text.toString()
        ).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                // Sign in success, update UI with the signed-in user's information
                Log.d(TAG, "createUserWithEmail:success")
                val user = auth.currentUser
                Toast.makeText(
                    baseContext,
                    "Register success.",
                    Toast.LENGTH_SHORT,
                ).show()
            } else {
                // If sign in fails, display a message to the user.
                Log.w(TAG, "createUserWithEmail:failure", task.exception)
                Toast.makeText(
                    baseContext,
                    "Register failed: ${task.exception?.localizedMessage}",
                    Toast.LENGTH_SHORT,
                ).show()
            }
        }
    }

}