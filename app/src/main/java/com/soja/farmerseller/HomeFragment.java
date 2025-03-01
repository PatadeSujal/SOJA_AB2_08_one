package com.soja.farmerseller;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.Map;

public class HomeFragment extends Fragment {

    ImageButton chatbot_btn;
    private FirebaseFirestore firestore;
    private ListenerRegistration listenerRegistration;
    private ArrayList<ProductSellsManager> soldProductsList; // ✅ Corrected Type Safety
    private ProductSellAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.porfolioRecyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        firestore = FirebaseFirestore.getInstance();
        soldProductsList = new ArrayList<>();
        adapter = new ProductSellAdapter(soldProductsList, getContext());
        recyclerView.setAdapter(adapter);

        // Firestore Data Listener
        listenerRegistration = firestore.collection("SellerDetails")
                .document("ProductsPortfolio")
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (snapshot != null && snapshot.exists()) {
                        soldProductsList.clear();

                        try {
                            Map<String, Object> data = snapshot.getData();
                            if (data != null) {
                                for (Map.Entry<String, Object> entry : data.entrySet()) {
                                    if (entry.getValue() instanceof ArrayList<?>) {
                                        ArrayList<?> products = (ArrayList<?>) entry.getValue();

                                        if (products.size() == 3) { // ✅ Ensure at least 4 elements exist
                                            String productName = products.get(0).toString();
                                            String productSells = products.get(1).toString();
                                            String productEarnings = products.get(2).toString();

                                            ProductSellsManager p = new ProductSellsManager(productName, productSells, productEarnings);
                                            soldProductsList.add(p);
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            Toast.makeText(getContext(), "Data Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }

                        adapter.notifyDataSetChanged();
                    }
                });

        return view;
    }
}
