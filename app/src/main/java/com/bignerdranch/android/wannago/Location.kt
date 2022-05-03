package com.bignerdranch.android.wannago

import com.google.firebase.firestore.DocumentId

class Location(val lat: Double = 0.0, val long: Double = 0.0, @DocumentId var documentId: String = "") {

}