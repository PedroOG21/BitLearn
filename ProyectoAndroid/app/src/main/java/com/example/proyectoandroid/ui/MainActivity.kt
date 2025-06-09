package com.example.proyectoandroid.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.proyectoandroid.R
import com.example.proyectoandroid.databinding.ActivityMainBinding
import com.example.proyectoandroid.ui.prefenrences.Preferences
import com.example.proyectoandroid.utils.LocaleHelper
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        val prefs = Preferences(newBase)
        super.attachBaseContext(LocaleHelper.wrap(newBase, prefs.getIdioma()))
    }

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private var email       = ""
    private var contraseña  = ""

    private val googleLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val cuenta   = task.getResult(ApiException::class.java)
                    val idToken  = cuenta?.idToken
                    if (cuenta != null && idToken != null) {
                        val cred = GoogleAuthProvider.getCredential(idToken, null)
                        FirebaseAuth.getInstance().signInWithCredential(cred)
                            .addOnCompleteListener { t ->
                                if (t.isSuccessful) irActivity()
                                else Toast.makeText(
                                    this,
                                    getString(R.string.toast_error_autenticar_google),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(
                                    this,
                                    getString(R.string.toast_error_generico, it.message),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                } catch (e: ApiException) {
                    Toast.makeText(
                        this,
                        getString(R.string.toast_fallo_autenticacion_google, e.message),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else if (result.resultCode == RESULT_CANCELED) {
                Toast.makeText(
                    this,
                    getString(R.string.toast_google_cancelado),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val sys = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom)
            insets
        }

        auth = Firebase.auth
        setListeners()
    }

    private fun setListeners() {
        binding.btLogin.setOnClickListener   { login() }
        binding.btRegistrarse.setOnClickListener { registrarse() }
        binding.btGoogle.setOnClickListener  { loginGoogle() }

        binding.cbMostrar.setOnCheckedChangeListener { _, isChecked ->
            binding.etContrasena.transformationMethod =
                if (isChecked) HideReturnsTransformationMethod.getInstance()
                else            PasswordTransformationMethod.getInstance()
        }
    }

    private fun loginGoogle() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        val googleClient = GoogleSignIn.getClient(this, gso)
        googleClient.signOut()               // Evitar login automático previo
        googleLauncher.launch(googleClient.signInIntent)
    }

    private fun registrarse() {
        if (!datosCorrectos()) return
        auth.createUserWithEmailAndPassword(email, contraseña)
            .addOnCompleteListener { if (it.isSuccessful) login() }
            .addOnFailureListener {
                Toast.makeText(this, it.message.toString(), Toast.LENGTH_SHORT).show()
            }
    }

    private fun login() {
        if (!datosCorrectos()) return
        auth.signInWithEmailAndPassword(email, contraseña)
            .addOnCompleteListener { if (it.isSuccessful) { limpiar(); irActivity() } }
            .addOnFailureListener  {
                Toast.makeText(this, it.message.toString(), Toast.LENGTH_SHORT).show()
            }
    }

    private fun irActivity() = startActivity(Intent(this, MenuActivity::class.java))

    private fun datosCorrectos(): Boolean {
        email = binding.etEmail.text.toString().trim()
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = getString(R.string.error_email_invalido)
            return false
        }
        contraseña = binding.etContrasena.text.toString().trim()
        if (contraseña.length < 6) {
            binding.etContrasena.error = getString(R.string.error_contrasena_corta)
            return false
        }
        return true
    }

    private fun limpiar() {
        binding.etEmail.setText("")
        binding.etContrasena.setText("")
    }

    override fun onStart() {
        super.onStart()
        if (auth.currentUser != null) irActivity()
    }
}
