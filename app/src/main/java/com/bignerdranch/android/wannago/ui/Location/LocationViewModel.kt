package com.bignerdranch.android.wannago.ui.Location

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bignerdranch.android.wannago.Location
import com.google.firebase.firestore.FirebaseFirestore

private const val  TAG = "LocationViewModel"


class LocationViewModel : ViewModel() {
    // TODO: Implement the ViewModel
    private lateinit var firestore: FirebaseFirestore
    private var location: Location? = null
    internal var listOfLocations: MutableLiveData<ArrayList<Location>> = MutableLiveData<ArrayList<Location>>()  // A place to store the data we get from the firebase firestore


    private val _text = MutableLiveData<String>().apply {
        value = "List of Locations"
    }

    init {
        firestore = initFirestore()
    }

    private fun initFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    internal fun writeToFirestore(lat: Double, long: Double, collection: String) {
        Log.d(TAG, "In writeToFirestore")
        // Write the data
        val docData = hashMapOf(
            "lat" to lat,
            "long" to long,
        )
        firestore.collection(collection)
            .add(docData)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "DocumentSnapshot written with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding Document", e)
            }
    }

    internal fun deletefromFirestore(doc: String, collection: String){
        firestore.collection(collection).document(doc).delete().addOnSuccessListener{ Log.d(TAG,"Delete Successful")
        }.addOnFailureListener{ e -> Log.w(TAG, "Error deleting document", e)}
    }

    internal fun listenToCollection(collection: String) {
        firestore.collection(collection).addSnapshotListener { snapshot, e ->
            if (e != null) {
                return@addSnapshotListener
            }
            // If we get to this line, there's a connection to the database
            if (snapshot != null) {
                // there are records in this collection
                val list = ArrayList<Location>()
                val documents = snapshot.documents
                documents.forEach {
                    val listLocation = it.toObject(Location::class.java)
                    list.add(listLocation!!)
                }
                listOfLocations.value = list
            }
        }

    }

}