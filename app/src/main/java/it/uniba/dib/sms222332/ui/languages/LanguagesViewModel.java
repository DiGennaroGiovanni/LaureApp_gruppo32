package it.uniba.dib.sms222332.ui.languages;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class LanguagesViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public LanguagesViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is languages fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}