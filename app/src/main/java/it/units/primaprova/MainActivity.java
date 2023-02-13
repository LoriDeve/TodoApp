package it.units.primaprova;

import static android.view.View.GONE;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import it.units.primaprova.databinding.ActivityMainBinding;


public class MainActivity extends AppCompatActivity {

    // TAG per Log di sistema
    private static final String TAG = "LoginEmailPassword";

    private ActivityMainBinding binding = null;

    public static final String key = "info";

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        mAuth = FirebaseAuth.getInstance(); // Inizializza Firebase Auth
        updateUI(null);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null) {
            reload();
        }
    }

    @Override
    protected void onResume() { super.onResume();
        Button loginButton = binding.LoginButton;
        Button registerButton = binding.RegisterButton;
        Button verifyEmail = binding.VerifyUserButtonLogin;

        loginButton.setOnClickListener(loginButtonListener);
        registerButton.setOnClickListener(RegisterButtonListener);
        verifyEmail.setOnClickListener(VerifyEmailButtonListener);
    }

    /* Listener pulsante Registrati */
    final private View.OnClickListener RegisterButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.i("LDEV","Premuto Register");
            Intent intentRegister = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intentRegister);
        }
    };

    /* Listener pulsante Login */
    final private View.OnClickListener loginButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.i("LDEV","Premuto Login");
            String email = binding.LoginEmail.getText().toString();
            String password = binding.LoginPassword.getText().toString();
            signIn(email, password);
        }
    };

    /* Listener pulsante Verifica */
    final private View.OnClickListener VerifyEmailButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            sendEmailVerification();
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    /* Metodo signIn: esegue sign in utente e attende completamento operazione */
    private void signIn(String email, String password) {
        Log.d(TAG, "signIn:" + email);
        if(!validateCredentials()) {
            return;
        }
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this,
                new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "signInWithEmailAndPassword:OK");
                    FirebaseUser user = mAuth.getCurrentUser();
                    updateUI(user);
                } else {
                    Log.w(TAG, "signInWithEmailAndPassword:FAIL", task.getException());
                    Toast.makeText(MainActivity.this,
                            "Authentication failed",
                            Toast.LENGTH_SHORT).show();
                    updateUI(null);
                }
            }
        });
    }

    /* Metodo reload: esegue refresh dei dati utente e passa l'utente a updateUI*/
    private void reload() {
        mAuth.getCurrentUser().reload().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    updateUI(mAuth.getCurrentUser());
                    Toast.makeText(MainActivity.this,
                            "Reloaded",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Log.e(TAG, "Reload", task.getException());
                    Toast.makeText(MainActivity.this,
                            " Reload Failed",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /* Metodo updateUI: aggiorna UI in base a stato di login dell'utente.
     - Loggato: e utente è verificato, crea intent per lanciare HomeActivity. Altrimenti mostra
    messaggio di email non verificata e pulsante Verifica per inviare mail di verifica.
    - Non loggato: mantiene pulsanti di Login e Registrati. */
    private void updateUI(FirebaseUser user) {
        if (user != null) {
            if (!user.isEmailVerified()) {
                binding.VerifyUserButtonLogin.setVisibility(View.VISIBLE);
                binding.VerifyEmailLoginHint.setText(getResources().getString(R.string.verification_required_login));
                binding.VerifyEmailLoginHint.setVisibility(View.VISIBLE);
            } else {
                binding.VerifyUserButtonLogin.setVisibility(View.GONE);
                binding.VerifyEmailLoginHint.setText("");
                binding.VerifyEmailLoginHint.setVisibility(View.GONE);
                Intent intentLogin = new Intent(MainActivity.this, HomeActivity.class);
                intentLogin.putExtra(key, "Messaggio da Login a Home"); // test
                startActivity(intentLogin);
                finish();
            }
        } else {
            binding.VerifyUserButtonLogin.setVisibility(View.GONE);
            binding.VerifyEmailLoginHint.setText("");
            binding.VerifyEmailLoginHint.setVisibility(View.GONE);
        }
    }

    /* Metodo sendEmailVerification: invia mail di verifica e usa listener per verificare
    completamento dell'operazione */
    private void sendEmailVerification() {
        binding.VerifyUserButtonLogin.setEnabled(false);
        final FirebaseUser user = mAuth.getCurrentUser();
        user.sendEmailVerification().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        binding.VerifyUserButtonLogin.setEnabled(true);
                        if(task.isSuccessful()) {
                            Toast.makeText(MainActivity.this,
                                    getResources().getString(R.string.verification_sent_success_toast)
                                            + " " + user.getEmail(),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e(TAG, "sendEmailVerification", task.getException());
                            Toast.makeText(MainActivity.this,
                                    getResources().getString(R.string.verification_sent_error_toast),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    /* Metodo validateCredentials: esegue validazione credenziali in base alla lunghezza e
    imposta messaggio d'errore. Se password ha meno di 6 caratteri o campo email vuoto,
    ritorna valore false */
    private boolean validateCredentials() {
        boolean valid = true;
        String password = binding.LoginPassword.getText().toString();
        String email = binding.LoginEmail.getText().toString();
        if (password.length() < 6) {
            valid = false;
            binding.LoginPassword.setError(getResources().getString(R.string.new_password_short));
            binding.LoginPassword.setText("");
        }
        if  (email.length() == 0) {
            binding.LoginEmail.setError(getResources().getString(R.string.empty_email));
        }
        return valid;
    }

}