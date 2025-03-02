package com.soja.farmerseller;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.soja.farmerseller.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
            productImage.setImageURI(imageUri);
        }
    }

    private void uploadImageToImgBB() {
        if (imageUri == null) {
            Toast.makeText(getContext(), "Please select an image", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            InputStream inputStream = getContext().getContentResolver().openInputStream(imageUri);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }
            byte[] imageData = byteArrayOutputStream.toByteArray();
            inputStream.close();

            OkHttpClient client = new OkHttpClient();
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("key", IMGBB_API_KEY)
                    .addFormDataPart("image", "product.jpg",
                            RequestBody.create(imageData, MediaType.parse("image/*")))
                    .build();

            Request request = new Request.Builder()
                    .url("https://api.imgbb.com/1/upload")
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String responseBody = response.body().string();
                        String imageUrl = extractImageUrl(responseBody);
                        if (imageUrl != null) {
                            saveProductToFirestore(imageUrl);
                        } else {
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(getContext(), "Failed to get image URL", Toast.LENGTH_SHORT).show();
                            });
                        }
                    } else {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "Server error: " + response.code(), Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
            getActivity().runOnUiThread(() -> {
                Toast.makeText(getContext(), "Error reading image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
    }

    private String extractImageUrl(String jsonResponse) {
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            return jsonObject.getJSONObject("data").getString("url");
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void saveProductToFirestore(String imageUrl) {
        String name = productName.getText().toString().trim();
        String description = productDescription.getText().toString().trim();
        String price = productPrice.getText().toString().trim();
        String totalSells = "0";

        if (name.isEmpty() || description.isEmpty() || price.isEmpty()) {
            Toast.makeText(getContext(), "All fields are required!", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> productData = Arrays.asList(name, description, price, imageUrl,totalSells);
        Map<String, Object> updates = new HashMap<>();

        int productNumber = getNextProductNumber();
        updates.put("product" + productNumber, productData);

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String userEmail = sharedPreferences.getString("user_email", "default_email@gmail.com");
        String documentName = userEmail.replace(".", "_");

        db.collection("MarketPlace").document(documentName)
                .set(updates, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    clearForm();
                    Toast.makeText(getContext(), "Product added successfully!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(ex -> {
                    Toast.makeText(getContext(), "Error: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void clearForm() {
        productName.setText("");
        productDescription.setText("");
        productPrice.setText("");
        productImage.setImageResource(R.drawable.ic_launcher_background);
        imageUri = null;
    }

    private int getNextProductNumber() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        int productCount = sharedPreferences.getInt("product_count", 0);
        productCount++;
        sharedPreferences.edit().putInt("product_count", productCount).apply();
        return productCount;
    }
}
