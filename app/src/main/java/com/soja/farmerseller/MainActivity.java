package com.soja.farmerseller;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottom_nav;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        bottom_nav = findViewById(R.id.bottom_nav);



        bottom_nav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;

                int id = item.getItemId();
                if (id == R.id.nav_home) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.main, new HomeFragment()).commit();
                }else if(id == R.id.nav_addItems){
                    getSupportFragmentManager().beginTransaction().replace(R.id.main, new AddItemsFragment()).commit();
                }else if(id == R.id.nav_notification) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.main, new NotificationsFragment()).commit();
                }else{
                    getSupportFragmentManager().beginTransaction().replace(R.id.main, new ProfileFragment()).commit();
                }
                return true;
            }

        });
        bottom_nav.setSelectedItemId(R.id.nav_home);

    }
}