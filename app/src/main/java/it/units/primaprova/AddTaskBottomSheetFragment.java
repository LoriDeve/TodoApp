package it.units.primaprova;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;

import android.os.Bundle;

import androidx.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;

import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.Calendar;
import java.util.Date;


public class AddTaskBottomSheetFragment extends BottomSheetDialogFragment {

    // TAG per Log di sistema
    final static private String TAG = "BottomSheetFragment";

    // key per elementi Bundle nel metodo newInstance
    final static private String UID_KEY = "uid";
    final static private String CONFIG_KEY = "config";
    final static private String TASK_KEY = "task";

    // UI
    private View view;
    private DatePickerDialog datePickerDialog;
    private TimePickerDialog timePickerDialog;
    private TextView titleText;
    private Button saveButton;
    private Button pickDate;
    private Button pickTime;
    private ImageView deleteImage;
    private TextInputEditText textInputTitle;
    private TextInputEditText textInputDescr;

    private String uId;
    private String taskDate;
    private String taskTime;
    private TodoTask currentTask;
    private boolean dateChanged;
    private boolean timeChanged;

    private TodoTaskDao dao;

    public enum DialogConfig {ADD, UPDATE}

    private DialogConfig dialogConfig = DialogConfig.ADD;

    public AddTaskBottomSheetFragment() {
        // Required empty public constructor
    }

