package it.units.primaprova;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import it.units.primaprova.databinding.ActivityRegisterBinding;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding = null;

    // Tag per Log di sistema
    private static final String TAG = "CreateEmailPassword";

    // firebase instance object
    private FirebaseAuth mAuth;

    private FirebaseDatabase dataBase = FirebaseDatabase.getInstance("https://primaprova-a464f-default" +
            "-rtdb.europe-west1.firebasedatabase.app");

    private DatabaseReference dbRef = dataBase.getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        mAuth = FirebaseAuth.getInstance(); // inizializza Firebase Auth
        updateUI(null);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Button createNewUserButton = binding.createUserButton;
        Button verifyUserButton = binding.verifyUserButton;
        createNewUserButton.setOnClickListener(createNewUserButtonListener);
        verifyUserButton.setOnClickListener(verifyUserButtonListener);
        updateUI(mAuth.getCurrentUser());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    /* Listener pulsante Registrati */
    final private View.OnClickListener createNewUserButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String email = binding.NewEmail.getText().toString();
            String password = binding.NewPassword.getText().toString();
            String username = binding.NewUsername.getText().toString();
            createAccount(email, password, username);
        }
    };

    /* Listener pulsante Verifica */
    final private View.OnClickListener verifyUserButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            sendEmailVerification();
        }
    };

    /* Metodo createAccount: esegue registrazione dell'utente con Email e Password e attende il
    completamento dell'operazione */
    private void createAccount(String email, String password, String username) {
        Log.i(TAG, "newAccountCreate:"+email);
        if(!validateForm()){
            return;
        }
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this,
                new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.i(TAG,"createUserWithEmailAndPassword:OK");
                    FirebaseUser user = mAuth.getCurrentUser();
                    String userId = user.getUid();
                    addNewUser(username, email, userId);
                    updateUI(user);
                    Log.i(TAG,user.getDisplayName() + "/" + userId);
                } else {
                    Log.w(TAG, "createUserWithEmailAndPassword:FAIL", task.getException());
                    Toast.makeText(RegisterActivity.this,
                            "Error during creation of the account",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /* Metodo sendEmailVerification: invia email di verifica e attende il completamento
    dell'operazione */
    private void sendEmailVerification() {
        binding.verifyUserButton.setEnabled(false);
        final FirebaseUser user = mAuth.getCurrentUser();
        user.sendEmailVerification().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                binding.verifyUserButton.setEnabled(true);
                if(task.isSuccessful()) {
                    Toast.makeText(RegisterActivity.this,
                            getResources().getString(R.string.verification_sent_success_toast)
                                    + user.getEmail(),
                            Toast.LENGTH_SHORT).show();
                    FirebaseAuth.getInstance().signOut();
                } else {
                    Log.e(TAG, "sendEmailVerification", task.getException());
                    Toast.makeText(RegisterActivity.this,
                            getResources().getString(R.string.verification_sent_error_toast),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /* Metodo validateForm: esegue la validazione dei dati nel form in base alla lunghezza dei
    campi e al match della password e imposta il relativo messaggio di errore. Se il campo email
    è vuoto || username è vuoto || la password ha meno di 6 caratteri || le password non
    coincidono, restituisce false */
    private boolean validateForm() {
        boolean valid = true;
        String newPassword = binding.NewPassword.getText().toString();
        String repeatPassword = binding.RepeatPassword.getText().toString();
        String email = binding.NewEmail.getText().toString();
        String username = binding.NewUsername.getText().toString();

        // check if email is empty
        if(TextUtils.isEmpty(email)) {
            binding.NewEmail.setError(getResources().getString(R.string.valid_email));
            valid = false;
        } else {
            binding.NewEmail.setError(null);
        }

        // check if username is empty
        if(TextUtils.isEmpty(username)) {
            binding.NewUsername.setError(getResources().getString(R.string.valid_username));
            valid = false;
        } else {
            binding.NewUsername.setError(null);
        }

        // check if passwords match
        if ((newPassword.length() < 6)) {
            valid = false;
            binding.NewPassword.setError(getResources().getString(R.string.new_password_short));
            binding.NewPassword.setText("");
        } else if (!newPassword.equals(repeatPassword)){
            valid = false;
            binding.RepeatPassword.setError(getResources().getString(R.string.password_not_match));
            binding.RepeatPassword.setText("");
        } else {
            binding.NewPassword.setError(null);
            binding.RepeatPassword.setError(null);
        }
        return valid;
    }

    /* Metodo addNewUser: aggiunge nuovo record User nel database come child del nodo "users" */
    private void addNewUser(String username, String email, String uid) {
        User user = new User(username, email, uid);
        dbRef.child("users").child(uid).setValue(user);
        Log.i(TAG, "UserRecordAdded");
    }

    /* Metodo updateUI: aggiorna UI in base allo stato dell'utente:
    * Utente non registrato: abilita pulsante Registrati e disabilita Verifica;
    * Utente registrato && mail non verificata: abilita pulsante Verifica, disabilita Registrati
    e mostra messaggio di controllo della casella di posta;
    * Utente registrato && mail verificata: abilita pulsante Verifica, disabilita Registrati
    e mostra messaggio di procedere con login; */
    private void updateUI(FirebaseUser user) {
        if (user == null) {
            binding.createUserButton.setEnabled(true);
            binding.NewEmail.setEnabled(true);
            binding.NewPassword.setEnabled(true);
            binding.RepeatPassword.setEnabled(true);
            binding.NewUsername.setEnabled(true);
            binding.verifyUserButton.setEnabled(false);
            binding.registerInfoMessage.setText("");
        } else {
            if (!user.isEmailVerified()) {
                binding.createUserButton.setEnabled(false);
                binding.NewEmail.setEnabled(false);
                binding.NewPassword.setEnabled(false);
                binding.RepeatPassword.setEnabled(false);
                binding.NewUsername.setEnabled(false);
                binding.verifyUserButton.setEnabled(true);
                binding.registerInfoMessage.setText(getResources().getString(R.string.verification_sent_hint));
            } else {
                binding.createUserButton.setEnabled(false);
                binding.NewEmail.setEnabled(false);
                binding.NewPassword.setEnabled(false);
                binding.RepeatPassword.setEnabled(false);
                binding.NewUsername.setEnabled(false);
                binding.verifyUserButton.setEnabled(true);
                binding.registerInfoMessage.setText(getResources().getString(R.string.email_verified_hint));
            }
        }
    }
}