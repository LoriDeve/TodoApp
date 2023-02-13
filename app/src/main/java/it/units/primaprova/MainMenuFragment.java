package it.units.primaprova;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainMenuFragment extends Fragment implements LogoutDialogFragment.LogoutDialogListenerInterface {

    private String TAG = "MainMenuFragment";

    final static private String LISTENER_KEY = "listener";

    // Firebase Authentication
    private FirebaseAuth mAuth;

    // Firebase Realtime Database instance
    private FirebaseDatabase db = FirebaseDatabase.getInstance("https://primaprova-a464f-default" +
            "-rtdb.europe-west1.firebasedatabase.app");
    private DatabaseReference dbRef = db.getReference();

    // UI
    private TextView logoutText;
    private TextView usernameText;
    private TextView emailText;

    private String email;
    private String username;
    private String uid;

    private HomeLogoutListenerInterface listener;

    LogoutDialogFragment.LogoutDialogListenerInterface logoutDialogListenerInterface = this;

    /* Interfaccia HomeLogoutListenerInterface: dichiara un metodo, da implementare per la gestione
     del logout */
    public interface HomeLogoutListenerInterface extends Parcelable {
        public void OnLogoutListener(MainMenuFragment fragment);
    }

    public MainMenuFragment() {
        // Required empty public constructor
    }

    public static MainMenuFragment newInstance(HomeLogoutListenerInterface listener) {
        MainMenuFragment fragment = new MainMenuFragment();
        Bundle args = new Bundle();
        args.putParcelable(LISTENER_KEY, listener);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            listener = getArguments().getParcelable(LISTENER_KEY);
        } else {
            listener = new HomeLogoutListenerInterface() {
                @Override
                public void OnLogoutListener(MainMenuFragment fragment) {

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
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_main_menu, container, false);
        logoutText = view.findViewById(R.id.logout_text);
        usernameText = view.findViewById(R.id.username_text);
        emailText = view.findViewById(R.id.user_email_text);
        logoutText.setOnClickListener(logoutTextListener);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(mAuth.getCurrentUser().getUid() != null) {
            Log.i(TAG, "CurrentUserId:" + mAuth.getCurrentUser().getUid());
            loadUserData(mAuth.getCurrentUser().getUid());
        } else {
            email = getResources().getString(R.string.resource_not_found);
            username = getResources().getString(R.string.resource_not_found);
            uid = getResources().getString(R.string.resource_not_found);
            Toast.makeText(getContext(), "Failed to load user data", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "CurrentUser: null");
        }
        Log.i(TAG, "CurrentUser:" + email + "-" + username);
        //updateUI();
    }

    /* Listener scritta Logout */
    final private View.OnClickListener logoutTextListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            LogoutDialogFragment fragment = LogoutDialogFragment.newInstance(
                    logoutDialogListenerInterface);
            fragment.show(getParentFragmentManager(), "Logout");    // mostra DialogFragment
        }
    };

    /* Metodo loadUserData: scarica i dati dell'utente dal database e attende il completamento
    dell'operazione. Al completamento, chiama updateUI(User user) per aggiornare la UI */
    private void loadUserData(String uId) {
        dbRef.child("users").child(uId).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                User user = new User();
                if(!task.isSuccessful()) {
                    Log.e(TAG,"LoadUser:FAIL", task.getException());
                    user.setUsername(getResources().getString(R.string.resource_not_found));
                    user.setEmail(getResources().getString(R.string.resource_not_found));
                    user.setUid(getResources().getString(R.string.resource_not_found));

                } else {
                    user = task.getResult().getValue(User.class);
                    Log.i(TAG,"LoadUser:OK" + user);
                }
                updateUI(user);
            }
        });
    }

    /* Metodo updateUI: aggiorna i campi della UI con i dati dell'utente ricevuto come parametro. */
    private void updateUI(User user) {
        usernameText.setText(user.getUsername());
        emailText.setText(user.getEmail());
    }

    /* IMPLEMENTAZIONE LogoutDialogListenerInterface */

    /* Implementazione metodo onDialogPositiveListener di LogoutDialogListenerInterface. Quando
    viene ricevuta la notifica da LogoutDialogFragment del logout, chiama OnLogoutListener per
    notificare HomeActivity del logout. */
    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        FirebaseAuth.getInstance().signOut();   // logout
        listener.OnLogoutListener(MainMenuFragment.this);
    }

    /* Implementazione metodo onDialogPositiveListener di LogoutDialogListenerInterface. In
    questo caso il LogoutDialog è stato cancellato, e nessuna azione viene eseguita. */
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

    Parcelable.Creator<MainMenuFragment> CREATOR = new Parcelable.Creator<MainMenuFragment>() {
        @Override
        public MainMenuFragment createFromParcel(Parcel source) {
            return null;
        }

        @Override
        public MainMenuFragment[] newArray(int size) {
            return new MainMenuFragment[0];
        }
    };
}