    public static AddTaskBottomSheetFragment newInstance(TodoTask task, DialogConfig config) {
        String uid = task.getUserId();
        AddTaskBottomSheetFragment fragment = new AddTaskBottomSheetFragment();
        Bundle args = new Bundle();
        args.putString(UID_KEY, uid);
        args.putSerializable(CONFIG_KEY, config);
        args.putParcelable(TASK_KEY, task);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            uId = getArguments().getString(UID_KEY);
            dialogConfig = (DialogConfig) getArguments().get(CONFIG_KEY);
            currentTask = (TodoTask) getArguments().getParcelable(TASK_KEY);
        } else {
            dialogConfig = DialogConfig.ADD;
            currentTask = new TodoTask();
        }
        dao = new TodoTaskDao();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_add_task_bottom_sheet,
                container, false);

        titleText = view.findViewById(R.id.bottom_sheet_title);
        saveButton = view.findViewById(R.id.save_task_button);
        pickDate = view.findViewById(R.id.pick_date_button);
        pickTime = view.findViewById(R.id.pick_time_button);
        deleteImage = view.findViewById(R.id.delete_task_image);
        textInputTitle = view.findViewById(R.id.new_task_title);
        textInputDescr = view.findViewById(R.id.new_task_description);

        updateUI();

        // Listeners
        deleteImage.setOnLongClickListener(deleteImageListener);
        pickDate.setOnClickListener(pickDateListener);
        pickTime.setOnClickListener(pickTimeListener);
        initDatePicker();
        initTimePicker();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    /* Listener per icona Elimina (pressione prolungata) */
    final private View.OnLongClickListener deleteImageListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            deleteCurrentTask(currentTask);
            Toast.makeText(getContext(), "Task deleted", Toast.LENGTH_SHORT).show();
            return true;
        }
    };

    /* Listener pulsante Aggiorna */
    final private View.OnClickListener updateButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            updateTask(textInputTitle.getText().toString(), textInputDescr.getText().toString());
        }
    };

    /* Listener pulsante Salva */
    final private View.OnClickListener saveButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            createTask(textInputTitle.getText().toString(), textInputDescr.getText().toString());
        }
    };

    /* Listener DatePicker */
    final private View.OnClickListener pickDateListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            datePickerDialog.show();
        }
    };

    /* Listener TimePicker */
    final private View.OnClickListener pickTimeListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            timePickerDialog.show();
        }
    };

    /* Metodo createTask: crea nuovo task a partire da task vuoto sulla base dei dati inseriti e lo
    passa ad addNewTask(TodoTask task) */
    private void createTask(String title, String descr) {
        if(!validateForm()) {
            return;
        }
        TodoTask task = new TodoTask(null, title, descr, taskTime, taskDate, uId,"normale",
                false);
        addNewTask(task);
    }

    /* Metodo updateTask: crea nuovo task sulla base dei dati task selezionato e dei nuovi dati
    inseriti e lo passa a updateCurrentTask(TodoTask task) */
    private void updateTask(String title, String descr) {
        if (!validateForm()) {
            return;
        }
        String newDate = currentTask.getDate();
        String newTime = currentTask.getTime();
        if (dateChanged) {
            newDate = taskDate;
        }

        if (timeChanged) {
            newTime = taskTime;
        }
        TodoTask task = new TodoTask(currentTask.getId(), title, descr, newTime, newDate, uId,
                "normale",
                false);
        updateCurrentTask(task);
    }

    /* Metodo addNewTask: aggiunge nuovo task nel database e attende il completamento
    dell'operazione */
    private void addNewTask(TodoTask task) {
        dao.addTask(task).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(getContext(),getResources().getString(R.string.toast_task_created),
                        Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(),
                        getResources().getString(R.string.toast_task_create_error),
                        Toast.LENGTH_SHORT).show();
                Log.w(TAG, "createTask:FAIL", e);
            }
        });
    }

    /* Metodo deleteCurrentTask: elimina task corrente dal database e attende il completamento
    dell'operazione */
    private void deleteCurrentTask(TodoTask task) {
        dao.deleteTask(task).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(getContext(), getResources().getString(R.string.toast_task_deleted),
                        Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(),
                        getResources().getString(R.string.toast_task_delete_error),
                        Toast.LENGTH_SHORT).show();
                Log.w(TAG, "deleteTask:FAIL", e);
            }
        });
    }

    /* Metodo updateCurrentTask: aggiorna il task selezionato nel database e attende il
    completamento dell'operazione */
    private void updateCurrentTask(TodoTask task) {
        dao.updateTask(task).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(getContext(), getResources().getString(R.string.toast_task_updated),
                        Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(),
                        getResources().getString(R.string.toast_task_update_error),
                        Toast.LENGTH_SHORT).show();
                Log.w(TAG, "updateTask:FAIL", e);
            }
        });
    }

    /* Metodo updateUI: aggiorna la UI in base alla modalità di visualizzazione:
    * ADD: modalità di creazione di un nuovo task. Registra saveButtonListener per il pulsante
    Salva e disattiva l'icona Elimina;
    * UPDATE: modalità di modifica del task selezionato. Se il task non è stato completato,
    registra updateButtonListener per il pulsante Salva e attiva l'icona Elimina. SE il task è
    stato completato, disattiva il pulsante Salva e disattiva tutti i campi di testo
    modificabili, rendendo possibile solo l'eliminazione del task */
    private void updateUI() {
        if (dialogConfig == DialogConfig.ADD) {
            deleteImage.setVisibility(GONE);
            saveButton.setText(getResources().getString(R.string.save_task_button));
            saveButton.setOnClickListener(saveButtonListener);
        } else {
            deleteImage.setVisibility(VISIBLE);
            titleText.setText(getResources().getString(R.string.update_task_title));
            textInputTitle.setText(currentTask.getTitle());
            textInputDescr.setText(currentTask.getDescr());
            pickDate.setText(currentTask.getDate());
            pickTime.setText(currentTask.getTime());
            saveButton.setText(getResources().getString(R.string.update_task_button));
            if(!currentTask.getCompleted()) {
                saveButton.setOnClickListener(updateButtonListener);
                saveButton.setEnabled(true);
                textInputTitle.setEnabled(true);
                textInputDescr.setEnabled(true);
                pickTime.setEnabled(true);
                pickDate.setEnabled(true);
            } else {
                deleteImage.setVisibility(VISIBLE);
                saveButton.setEnabled(false);
                textInputTitle.setEnabled(false);
                textInputDescr.setEnabled(false);
                pickTime.setEnabled(false);
                pickDate.setEnabled(false);
            }
        }
    }

    /* Metodo validateForm: esegue validazione dei campi. Restituisce false se il campo titolo è
    vuoto. Tutti gli altri campi sono facoltativi */
    private boolean validateForm() {
        boolean valid = false;
        TextInputEditText title = view.findViewById(R.id.new_task_title);
        Button date = view.findViewById(R.id.pick_time_button);
        String t = title.getText().toString();
        String d = date.getText().toString();

        // check empty title
        if(!TextUtils.isEmpty(t)) {
            valid = true;
            title.setError(null);
        } else {
            title.setError(getResources().getString(R.string.add_task_empty_title));
        }

        return valid;
    }

    /* Metodo initDatePicker: inizializza il datePicker e registra callback per la pressione del
    pulsante OK nel DatePickerDialog */
    private void initDatePicker() {
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                Date date = new Date();
                month = month + 1;
                taskDate = dayOfMonth + "/" + month + "/" + year;
                pickDate.setText(taskDate);
                dateChanged = true;
            }
        };

        // formatta data in modo corretto nel caso di update di un task
        dateChanged = false;
        Calendar cal = Calendar.getInstance();  // data di default è la data corrente
        if (dialogConfig == DialogConfig.UPDATE) {
            if (currentTask.getDate() != null) {
                Date date = new Date();
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                try {
                    date = dateFormat.parse(currentTask.getDate());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                cal.setTime(date);
            }
        }

        int yyyy = cal.get(Calendar.YEAR);
        int mm = cal.get(Calendar.MONTH);
        int dd = cal.get(Calendar.DAY_OF_MONTH);

        int style = R.style.DateTimePickerStyle;

        datePickerDialog = new DatePickerDialog(getContext(), style, dateSetListener, yyyy, mm, dd);
    }

    /* Metodo initTimePicker: inizializza il timePicker e registra callback per la pressione del
    pulsante OK nel TimePickerDialog */
    private void initTimePicker() {
        TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                taskTime = hourOfDay + ":" + minute;
                pickTime.setText(taskTime);
                timeChanged = true;
            }
        };

        // formatta data in modo corretto nel caso di update di un task
        timeChanged = false;
        Calendar cal = Calendar.getInstance();  // ora di default è l'ora corrente
        if (dialogConfig == DialogConfig.UPDATE) {
            if (currentTask.getTime() != null) {
                Date date = new Date();
                SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
                try {
                    date = dateFormat.parse(currentTask.getTime());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                cal.setTime(date);
            }
        }
        int hh = cal.get(Calendar.HOUR);
        int mm = cal.get(Calendar.MINUTE);

        Log.i(TAG, "HH:mm = " + hh + ":" + mm);

        int style = R.style.DateTimePickerStyle;

        timePickerDialog = new TimePickerDialog(getContext(), style, timeSetListener, hh, mm, true);
    }
}