package com.newdimension.rnr_cinematic.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.newdimension.rnr_cinematic.R;

public class CinematicFragment extends Fragment {

    public static CinematicFragment newInstance() { return new CinematicFragment(); }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.rnr_cinematic_fragment, container, false);
    }
}
