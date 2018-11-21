package com.thing.now

import android.databinding.ObservableArrayMap
import android.databinding.ObservableMap

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldPath

import java.lang.reflect.ParameterizedType

class FirestoreList<E> : HashMap<String, E> {

    private var collectionReference: CollectionReference? = null
    private var classType: Class<E>? = null
    var addListener: OnAddListener<E>? = null
    var deleteListener: OnDeleteListener<E>? = null
    var modifyListener: OnModifyListener<E>? = null

    var addedListener: OnAddedListener<E>? = null
    var deletedListener: OnDeletedListener<E>? = null
    var modifiedListener: OnModifiedListener<E>? = null

    var failedListener: OnFailedListener<E>? = null

    constructor(classType: Class<E>, collectionReference: CollectionReference, docsToFetch: List<String>) {
        this.classType = classType
        this.collectionReference = collectionReference
        val dr = ArrayList<DocumentReference>()
        for (dtf in docsToFetch) {
            dr.add(collectionReference.document(dtf))
        }
        var query = if (docsToFetch.isEmpty()) collectionReference else collectionReference.whereEqualTo(
            FieldPath.documentId(),
            docsToFetch
        )
        collectionReference.addSnapshotListener { queryDocumentSnapshots, e ->
            if (queryDocumentSnapshots == null) return@addSnapshotListener
            for (dc in queryDocumentSnapshots.documentChanges) {
                when (dc.type) {
                    DocumentChange.Type.ADDED -> {
                        var eC = dc.document.toObject(classType)
                        put(dc.document.id, eC)
                        addedListener?.onAdded(dc.document.id, eC)
                    }
                    DocumentChange.Type.REMOVED -> {
                        var eC = get(dc.document.id) ?: return@addSnapshotListener
                        remove(dc.document.id)
                        deleteListener?.onDelete(dc.document.id, eC)
                    }
                    DocumentChange.Type.MODIFIED -> {
                        var nE = dc.document.toObject(classType)
                        remove(dc.document.id)
                        put(dc.document.id, nE)
                        modifiedListener?.onModified(dc.document.id, nE)
                    }
                }
            }
        }
    }

    constructor(collectionReference: CollectionReference) {
        this.classType = (javaClass
            .genericSuperclass as ParameterizedType).actualTypeArguments[0] as Class<E>
        this.collectionReference = collectionReference
    }

    //
    //    public void populate(int size) {
    //        collectionReference.limit(size).get().addOnSuccessListener(queryDocumentSnapshots -> {
    //            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
    //                this.add(documentSnapshot);
    //            }
    //        });
    //    }
    fun add(obj: E) {
        var id = obj.toString()
        addListener?.onAdd(id, obj)
        collectionReference!!.add(obj as Any).addOnSuccessListener {
            put(it.id, obj)
        }.addOnFailureListener {
            failedListener?.onFailed(id, obj)
        }
    }

    fun getIdFor(e: E): String {
        val arr = entries.toTypedArray()
        for (i in 0 until arr.size) {
            if (arr[i].value == e) {
                return arr[i].key
            }
        }
        return ""
    }

    fun delete(obj: E) {
        val id = getIdFor(obj)
        deleteListener?.onDelete(id, obj)
        collectionReference!!.document(id!!).delete().addOnSuccessListener {
            remove(id)
        }.addOnFailureListener {
            failedListener?.onFailed(id, obj)
        }
    }

    fun modify(obj: E) {
        val id = getIdFor(obj)
        modifyListener?.onModify(id, obj)
        collectionReference!!.document(id).set(obj as Any).addOnSuccessListener {
            remove(id)
            put(id, obj)
        }.addOnFailureListener {
            failedListener?.onFailed(id, obj)
        }
    }

    interface OnAddListener<E> {
        fun onAdd(k: String, e: E)
    }

    interface OnDeleteListener<E> {
        fun onDelete(k: String, e: E)
    }

    interface OnModifyListener<E> {
        fun onModify(k: String, e: E)
    }

    interface OnAddedListener<E> {
        fun onAdded(k: String, e: E)
    }

    interface OnDeletedListener<E> {
        fun onDeleted(k: String, e: E)
    }

    interface OnModifiedListener<E> {
        fun onModified(k: String, e: E)
    }

    interface OnFailedListener<E> {
        fun onFailed(k: String, e: E)
    }
}