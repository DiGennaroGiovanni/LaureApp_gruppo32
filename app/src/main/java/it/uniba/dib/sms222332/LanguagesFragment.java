package it.uniba.dib.sms222332;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public class LanguagesFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(getResources().getString(R.string.languagesToolbar));

        View view = inflater.inflate(R.layout.fragment_languages, container, false);

        ImageButton en = view.findViewById(R.id.btn_eng);
        ImageButton it = view.findViewById(R.id.btn_it);
        LanguageManager lang = new LanguageManager(getActivity());
        lang.updateResource(lang.getLang());

        en.setScaleType(ImageView.ScaleType.FIT_XY);
        it.setScaleType(ImageView.ScaleType.FIT_XY);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(150, 150);
        en.setLayoutParams(params);
        it.setLayoutParams(params);

        en.setOnClickListener(v -> {
            lang.updateResource("en");
            getActivity().recreate();
        });
        it.setOnClickListener(v -> {
            lang.updateResource("it");
            getActivity().recreate();
        });

        return view;
    }
}
