package it.uniba.dib.sms222332;

import android.os.Bundle;


import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;


public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final DrawerLayout drawerLayout = findViewById(R.id.drawerLayout);

        findViewById(R.id.icon_settings).setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               drawerLayout.openDrawer(GravityCompat.START);
           }

        });


       // ImageView settingsIcon = findViewById(R.id.icon_settings);
       /* ImageView profileIcon = findViewById(R.id.icon_profile);


        profileIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new StudentProfileFragment()).commit();
            }
        });*/

      /*  settingsIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new SettingsFragment()).commit();
            }
        });*/

        NavigationBarView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(navListener);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();

       /* codice per collegare il bottone profilo al fragment

        NavigationView navigationView = findViewById(R.id.navigationView);
        NavController navController = Navigation.findNavController(this, R.id.fragment);
        NavigationUI.setupWithNavController(navigationView, navController);*/

    }

    private final NavigationBarView.OnItemSelectedListener navListener = item -> {
            Fragment selectedFragment;

            switch (item.getItemId()) {

                case R.id.star_button:
                    selectedFragment = new FavoritesFragment();
                    break;

                case R.id.chat_button:
                    selectedFragment = new MessagesFragment();
                    break;

                default:
                    selectedFragment = new HomeFragment();
            }


            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
        return true;
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.languages:
                // Mostra il sottomenu
                return true;
            case R.id.chat_button:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


}