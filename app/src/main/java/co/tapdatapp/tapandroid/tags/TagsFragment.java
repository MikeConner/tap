package co.tapdatapp.tapandroid.tags;

import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import co.tapdatapp.tapandroid.R;

public class TagsFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_tags, container, false);
    }



}
