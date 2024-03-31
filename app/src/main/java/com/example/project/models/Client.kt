package com.example.project.models

import javax.inject.Inject

data class Client @Inject constructor(
    var uid:String?,
    var nom: String,
    val prenom: String,
    val date_naissanace:String,
    val adresse:String,
    val numCin:String,
    val domicile:String,
    val numTele:String,
    var deviceInfo: DeviceInfo?,
    var fingerPrint: String,
    var identityCardFrontUrl: String?,
    var identityCardBackUrl: String?,
    var facePhotoUrl: String?
){

}