package it.units.primaprova;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;

public class TodoRecyclerViewAdapter extends RecyclerView.Adapter<TodoRecyclerViewAdapter.TodoViewHolder> {

    private Context context;

    private final TodoRecyclerViewInterface recyclerViewInterface;

    // Tag per Log di sistema
    final static private String TAG = "TodoRecyclerViewAdapter";

    // Lista dei task ottenuti dal database
    private ArrayList<TodoTask> taskList = new ArrayList<>();

    // tipo di ordinamento
    public enum OrderType {DATE, TIME}

    private OrderType orderMethod = OrderType.DATE;

    public TodoRecyclerViewAdapter(Context context, TodoRecyclerViewInterface rvIf,
                                   OrderType orderMethod) {
        this.context = context;
        this.orderMethod = orderMethod;
        this.recyclerViewInterface = rvIf;
    }

    // ViewHolder per elemento del RecyclerView
    public static class TodoViewHolder extends RecyclerView.ViewHolder {

        public TextView title_text;
        public TextView descr_text;
        public TextView time_text;
        public ImageView compl_image;


        public TodoViewHolder(View view, TodoRecyclerViewInterface rvIf, ArrayList<TodoTask> list) {
            super(view);
            title_text = view.findViewById(R.id.task_title);
            descr_text = view.findViewById(R.id.task_descr);
            time_text = view.findViewById(R.id.task_time);
            compl_image = view.findViewById(R.id.completed_imageView);

            // alla pressione di un elemento del RecyclerView, chiama OnTaskClick passando il
            // task corrispondente nella lista
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (rvIf != null) {
                        int pos = getAdapterPosition();
                        if (pos != RecyclerView.NO_POSITION) {
                            rvIf.onTaskClick(pos, list.get(pos));
                        }
                    }
                }
            });
        }

        public void setTimeText(String time_text) {
                this.time_text.setText(time_text);
        }

        public void setComplImage(int compl_image) {
            this.compl_image.setImageResource(compl_image);
        }

        public void setDescrText(String descr_text) {
            this.descr_text.setText(descr_text);
        }

        public void setTitleText(String title_text) {
            this.title_text.setText(title_text);
        }
    }

    @NonNull
    @Override
    public TodoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.todolist_row_item,
                parent, false);
        Log.d(TAG, "ViewHolderCreated");
        return new TodoViewHolder(view, recyclerViewInterface, taskList);
    }

    @Override
    public void onBindViewHolder(@NonNull TodoViewHolder holder, int position) {

        TodoTask task = taskList.get(position);
        DateTimeUtil dateTimeUtil = new DateTimeUtil();
        Boolean done = task.getCompleted();

        // manage completed icon
        if (done) {
            holder.setComplImage(R.drawable.ic_baseline_task_alt_24);
        }
        holder.compl_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!done){
                    TodoTaskDao dao = new TodoTaskDao();
                    dao.completeTask(task).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            holder.setComplImage(R.drawable.ic_baseline_task_alt_24);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "CompleteTask:FAIL");
                        }
                    });
                }
            }
        });

        // binding dei dati ai campi dell'elemento del RecyclerView
        holder.setTitleText(task.getTitle());
        holder.setDescrText(task.getDescr());
        String dateTime = null;
        if (orderMethod == OrderType.DATE) {
            dateTime = task.getDate();
        } else {
            if (task.getTime() != null) {
                dateTime = dateTimeUtil.displayFormatTime(task.getTime());
            }
        }
        if (dateTime != null) {
            holder.setTimeText(dateTime);
        } else {
            holder.setTimeText("");
        }

        Log.d(TAG, "ElementSet: " + position);
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    /* Metodo addTodoTask: aggiunge alla lista dei task il task ricevuto come parametro in base
    al tipo di ordinamento:
    * Ordinamento == TIME => ci si trova nella schermata "Oggi". In questo caso vengono
    aggiunti alla lista solo gli elementi che hanno una data corrispondente alla data odierna o
    null;
    * Ordinamento == DATE => ci si trova nella schermata "Prossimi". In questo caso vengono
    aggiunti alla lista tutti i task ricevuti;
    Il metodo ritorna un intero >= 0 corrispondente alla posizione nel RecyclerView del task
    aggiunto. Se non è stato aggiunto nessun elemento, ritorna -1. */
    public int addTodoTask(TodoTask task) {

        String key = task.getId();
        int index;
        DateTimeUtil dateTimeUtil = new DateTimeUtil();
        Log.i(TAG, "Date to be compared:" + task.getDate() + "  " + dateTimeUtil.getCurrentDate());

        if (orderMethod == OrderType.TIME) {
            if (dateTimeUtil.compareDate(task.getDate(), dateTimeUtil.getCurrentDate()) || task.getDate() == null) {
                taskList.add(task);
            } else {
                return -1;
            }
        } else {
            taskList.add(task);
        }

        if (orderMethod == OrderType.DATE) {
            dateTimeUtil.sortTaskByDate(taskList);
        } else {
            dateTimeUtil.sortTaskByTime(taskList);
        }

        index = findPosition(key);
        Log.i(TAG, "IndexAdded:" + index);
        Log.i(TAG, "TaskList:" + taskList.toString());

        return index;
    }

    /* Metodo updateTodoTask: aggiorna task nella lista se questo è presente. Ritorna un intero >=
     0 corrispondente alla posizione del task aggiornato. Se l'elemento non esiste, ritorna -1. */
    public int updateTodoTask(TodoTask task) {

        String key = task.getId();

        if (existsInList(key)) {
            int pos = findPosition(key);
            Log.i(TAG, "TaskList:" + taskList.toString());
            taskList.set(pos, task);
            return pos;
        } else {
            return -1;
        }
    }

    /* Metodo deleteTodoTask: rimuove task dalla lista se questo è presente. Ritorna un intero >=
     0 corrispondente alla posizione del task aggiornato. Se l'elemento non esiste, ritorna -1. */
    public int deleteTodoTask(TodoTask task) {

        String key = task.getId();

        if (existsInList(key)) {
            int pos = findPosition(key);
            Log.i(TAG, "TaskList:" + taskList.toString());
            taskList.remove(pos);
            return pos;
        } else {
            return -1;
        }

    }

    /* Metodo findPosition: ritorna la posizione di un task presente nella lista. Se il task non
    esiste, ritorna la dimensione della lista. */
    private int findPosition(String key) {
        int index = 0;
        String id;
        for (int i = 0; i < taskList.size(); i++) {
            id = taskList.get(i).getId();
            if (id.equals(key)) {
                break;
            } else {
                index++;
            }
        }
        return index;
    }

    /* Metodo existsInList: ricerca se un task esiste nella lista sulla base dell'id. Se il task
    è presente ritorna true, altrimenti false. */
    private boolean existsInList(String key) {
        boolean exist = false;
        for (TodoTask task : taskList) {
            if (task.getId().equals(key)) {
                exist = true;
            }
        }
        return exist;
    }
}
