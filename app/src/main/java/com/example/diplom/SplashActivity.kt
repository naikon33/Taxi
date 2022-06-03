package com.example.diplom

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.example.diplom.Model.InfoModel
import com.firebase.ui.auth.AuthMethodPickerLayout
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.iid.internal.FirebaseInstanceIdInternal
import com.google.firebase.messaging.FirebaseMessaging
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import java.util.*
import java.util.concurrent.TimeUnit

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private val LOGIN_REQUEST_CODE = 7171

    private lateinit var providers: List<AuthUI.IdpConfig>
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var listener: FirebaseAuth.AuthStateListener
    private lateinit var database: FirebaseDatabase
    private lateinit var reference: DatabaseReference

    private lateinit var progressBar: ProgressBar
    override fun onStart() {
        super.onStart()
        delaySplashScreen()
    }

    override fun onStop() {
        firebaseAuth.removeAuthStateListener { listener }
        super.onStop()
    }

    @SuppressLint("CheckResult")
    private fun delaySplashScreen() {
        Completable.timer(3, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
            .subscribe {
                firebaseAuth.addAuthStateListener(listener)
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        init()
    }

    private fun init() {
        database= FirebaseDatabase.getInstance()
        reference=database.getReference(Authentification.Driver_Info)
        providers = listOf(
            AuthUI.IdpConfig.PhoneBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

        firebaseAuth = FirebaseAuth.getInstance()
        listener = FirebaseAuth.AuthStateListener { myFirebaseAuth ->
            val user = myFirebaseAuth.currentUser
            if (user != null) {
                FirebaseMessaging.getInstance().token.addOnFailureListener { e ->
                    Log.e(
                        "SplashScreen",
                        e.message.toString()
                    )
                }
                    .addOnSuccessListener { instanceIdResult ->


                        Log.d("TOKEN", " " + instanceIdResult)
                        UserUtils.updateToken(this, instanceIdResult)

                        checkUserFromFirebase()
                    }
            }
            else
                showLoginLayout()

        }
    }

    private fun checkUserFromFirebase() {
        reference
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
            .addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
//                        Toast.makeText(this@SplashActivity,"User already register",Toast.LENGTH_SHORT).show()
                        val model=snapshot.getValue(InfoModel::class.java)
                        goToHomeActivity(model)
                    }
                    else{
                        showRegister()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@SplashActivity,error.message,Toast.LENGTH_SHORT).show()
                }

            })
    }

    private fun goToHomeActivity(model: InfoModel?) {
        Authentification.currentUser=model
        startActivity(Intent(this,DriverActivity::class.java))
        finish()
    }

    private fun showRegister() {
        val builder= AlertDialog.Builder(this,R.style.DialogTheme)
        val itemView= LayoutInflater.from(this).inflate(R.layout.layout_register,null)

        val ed_first_name=itemView.findViewById<View>(R.id.ed_first_name) as TextInputEditText
        val ed_last_name=itemView.findViewById<View>(R.id.ed_last_name) as TextInputEditText
        val ed_phone_number=itemView.findViewById<View>(R.id.ed_phone_number) as TextInputEditText

        val btn_continue=itemView.findViewById<View>(R.id.btn_register) as Button

        if(FirebaseAuth.getInstance().currentUser!!.phoneNumber !=null && TextUtils.isDigitsOnly(FirebaseAuth.getInstance().currentUser!!.phoneNumber)){
            ed_phone_number.setText(FirebaseAuth.getInstance().currentUser!!.phoneNumber)
        }
        builder.setView(itemView)
        val dialog=builder.create()
        dialog.show()
        btn_continue.setOnClickListener {
            if (TextUtils.isDigitsOnly(ed_first_name.text.toString())) {
                Toast.makeText(this, "Please enter first name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else if (TextUtils.isDigitsOnly(ed_last_name.text.toString())) {
                Toast.makeText(this, "Please enter last name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else if (TextUtils.isDigitsOnly(ed_phone_number.text.toString())) {
                Toast.makeText(this, "Please enter phone number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            else{
                val model = InfoModel()
                model.firstName=ed_first_name.text.toString()
                model.lastName=ed_last_name.text.toString()
                model.phoneNumber=ed_phone_number.text.toString()
                model.rating=0.0

                reference.child(FirebaseAuth.getInstance().currentUser!!.uid)
                    .setValue(model)
                    .addOnFailureListener{
                        Toast.makeText(this,"${it.message}",Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                        progressBar.visibility=View.GONE
                    }
                    .addOnSuccessListener {
                        Toast.makeText(this,"Register Successfully!",Toast.LENGTH_SHORT).show()
                        dialog.dismiss()

                        goToHomeActivity(model)

                        progressBar.visibility=View.GONE
                    }
            }
        }
    }

    private fun showLoginLayout() {
        val authMethodPickerLayout = AuthMethodPickerLayout.Builder(R.layout.activity_sign_up)
            .setPhoneButtonId(R.id.bt_phone_sign_in)
            .setGoogleButtonId(R.id.bt_google_sign_in)
            .build()


        val instaceOfAuth = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAuthMethodPickerLayout(authMethodPickerLayout)
            .setTheme(R.style.LoginTheme)
            .setAvailableProviders(providers)
            .setIsSmartLockEnabled(false)
            .build()

        getResult.launch(instaceOfAuth)
    }
    private val getResult =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {

            if (it.resultCode == LOGIN_REQUEST_CODE) {
                val response = IdpResponse.fromResultIntent(it.data)
                if (it.resultCode == Activity.RESULT_OK) {
                    val user = FirebaseAuth.getInstance().currentUser
                } else {
                    Toast.makeText(this@SplashActivity, ""+ response!!.error!!.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
}