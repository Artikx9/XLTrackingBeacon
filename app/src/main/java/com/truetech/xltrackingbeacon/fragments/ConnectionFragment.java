package com.truetech.xltrackingbeacon.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.truetech.xltrackingbeacon.MainActivity;
import com.truetech.xltrackingbeacon.R;
import com.truetech.xltrackingbeacon.ScannerActivity;
import com.truetech.xltrackingbeacon.Utils.Util;
import com.truetech.xltrackingbeacon.service.TrackerService;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutionException;

import static com.truetech.xltrackingbeacon.Utils.Constant.HEX_NULL;
import static com.truetech.xltrackingbeacon.Utils.Constant.HEX_ONE;
import static com.truetech.xltrackingbeacon.Utils.Constant.MINUS_ONE;
import static com.truetech.xltrackingbeacon.Utils.Constant.SOCKET_TIMEOUT;
import static com.truetech.xltrackingbeacon.Utils.Util.getIntFromPref;
import static com.truetech.xltrackingbeacon.Utils.Util.getStringFromPref;

public class ConnectionFragment extends Fragment {

    private EditText edtCode;
    private TextView textCode;
    private ProgressBar progressBar;
    private LinearLayout lineaConnect;
    private LinearLayout lineaDisconnect;
    Intent intentService;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.connection, viewGroup, false);

        intentService = new Intent(getContext(), TrackerService.class);

        edtCode = view.findViewById(R.id.edt_code);
        Button btnScan = view.findViewById(R.id.btn_scan);
        Button btnConnect = view.findViewById(R.id.btn_connect);
        Button btnDisconnect = view.findViewById(R.id.btn_disconnect);
        progressBar = view.findViewById(R.id.progressbar);
        lineaConnect = view.findViewById(R.id.linearConnect);
        lineaDisconnect = view.findViewById(R.id.linearDisconnect);
        textCode = view.findViewById(R.id.txt_code);

        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), ScannerActivity.class));
            }
        });
        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.QR_CODE = edtCode.getText().toString();
                try {
                    CheckConnect checkConnect = new CheckConnect();
                    checkConnect.execute();
                    if(checkConnect.get()) connectTrue();
                    else connectFalse();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        btnDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getStringFromPref(R.string.key_block_password_setting) == null){
                    disconnect();
                }else if(!getStringFromPref(R.string.key_block_password_setting).equals(""))
                checkPassword();
                else
                disconnect();
            }
        });
        if(Util.SERVICE_ACTIVATE){
            lineaConnect.setVisibility(View.GONE);
            lineaDisconnect.setVisibility(View.VISIBLE);
        }
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (Util.QR_CODE != null)
            edtCode.setText(Util.QR_CODE);
    }


    class CheckConnect extends AsyncTask<Void,Void,Boolean>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            return sentQRToServer();
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    private boolean sentQRToServer(){
        InetAddress serverAddr;
        Socket socket ;
        DataOutputStream sockOut ;
        DataInputStream sockIn ;
        byte request;
        try {
            serverAddr = InetAddress.getByName(getStringFromPref(R.string.key_server));
            socket = new Socket(serverAddr, getIntFromPref(R.string.key_port));
            socket.setSoTimeout(SOCKET_TIMEOUT);
            sockOut = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            byte[] imei = Util.QR_CODE.getBytes(Charset.forName("UTF-8"));
            sockOut.writeShort(imei.length);
            sockOut.write(imei);
            sockOut.flush();
            request = MINUS_ONE;
            sockIn = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            while (true) {
                try {
                    request = sockIn.readByte();
                } catch (EOFException e) {
                    /**I catch the expiration at the end of the readable bytes*/
                }
                if (request == HEX_ONE) {
                    return  true;
                } else if (request == HEX_NULL) {
                    return  false;
                }
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return  false;
    }


    private void connectTrue(){
        getContext().setTheme(R.style.CustomAlertDialog);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setTitle(getString(R.string.alert_connection_true));
        alertDialog.setMessage(getString(R.string.alert_connection_true_text));
        alertDialog.setButton(Dialog.BUTTON_POSITIVE,getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                textCode.setText(Util.QR_CODE);
                lineaConnect.setVisibility(View.GONE);
                lineaDisconnect.setVisibility(View.VISIBLE);
                Util.SERVICE_ACTIVATE = true;
                getContext().startService(intentService);
                MainActivity.updateTab(getContext());
            }
        });
        alertDialog.show();
    }

    private void connectFalse(){
        getContext().setTheme(R.style.CustomAlertDialog);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setTitle(getString(R.string.alert_connection_false));
        alertDialog.setMessage(getString(R.string.alert_connection_false_text));
        alertDialog.setButton(Dialog.BUTTON_POSITIVE,getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        alertDialog.show();
    }

    private void disconnect(){
        getContext().setTheme(R.style.CustomAlertDialog);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setTitle(getString(R.string.alert_disconnect));
        alertDialog.setMessage(getString(R.string.alert_disconnect_text));
       alertDialog.setButton(Dialog.BUTTON_POSITIVE, getString(R.string.ok), new DialogInterface.OnClickListener() {
           @Override
           public void onClick(DialogInterface dialog, int which) {
               getContext().stopService(intentService);
               lineaConnect.setVisibility(View.VISIBLE);
               lineaDisconnect.setVisibility(View.GONE);
               Util.SERVICE_ACTIVATE = false;
               MainActivity.updateTab(getContext());
           }
       });
       alertDialog.setButton(Dialog.BUTTON_NEGATIVE, getString(R.string.cancel), new DialogInterface.OnClickListener() {
           @Override
           public void onClick(DialogInterface dialog, int which) {

           }
       });
        alertDialog.show();
    }


    private void checkPassword() {
        LayoutInflater li = LayoutInflater.from(getContext());
        View promptsView = li.inflate(R.layout.password_dialog, null);
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
        alertDialogBuilder.setView(promptsView);
        final EditText userInput = promptsView
                .findViewById(R.id.editTextDialogPassword);
        alertDialogBuilder
                .setCancelable(false)
                .setNegativeButton(this.getString(R.string.ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                if(getStringFromPref(R.string.key_block_password_setting).equals(userInput.getText().toString())) {
                                    disconnect();
                                } else {
                                    Toast.makeText(getContext(),getText(R.string.wrong_data),Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                .setPositiveButton(this.getString(R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }

                        }

                );
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
}
