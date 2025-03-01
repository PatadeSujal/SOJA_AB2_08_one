package com.soja.farmerseller;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.soja.farmerseller.R;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AddItemFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final String IMGBB_API_KEY = "fc50074e638d1229006be871defcb3b5"; // Replace with your ImgBB API Key

    private ImageView productImage;
    private EditText productName, productDescription, productPrice;
    private Button selectImageButton, addProductButton;
//    private ProgressBar progressBar;

    private Uri imageUri;
    private FirebaseFirestore db;

    public AddItemFragment() {
        // Required empty constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_items, container, false);

        productImage = view.findViewById(R.id.productImage);
        productName = view.findViewById(R.id.productName);
        productDescription = view.findViewById(R.id.productDescription);
        productPrice = view.findViewById(R.id.productPrice);
        selectImageButton = view.findViewById(R.id.selectImage);
        addProductButton = view.findViewById(R.id.submitProductBtn);
//        progressBar = view.findViewById(R.id.progressBar);

        db = FirebaseFirestore.getInstance();

        selectImageButton.setOnClickListener(v -> openFileChooser());
        addProductButton.setOnClickListener(v -> uploadImageToImgBB());

        return view;
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            productImage.setImageURI(imageUri); // ✅ Directly set image URI
        }
    }

    private void uploadImageToImgBB() {
        if (imageUri == null) {
            Toast.makeText(getContext(), "Please select an image", Toast.LENGTH_SHORT).show();
            return;
        }

//        progressBar.setVisibility(View.VISIBLE);

        try {
            byte[] imageData = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                imageData = getContext().getContentResolver().openInputStream(imageUri).readAllBytes();
            }

            OkHttpClient client = new OkHttpClient();
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("key", IMGBB_API_KEY)
                    .addFormDataPart("image", "product.jpg", RequestBody.create(imageData, MediaType.parse("image/*")))
                    .build();

            Request request = new Request.Builder()
                    .url("https://api.imgbb.com/1/upload")
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    getActivity().runOnUiThread(() -> {
//                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Image upload failed", Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String responseBody = response.body().string();
                        String imageUrl = extractImageUrl(responseBody);
                        saveProductToFirestore("newProduct",imageUrl);
                    } else {
                        getActivity().runOnUiThread(() -> {
//                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(getContext(), "Failed to upload image", Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
//            progressBar.setVisibility(View.GONE);
            Toast.makeText(getContext(), "Error selecting image", Toast.LENGTH_SHORT).show();
        }
    }

    private String extractImageUrl(String jsonResponse) {
        try {
            org.json.JSONObject jsonObject = new org.json.JSONObject(jsonResponse);
            return jsonObject.getJSONObject("data").getString("url");
        } catch (org.json.JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void saveProductToFirestore(String productId, String imageUrl) {
        if (getContext() == null) return; // Prevent null context crash

        String name = productName.getText().toString().trim();
        String description = productDescription.getText().toString().trim();
        String price = productPrice.getText().toString().trim();

        if (name.isEmpty() || description.isEmpty() || price.isEmpty() || imageUrl == null || imageUrl.isEmpty()) {
            Toast.makeText(getContext(), "All fields are required!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Prepare product details array
        List<String> productDetails = Arrays.asList(name, description, price, imageUrl);

        // 🔥 Firestore Reference
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("SellerDetails").document("Products");

        // 🔄 Try to update the existing document
        docRef.update(productId, FieldValue.arrayUnion(productDetails))
                .addOnSuccessListener(aVoid -> {
                    if (getActivity() != null) getActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Product added successfully!", Toast.LENGTH_SHORT).show()
                    );
                })
                .addOnFailureListener(e -> {
                    // 🔄 If document doesn't exist, create it
                    Map<String, Object> newProduct = new HashMap<>();
                    newProduct.put(productId, productDetails);

                    docRef.set(newProduct, SetOptions.merge())
                            .addOnSuccessListener(aVoid -> {
                                if (getActivity() != null) getActivity().runOnUiThread(() ->
                                        Toast.makeText(getContext(), "Product added successfully!", Toast.LENGTH_SHORT).show()
                                );
                            })
                            .addOnFailureListener(ex -> {
                                if (getActivity() != null) getActivity().runOnUiThread(() ->
                                        Toast.makeText(getContext(), "Failed to add product!", Toast.LENGTH_SHORT).show()
                                );
                            });
                });
    }



}
