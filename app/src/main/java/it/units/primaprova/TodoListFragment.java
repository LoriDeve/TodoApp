package it.units.primaprova;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class TodoListFragment extends Fragment implements TodoRecyclerViewInterface {

    // Tag per Log di sistema
    private static final String TAG = "TodoListFragment";

    final static private String ORDER_KEY = "order";

    private String TASK_LIST_KEY = "TaskListKey";

    // Firebase Authentication
    private FirebaseAuth mAuth;

    // Firebase Realtime Database instance
    private FirebaseDatabase db = FirebaseDatabase.getInstance("https://primaprova-a464f-default" +
            "-rtdb.europe-west1.firebasedatabase.app");
    private DatabaseReference dbRef = db.getReference();

    // Firebase Database listener map
    private ArrayList<ChildEventListener> mListenerList = new ArrayList<>();    // test

    private DatabaseReference mSnapshot;

    //UI
    protected RecyclerView recyclerView;
    protected TodoRecyclerViewAdapter adapter;
    protected FloatingActionButton fab;
    protected RecyclerView.LayoutManager layoutManager;

    private Parcelable mTaskListState;

    private TodoTaskDao dao;
    private TodoRecyclerViewAdapter.OrderType orderType;

    public TodoListFragment() {}

    public static TodoListFragment newInstance(TodoRecyclerViewAdapter.OrderType order) {
        TodoListFragment fragment = new TodoListFragment();
        Bundle args = new Bundle();
        args.putSerializable(ORDER_KEY, order);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            orderType = (TodoRecyclerViewAdapter.OrderType) args.getSerializable(ORDER_KEY);
        } else {
            orderType = TodoRecyclerViewAdapter.OrderType.TIME;
        }
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_todo_list, container, false);
        fab = view.findViewById(R.id.add_task_fab);
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);

        // RecyclerView layout manager e adapter
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        adapter = new TodoRecyclerViewAdapter(getContext(), this, orderType);
        recyclerView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState != null) {
            mTaskListState = savedInstanceState.getParcelable(TASK_LIST_KEY);
        }
        dao = new TodoTaskDao();
        if (mAuth.getCurrentUser().getUid() != null) {
            loadData(mAuth.getCurrentUser().getUid());
        } else {
            Log.w(TAG, "loadTasks:FAIL");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mTaskListState != null) {
            layoutManager.onRestoreInstanceState(mTaskListState);
        }

        fab.setOnClickListener(addTaskButtonListener);
        Log.i(TAG, "Number of listeners:" + mListenerList.size());
    }

    @Override
    public void onPause() {
        super.onPause();
        for (int i = 0;i < mListenerList.size(); i++) {
            ChildEventListener listener = mListenerList.get(i);
            dbRef.removeEventListener(listener);    // test per rimuovere ChildEventListener da
            // nodi database
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (layoutManager != null) {
            mTaskListState = layoutManager.onSaveInstanceState();
            outState.putParcelable(TASK_LIST_KEY, mTaskListState);
        }
    }

    /* Listener pulsante "+" */
    final private View.OnClickListener addTaskButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mAuth.getCurrentUser().getUid() != null) {
                String Uid = mAuth.getCurrentUser().getUid();

                TodoTask td = new TodoTask();
                td.setUserId(Uid);  // utilizza id ottenuto da FirebaseAuth

                // pass new empty task with user id setted to current user
                AddTaskBottomSheetFragment fragment = AddTaskBottomSheetFragment.newInstance(td,
                        AddTaskBottomSheetFragment.DialogConfig.ADD);
                fragment.show(getParentFragmentManager(), fragment.getTag());
            } else {
                Log.w(TAG, "createTask:FAIL");
            }
        }
    };

    /* Metodo loadData: scarica i nuovi dati dal database registrando un ChildEventListener verso
     la Query. La query è relativa al nodo contenente tutti i task dell'utente */
    private void loadData(String Uid) {
        ChildEventListener listener =
                dao.getUserTasks(Uid).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                mSnapshot = snapshot.getRef();
                TodoTask task = snapshot.getValue(TodoTask.class);
                if (task != null) {
                    String key = task.getId();
                    Log.w(TAG, "tasksLoad:OK:NumberOfItems:" + adapter.getItemCount());
                    int position = adapter.addTodoTask(task);
                    if (position >= 0) {
                        adapter.notifyItemInserted(position);
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                mSnapshot = snapshot.getRef();
                TodoTask task = snapshot.getValue(TodoTask.class);
                if (task != null) {
                    String key = task.getId();
                    Log.i(TAG, "ChangedTask:" + task.toString());
                    int position = adapter.updateTodoTask(task);
                    if (position >= 0) {
                        adapter.notifyItemChanged(position);
                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                TodoTask task = snapshot.getValue(TodoTask.class);
                if (task != null) {
                    String key = task.getId();
                    Log.i(TAG, "DeletedTask:" + task.toString());
                    int position = adapter.deleteTodoTask(task);
                    if (position >= 0) {
                        adapter.notifyItemRemoved(position);
                        adapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "tasksLoad:FAIL");
            }
        });
        mListenerList.add(listener);
    }

    /* Implementazione metodo onTaskClick di TodoRecyclerViewInterface. Quando viene eseguita la
    chiamata da TodoRecyclerViewAdapter, mostra AddTaskBottomSheetFragment con i dati relativi al
    task cliccato */
    @Override
    public void onTaskClick(int pos, TodoTask task) {
        if (mAuth.getCurrentUser().getUid() != null) {
            String Uid = mAuth.getCurrentUser().getUid();

            // pass selected task
            AddTaskBottomSheetFragment fragment = AddTaskBottomSheetFragment.newInstance(task,
                    AddTaskBottomSheetFragment.DialogConfig.UPDATE);
            fragment.show(getParentFragmentManager(), fragment.getTag());
        } else {
            Log.w(TAG, "createTask:FAIL");
        }
    }
}