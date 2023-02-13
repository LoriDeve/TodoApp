package it.units.primaprova;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.util.ArrayList;
import java.util.List;

import it.units.primaprova.databinding.ActivityHomeBinding;

public class HomeActivity extends AppCompatActivity implements MainMenuFragment.HomeLogoutListenerInterface {

    // Tag per Log di sistema
    private static final String TAG = "BottomMenuTransition:";

    // Gestione del backstack dei fragment. Se false, la gestione è disabilitata
    final static private boolean manageBackstack = false;

    private ActivityHomeBinding bindingHome = null;

    private FragmentManager fragmentManager;

    private int navigationItemIdPrev = R.id.todaySummary;

    private int navigationItemId = R.id.todaySummary;

    private List<String> stackList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindingHome = ActivityHomeBinding.inflate(getLayoutInflater());
        View view = bindingHome.getRoot();
        setContentView(view);

        // Messaggio da intent
        String intentMsg = getIntent().getStringExtra(MainActivity.key);
        Log.i("LDEV", intentMsg);

        // fragment transaction
        fragmentManager = getSupportFragmentManager();
        if (savedInstanceState == null) {
            fragmentManager.beginTransaction()
                    .setReorderingAllowed(true)
                    .add(R.id.fragmentContainerView, TodoListFragment.class, null)
                    .commit();
        }
        navigationItemIdPrev = R.id.todaySummary;
        navigationItemId = R.id.todaySummary;

        BottomNavigationView bottomNavigationView = bindingHome.bottomNavigationView;
        bottomNavigationView.setOnItemSelectedListener(itemSelectedListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bindingHome = null;
    }

    /* Listener BottomNavigationView */
    final private NavigationBarView.OnItemSelectedListener itemSelectedListener =
            new NavigationBarView.OnItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    navigationItemIdPrev = navigationItemId;
                    navigationItemId = item.getItemId();
                    String prevStack = navigationItemIdToString(navigationItemIdPrev);
                    String currentStack = navigationItemIdToString(navigationItemId);
                    Log.d(TAG, "Previous:" + prevStack + " Current:" + currentStack);
                    updateUI(navigationItemId, currentStack, prevStack);
                    return true;
                }
            };

    /* Metodo update UI: aggiorna UI con nuovo fragment in base a elemento  selezionato nel
    BottomNavigationView */
    private void updateUI(int navigationItemId, String newStack, String prevStack) {

        /* Gestione backstack fragment. Se manageBackstack = true, aggiunge fragment a backstack
        se questo non è già presente. Se manageBackstack = false, non aggiunge a backstack e ad
        ogni cambiamento di BottomNavigationView il vecchio fragment viene distrutto e il nuovo
        viene creato.
        -- !! NOTA !! : facendo il restore del fragment il recyclerview non
        mantiene gli elementi caricati in precedenza, e viene mostrata una lista vuota. Creando il
        fragment da zero si forza il ricaricamento degli elementi da Firebase e la lista viene
        mostrata correttamente. */

        if (manageBackstack) {
            fragmentManager.saveBackStack(prevStack);
            int backStackCount = fragmentManager.getBackStackEntryCount();
            Log.d(TAG,"CurrentlyInBackstack:" + backStackCount);
        }

        if (!stackList.contains(newStack) || !manageBackstack) {
            stackList.add(newStack);
            Fragment fragment = selectFragment(navigationItemId);           // new fragment
            fragmentManager.beginTransaction()                              // transaction
                    .setReorderingAllowed(true)
                    .replace(R.id.fragmentContainerView, fragment, newStack)
                    .addToBackStack(newStack)
                    .commit();
            Log.d(TAG, "NewAddedToStack:" + newStack);
        }
        /* Restore del backstack */
        if (manageBackstack) {
            fragmentManager.restoreBackStack(newStack);                         // restore backstack
        }
    }

    /* Converte id dell'elemento del BottomNavigationView in stringa */
    private String navigationItemIdToString (int id) {
        String string = getResources().getString(R.string.menu_today_item);
        if (id == R.id.upcomingSummary) {
            string = getResources().getString(R.string.menu_next_item);
        } else if (id == R.id.settings) {
            string = getResources().getString(R.string.menu_settings_item);
        }
        return string;
    }

    /* Metodo selectFragment: sceglie quale fragment mostrare in base al valore del navigation
    item selezionato nel BottomNavigationView */
    private Fragment selectFragment (int navigationItemId) {
        Fragment fragment = TodoListFragment.newInstance(TodoRecyclerViewAdapter.OrderType.TIME);
        if (navigationItemId == R.id.upcomingSummary) {
            fragment = TodoListFragment.newInstance(TodoRecyclerViewAdapter.OrderType.DATE);
        } else if (navigationItemId == R.id.settings) {
            fragment = MainMenuFragment.newInstance(this);
        }

        return fragment;
    }

    /* IMPLEMENTAZIONE HomeLogoutListenerInterface */

    /* Implementazione metodo onLogoutListener di HomeLogoutListenerInterface. Quando viene
    eseguita la notifica da MainMenuFragment del logout, crea intent e lancia MainActivity. */
    @Override
    public void OnLogoutListener(MainMenuFragment fragment) {
        Intent intentLogout = new Intent(HomeActivity.this, MainActivity.class);
        intentLogout.putExtra("info", "User logged out");
        startActivity(intentLogout);
        finish();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {

    }

    Parcelable.Creator<HomeActivity> CREATOR = new Parcelable.Creator<HomeActivity>() {
        @Override
        public HomeActivity createFromParcel(Parcel source) {
            return null;
        }

        @Override
        public HomeActivity[] newArray(int size) {
            return new HomeActivity[0];
        }
    };
}