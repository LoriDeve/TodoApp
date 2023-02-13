package it.units.primaprova;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class LogoutDialogFragment extends DialogFragment {

    final static private String LISTENER_KEY = "listener";

    private Context context;

    private LogoutDialogListenerInterface listener;

    /* Interfaccia LogoutDialogListenerInterface: dichiara due metodi, da implementare per la
    gestione della pressione dei tasti nel LogoutDialogFragment */
    public interface LogoutDialogListenerInterface extends Parcelable {
        public void onDialogPositiveClick(DialogFragment dialog);
        public void onDialogNegativeClick(DialogFragment dialog);
    }

    public static LogoutDialogFragment newInstance(LogoutDialogListenerInterface listener) {

        Bundle args = new Bundle();
        args.putParcelable(LISTENER_KEY, listener);
        LogoutDialogFragment fragment = new LogoutDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public LogoutDialogFragment() {
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        if (getArguments() != null) {
            listener = getArguments().getParcelable(LISTENER_KEY);
        } else {
            listener = new LogoutDialogListenerInterface() {
                @Override
                public void onDialogPositiveClick(DialogFragment dialog) {

                }

                @Override
                public void onDialogNegativeClick(DialogFragment dialog) {

                }

                @Override
                public int describeContents() {
                    return 0;
                }

                @Override
                public void writeToParcel(@NonNull Parcel dest, int flags) {

                }
            };
        }
        // creazione del DialogFragment
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(getResources().getString(R.string.logout_alert_title))
                .setMessage(getResources().getString(R.string.logout_alert_message))
                .setPositiveButton(getResources().getString(R.string.logout_confirm_button),
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onDialogPositiveClick(LogoutDialogFragment.this);
                    }
                })
                .setNegativeButton(getResources().getString(R.string.logout_cancel_button), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onDialogNegativeClick(LogoutDialogFragment.this);
                    }
                });
        return builder.create();
    }
}
