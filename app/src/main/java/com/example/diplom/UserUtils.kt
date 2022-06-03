package com.example.diplom

import android.content.Context
import android.view.View
import android.widget.Toast
import com.example.diplom.Model.TokenModel
import com.google.android.gms.common.internal.service.Common
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

object UserUtils {
    fun updateUser(
        view: View?,
        updateData: Map<String, Any>
    ){
        FirebaseDatabase.getInstance()
            .getReference(Authentification.Driver_Info)
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
            .updateChildren(updateData)
            .addOnFailureListener {
                Snackbar.make(view!!, it.message!!, Snackbar.LENGTH_LONG).show()
            }.addOnSuccessListener {
                Snackbar.make(view!!, "UPdate successfull", Snackbar.LENGTH_LONG).show()
            }
    }
    fun updateToken(context: Context, token: String){
        val tokenModel = TokenModel()
        tokenModel.token = token

        FirebaseDatabase.getInstance()
            .getReference(Authentification.TOKEN_RFERENCE)
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
            .setValue(token)
            .addOnFailureListener { Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()}
            .addOnSuccessListener {  }
    }


}