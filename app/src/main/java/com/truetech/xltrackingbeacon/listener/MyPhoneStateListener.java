package com.truetech.xltrackingbeacon.listener;

import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;

public class MyPhoneStateListener extends PhoneStateListener {
    public static volatile int strength=0;
    @Override
    public void onSignalStrengthsChanged(SignalStrength signalStrength) {
        super.onSignalStrengthsChanged(signalStrength);
        int signal=signalStrength.getGsmSignalStrength();
        if (signal==99) strength=0;
        else strength=signal;
    }
}
