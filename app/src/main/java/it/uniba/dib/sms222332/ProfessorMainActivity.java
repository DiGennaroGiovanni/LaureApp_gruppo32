package it.uniba.dib.sms222332;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.navigation.NavigationBarView;


public class ProfessorMainActivity extends AppCompatActivity {




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NavigationBarView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(navListener);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new StudentHomeFragment()).commit();

    }

    private final NavigationBarView.OnItemSelectedListener navListener = item -> {
        Fragment selectedFragment;

        switch (item.getItemId()){

            case R.id.star_button:
                selectedFragment = new FavoritesFragment();
                break;

            case R.id.chat_button:
                selectedFragment = new MessagesFragment();
                break;

            default:
                selectedFragment = new ProfessorHomeFragment();
        }

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
        return true;
    };
}