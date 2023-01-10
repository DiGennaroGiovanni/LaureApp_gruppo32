package it.uniba.dib.sms222332.ui.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ProfileViewModelP extends ViewModel {

    private final MutableLiveData<String> mText;

    public ProfileViewModelP() {
        mText = new MutableLiveData<>();
        mText.setValue("This is profile teacher fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }

}
