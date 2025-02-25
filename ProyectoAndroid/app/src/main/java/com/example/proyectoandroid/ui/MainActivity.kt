package com.example.proyectoandroid.ui

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
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    private val responsableLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                val datos = GoogleSignIn.getSignedInAccountFromIntent(it.data)
                try {
                    val cuenta = datos.getResult(ApiException::class.java)
                    val idToken = cuenta?.idToken
                    if (cuenta != null && idToken != null) {
                        val credenciales = GoogleAuthProvider.getCredential(idToken, null)
                        FirebaseAuth.getInstance().signInWithCredential(credenciales)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    irActivity()
                                } else {
                                    Toast.makeText(
                                        this,
                                        "Error al autenticar con Google.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT)
                                    .show()
                            }
                    }
                } catch (e: ApiException) {
                    Toast.makeText(
                        this,
                        "Fallo en la autenticación: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            if (it.resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "El usuario a cancelado.", Toast.LENGTH_SHORT).show()
            }
        }


    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private var email = ""
    private var contraseña = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        auth = Firebase.auth
        setListeners()
    }

    private fun setListeners() {
        binding.btLimpiar.setOnClickListener {
            limpiar()
        }
        binding.btLogin.setOnClickListener {
            login()
        }
        binding.btRegistrarse.setOnClickListener {
            registrarse()
        }
        binding.btGoogle.setOnClickListener {
            loginGoogle()
        }
        binding.cbMostrar.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Se muestra la contraseña
                binding.etContrasena.transformationMethod =
                    HideReturnsTransformationMethod.getInstance()
            } else {
                // Se oculta la contraseña
                binding.etContrasena.transformationMethod =
                    PasswordTransformationMethod.getInstance()
            }
        }
    }

    private fun loginGoogle() {
        val googleConf = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            //REVISAR ESTA PARTE
            .requestIdToken(getString(R.string.default_web_client_id)) // Usar este ID
            .requestEmail()
            .build()
        val googleClient = GoogleSignIn.getClient(this, googleConf)

        googleClient.signOut() // Evitar login automático si se cierra sesión previamente

        responsableLauncher.launch(googleClient.signInIntent)
    }

    private fun registrarse() {
        if (!datosCorrectos()) return
        //Si los datos son correctos se registra.
        auth.createUserWithEmailAndPassword(email, contraseña)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    login()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, it.message.toString(), Toast.LENGTH_SHORT).show()
            }
    }

    private fun login() {
        // Validar los datos al logear un usuario
        if (datosCorrectos()) {
            auth.signInWithEmailAndPassword(email, contraseña)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        irActivity()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, it.message.toString(), Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun irActivity() {
        startActivity(Intent(this, MenuActivity::class.java))
    }

    private fun datosCorrectos(): Boolean {

        email = binding.etEmail.text.toString().trim()
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = "Se esperaba un correo correcto."
            return false
        }

        contraseña = binding.etContrasena.text.toString().trim()
        if (contraseña.length < 6) {
            binding.etContrasena.error = "La contraseña debe tener al menos 6 carácteres."
            return false
        }
        return true
    }

    private fun limpiar() {
        binding.etEmail.setText("")
        binding.etContrasena.setText("")
    }

    override fun onStart() {
        //Si ya tenemos sesion iniciada nos saltamos la parte del login
        super.onStart()
        val usuario = auth.currentUser
        if (usuario != null) irActivity()
    }

}