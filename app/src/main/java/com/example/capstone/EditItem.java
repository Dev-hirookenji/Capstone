package com.example.capstone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.app.AlertDialog;
import android.content.DialogInterface;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class EditItem extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private EditText editName;
    private EditText editQuantity;
    private EditText editPrice;
    private EditText editAddQuantity;
    private Button saveButton;
    private Button deleteButton;
    private String documentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_item);

        editName = findViewById(R.id.item_name);
        editQuantity = findViewById(R.id.item_quantity);
        editPrice = findViewById(R.id.item_price);
        editAddQuantity = findViewById(R.id.add_quantity);
        saveButton = findViewById(R.id.save_button);
        deleteButton = findViewById(R.id.delete_button);

        // Get the data from the Intent
        Intent intent = getIntent();
        String name = intent.getStringExtra("name");
        double quantity = intent.getDoubleExtra("quantity", 0);
        double price = intent.getDoubleExtra("price", 0);
        documentId = intent.getStringExtra("documentId");

        // Set the current values
        editName.setText(name);
        editQuantity.setText(String.valueOf(quantity));
        editPrice.setText(String.valueOf(price));

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get the new values
                String updatedName = editName.getText().toString();
                String updatedQuantityStr = editQuantity.getText().toString();
                String updatedAddQuantityStr = editAddQuantity.getText().toString();
                String updatedPriceStr = editPrice.getText().toString();

                if (updatedQuantityStr.isEmpty() || updatedPriceStr.isEmpty()) {
                    Toast.makeText(EditItem.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                double updatedQuantity = Double.parseDouble(updatedQuantityStr);
                double updatedAddQuantity = updatedAddQuantityStr.isEmpty() ? 0 : Double.parseDouble(updatedAddQuantityStr);
                double updatedPrice = Double.parseDouble(updatedPriceStr);

                // Update the item in Firestore
                String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
                DocumentReference itemRef = db.collection("users").document(userId).collection("item").document(documentId);

                // Update the item with prevQuantity field
                itemRef.update(
                                "name", updatedName,
                                "quantity", updatedQuantity + updatedAddQuantity,
                                "price", updatedPrice,
                                "restockedQuantity", updatedAddQuantity,
                                "prevQuantity", quantity
                        )
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(EditItem.this, "Item updated successfully", Toast.LENGTH_SHORT).show();
                                // Navigate back to Inventory activity
                                Intent intent = new Intent(EditItem.this, Inventory.class);
                                startActivity(intent);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(EditItem.this, "Error updating item", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(EditItem.this);
                builder.setMessage("Are you sure you want to delete this item?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
                                DocumentReference itemRef = db.collection("users").document(userId).collection("item").document(documentId);

                                itemRef.delete()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(EditItem.this, "Item deleted successfully", Toast.LENGTH_SHORT).show();
                                                // Navigate back to Inventory activity
                                                Intent intent = new Intent(EditItem.this, Inventory.class);
                                                startActivity(intent);
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(EditItem.this, "Error deleting item", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User cancelled the dialog, do nothing
                                dialog.cancel();
                            }
                        });
                // Create the AlertDialog object and display it
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });
    }
}