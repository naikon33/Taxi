package com.example.diplom

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.bumptech.glide.Glide
import com.example.diplom.databinding.ActivityDriverBinding
import com.google.android.gms.common.internal.service.Common
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.lang.StringBuilder

class DriverActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityDriverBinding
    private lateinit var navView: NavigationView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navController: NavController
    private lateinit var waitingDialog: AlertDialog
    private lateinit var img_avatar: ImageView
    private var imageUri: Uri? = null
    private lateinit var storageReference: StorageReference

    companion object {
        val PICK_REQUEST_CODE = 7171
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDriverBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarDriver.toolbar)

        binding.appBarDriver.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
        drawerLayout = binding.drawerLayout as DrawerLayout
        navView = binding.navView
        navController = findNavController(R.id.nav_host_fragment_content_driver)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        init()
    }

    private fun init() {
        storageReference = FirebaseStorage.getInstance().getReference()
        waitingDialog = AlertDialog.Builder(this)
            .setMessage("waiting")
            .setCancelable(false)
            .create()
        navView.setNavigationItemSelectedListener {
            if (it.itemId == R.id.nav_sign_out) {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Sign out")
                    .setMessage("Do you really want to sign out?")
                    .setNegativeButton("CANCEL") { dialogInterface, _ -> dialogInterface.dismiss() }
                    .setPositiveButton("SIGN OUT") { dialogInterface, _ ->
                        FirebaseAuth.getInstance().signOut()
                        val intent = Intent(this, SplashActivity::class.java)
                        intent.flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    }.setCancelable(false)

                val dialog = builder.create()
                dialog.setOnShowListener {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setTextColor(resources.getColor((android.R.color.holo_red_dark)))
                    dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                        .setTextColor(resources.getColor((R.color.colorAccent)))
                }
                dialog.show()

            }
            true
        }
        val headerView = navView.getHeaderView(0)
        val txt_name = headerView.findViewById<View>(R.id.txt_name) as TextView
        val txt_phone = headerView.findViewById<View>(R.id.txt_phone) as TextView
        val txt_star = headerView.findViewById<View>(R.id.txt_rating) as TextView
        img_avatar = headerView.findViewById<View>(R.id.img_avatar) as ImageView
        txt_name.text = Authentification.buildWelcomeMessage()
        txt_phone.text = Authentification.currentUser!!.phoneNumber
        txt_star.text = StringBuilder().append(Authentification.currentUser!!.rating)

        if (Authentification.currentUser != null && Authentification.currentUser!!.avatar != null && !TextUtils.isEmpty(
                Authentification.currentUser!!.avatar
            )
        ) {
            Glide.with(this)
                .load(Authentification.currentUser!!.avatar)
                .into(img_avatar)
        }

        img_avatar.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            val avatar = Intent.createChooser(intent, "Select picture")
            getResult.launch(avatar)
        }
    }

    private val getResult =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == PICK_REQUEST_CODE && it.resultCode == Activity.RESULT_OK) {
                if (it.data != null && it.data?.data != null) {
                    imageUri = it.data?.data
                    img_avatar.setImageURI(imageUri)
                    showDialogUpload()
                }
            }
        }

    fun showDialogUpload() {

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Change Avatar")
            .setMessage("Do you really want to change avatar?")
            .setNegativeButton("CANCEL") { dialogInterface, _ -> dialogInterface.dismiss() }
            .setPositiveButton("CHANGE") { _, _ ->
                if (imageUri != null) {
                    waitingDialog.show()
                    val avatarFolder =
                        storageReference.child("avatars/" + FirebaseAuth.getInstance().currentUser!!.uid)
                    avatarFolder.putFile(imageUri!!)
                        .addOnFailureListener {
                            Snackbar.make(drawerLayout, it.message!!, Snackbar.LENGTH_LONG).show()
                            waitingDialog.dismiss()
                        }.addOnCompleteListener {
                            if (it.isSuccessful) {
                                avatarFolder.downloadUrl.addOnSuccessListener { uri ->
                                    val updateData = HashMap<String, Any>()
                                    updateData.put("avatar", uri.toString())

                                    UserUtils.updateUser(drawerLayout, updateData)
                                }
                            }
                            waitingDialog.dismiss()
                        }.addOnProgressListener {
                            val progress = (100.0 * it.bytesTransferred / it.totalByteCount)
                            waitingDialog.setMessage(
                                StringBuilder("uploading: ").append(progress).append("%")
                            )
                        }
                }
            }.setCancelable(false)

        val dialog = builder.create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark))

            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                .setTextColor(ContextCompat.getColor(this, R.color.colorAccent))
        }
        dialog.show()
    }
}