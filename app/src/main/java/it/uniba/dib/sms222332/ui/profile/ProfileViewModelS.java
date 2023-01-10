package it.uniba.dib.sms222332.ui.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ProfileViewModelS extends ViewModel {

    private final MutableLiveData<String> mText;

    public ProfileViewModelS() {
        mText = new MutableLiveData<>();
        mText.setValue("This is profile student fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }

}
