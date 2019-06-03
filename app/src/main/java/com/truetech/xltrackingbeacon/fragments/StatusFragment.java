package com.truetech.xltrackingbeacon.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.truetech.xltrackingbeacon.R;

import static com.truetech.xltrackingbeacon.Utils.Util.COUNT_SATELLITES;
import static com.truetech.xltrackingbeacon.Utils.Util.QR_CODE;
import static com.truetech.xltrackingbeacon.Utils.Util.SERVICE_ACTIVATE;
import static com.truetech.xltrackingbeacon.Utils.Util.SPEED;

public class StatusFragment extends Fragment {


    private TextView statusText;
    private TextView statusCode;
    private TextView statusSatellites;
    private TextView statusSpeed;
    private LinearLayout disconnectView;
    private LinearLayout connectView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.status, viewGroup, false);
        statusText = view.findViewById(R.id.statusText);
        statusCode = view.findViewById(R.id.codeText);
        statusSatellites = view.findViewById(R.id.countView);
        statusSpeed = view.findViewById(R.id.speedText);
        disconnectView = view.findViewById(R.id.disconnectView);
        connectView = view.findViewById(R.id.connectView);
        return view;
    }



    public void updateScreen(){
        statusCode.setText(QR_CODE);
        statusSatellites.setText(String.valueOf(COUNT_SATELLITES));
        statusSpeed.setText(String.valueOf(SPEED));
        if(SERVICE_ACTIVATE) {
            connectView.setVisibility(View.VISIBLE);
            disconnectView.setVisibility(View.GONE);
            statusText.setText(getString(R.string.status_yes));
        } else {
            statusText.setText(getString(R.string.status_no));
            connectView.setVisibility(View.GONE);
            disconnectView.setVisibility(View.VISIBLE);
        }
    }




}
