package com.thing.now;

import android.databinding.ObservableArrayMap;
import android.databinding.ObservableMap;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;

import java.lang.reflect.ParameterizedType;

public class FirestoreList<T> extends ObservableArrayMap<T, String> {

    private CollectionReference collectionReference;
    private Class<T> classType;
    private OnAddListener<T> onAddListener;
    private OnDeleteListener<T> onDeleteListener;

    void setOnAddListener(OnAddListener<T> onAddListener) {
        this.onAddListener = onAddListener;
    }

    void setOnDeleteListener(OnDeleteListener<T> onDeleteListener) {
        this.onDeleteListener = onDeleteListener;
    }

    public FirestoreList(Class<T> classType, CollectionReference collectionReference) {
        this.classType = classType;
        this.collectionReference = collectionReference;

        addOnMapChangedCallback(new OnMapChangedCallback<ObservableMap<T, String>, T, String>() {
            @Override
            public void onMapChanged(ObservableMap<T, String> sender, T key) {
                if (onAddListener != null) {
                    onAddListener.onAdd(sender.get(key), key);//TODO
                }
            }
        });

        collectionReference.addSnapshotListener((queryDocumentSnapshots, e) -> {
            if (queryDocumentSnapshots == null) return;
            for (DocumentChange documentChange : queryDocumentSnapshots.getDocumentChanges()) {
                switch (documentChange.getType()) {
                    case ADDED:
                        put(documentChange.getDocument().toObject(classType), documentChange.getDocument().getId());
                        break;
                    case REMOVED:
                        //TODO use bimap
                        break;
                    case MODIFIED:
                        break;
                }
            }
        });

        removeOnMapChangedCallback(new OnMapChangedCallback<ObservableMap<T, String>, T, String>() {
            @Override
            public void onMapChanged(ObservableMap<T, String> sender, T key) {
                if (onDeleteListener != null) {
                    onDeleteListener.onDelete("", key);//FIXME
                }
            }
        });
    }

    @SuppressWarnings("unchecked")
    public FirestoreList(CollectionReference collectionReference) {
        this.classType = ((Class<T>) ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[0]);
        this.collectionReference = collectionReference;
    }
//
//    public void populate(int size) {
//        collectionReference.limit(size).get().addOnSuccessListener(queryDocumentSnapshots -> {
//            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
//                this.add(documentSnapshot);
//            }
//        });
//    }

    public void add(T obj) {
        collectionReference.add(obj).addOnSuccessListener(documentReference -> {
            documentReference.getId();
            put(obj, documentReference.getId());
        });
    }

    public void add(DocumentSnapshot documentSnapshot) {
        put(documentSnapshot.toObject(classType), documentSnapshot.getId());
    }

    public void delete(Object obj) {
        String id = get(obj);
        collectionReference.document(id).delete().addOnSuccessListener(aVoid -> {
            this.remove(obj);
        });
    }

    public interface OnAddListener<T> {
        void onAdd(String firestoreId, T t);
    }

    public interface OnDeleteListener<T> {
        void onDelete(String firestoreId, T t);
    }
}