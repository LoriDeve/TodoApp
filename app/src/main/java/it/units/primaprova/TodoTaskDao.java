package it.units.primaprova;

import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.HashMap;
import java.util.Map;

public class TodoTaskDao {

    private DatabaseReference dbRef;

    private DateTimeUtil dateTimeUtil = new DateTimeUtil();

    final static private String TAG = "TodoTaskDao";

    public TodoTaskDao() {
        FirebaseDatabase db = FirebaseDatabase.getInstance("https://primaprova-a464f-default" +
                "-rtdb.europe-west1.firebasedatabase.app");
        dbRef = db.getReference();
    }

    /* Metodo addTask: aggiunge nuovo task al nodo "/users-task/userId/" dove userId è id
    utente e corrisponde a id usato in FirebaseAuth. Prima viene aggiunto un nodo vuoto usando
    push(), poi la key relativa al nodo viene usata come parametro per costruire argomento di
    updateChildren(...). Restituisce un oggetto Task sul quale può essere registrato un listener. */
    public Task<Void> addTask(TodoTask task) {
        String key = dbRef.child("task").push().getKey();
        Map<String, Object> taskValues = task.toMap();
        taskValues.put("id", key);

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/users-task/" + task.getUserId() + "/" + key, taskValues);

        return dbRef.updateChildren(childUpdates);
    }

    /* Metodo getUserTasks: ritorna una query relativa al nodo "/users-task/userId" */
    public Query getUserTasks(String Uid) {
        Query query =
                dbRef.child("users-task").child(Uid).orderByKey();
        return query;
    }

    /* Metodo completeTask: modifica il parametro "completed" di un task impostandolo a true
    mediante il metodo setValue(true). Ritorna un oggetto Task sul quale può essere registrato un
     listener. */
    public Task<Void> completeTask(TodoTask task) {
        String key = task.getId();
        String uId = task.getUserId();
        return dbRef.child("users-task").child(uId).child(key).child("completed").setValue(true);
    }

    /* Metodo updateTask: aggiorna tutti i parametri di un task nel database con i nuovi valori
    usando il metodo updateChildren(...). Restituisce un oggetto Task sul quale può essere
    registrato un listener.*/
    public Task<Void> updateTask(TodoTask task) {
        String key = task.getId();
        String uId = task.getUserId();
        Map<String, Object> taskValues = task.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/users-task/" + uId + "/" + key, taskValues);

        return dbRef.updateChildren(childUpdates);
    }

    /* Metodo deleteTask: elimina un task dal database impostando il valore del nodo contenente
    il task a null. Restituisce un oggetto Task sul quale può essere registrato un listener. */
    public Task<Void> deleteTask(TodoTask task) {
        String key = task.getId();
        String uId = task.getUserId();
        return dbRef.child("users-task").child(uId).child(key).setValue(null);
    }
}
