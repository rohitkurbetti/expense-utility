package com.example.expenseutility.entityadapter;

import java.io.Serializable;
import java.util.List;

public class FirebaseSubItem implements Serializable {

    private List<FirebaseSubSubItem> firebaseSubSubItems;

    public List<FirebaseSubSubItem> getFirebaseSubSubItems() {
        return firebaseSubSubItems;
    }

    public void setFirebaseSubSubItems(List<FirebaseSubSubItem> firebaseSubSubItems) {
        this.firebaseSubSubItems = firebaseSubSubItems;
    }
}
