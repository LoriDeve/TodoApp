package it.units.primaprova;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

public class TodoTask implements Parcelable {

    private String id;              // id task
    private String title;           // titolo task
    private String descr;           // descrizione task
    private String time;            // ora task (HH:mm)
    private String date;            // data task (dd/MM/yyyy)
    private String userId;          // autore task (user id)
    private String category;        // categoria (Inizialmente prevista, NON USATA NEL PROGETTO)
    private Boolean completed;      // task completato

    public TodoTask(){}

    public TodoTask(String id, String title, String descr, String time, String date, String userId,
                    String category, boolean completed){
        this.id = id;
        this.title = title;
        this.time = time;
        this.date = date;
        this.descr = descr;
        this.userId = userId;
        this.category = category;
        this.completed = completed;
    };

    // setter
    public void setDescr(String descr) {
        this.descr = descr;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setTime(String time) { this.time = time; }

    public void setDate(String date) { this.date = date; }

    public void setCategory(String category) { this.category = category; }

    public void setTitle(String title) { this.title = title; }

    public void setCompleted(Boolean completed) { this.completed = completed; }

    // getter
    public String getTime() {
        return time;
    }

    public String getDate() { return date; }

    public String getDescr() {
        return descr;
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getCategory() { return category; }

    public String getTitle() { return title; }

    public Boolean getCompleted() { return completed; }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        dest.writeString(descr);
        dest.writeString(time);
        dest.writeString(date);
        dest.writeString(userId);
        dest.writeString(category);
        dest.writeInt(completed ? 1:0);
    }

    public static final Parcelable.Creator<TodoTask> CREATOR = new Parcelable.Creator<TodoTask>() {
        @Override
        public TodoTask createFromParcel(Parcel pc) {
            return new TodoTask(pc);
        }
        @Override
        public TodoTask[] newArray(int size){
            return new TodoTask[size];
        }
    };

    public TodoTask(Parcel pc){
      id = pc.readString();
      title = pc.readString();
      descr = pc.readString();
      time = pc.readString();
      date = pc.readString();
      userId = pc.readString();
      category = pc.readString();
      completed = pc.readInt() != 0;
    }

    public String toString() {
        return (id + "-" + title + "-" + time + "-" + date
                + "-" + descr + "-" + userId + "-" + category
                + "-" + completed);
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("title", title);
        result.put("time",time);
        result.put("date", date);
        result.put("descr", descr);
        result.put("userId", userId);
        result.put("category", category);
        result.put("completed", completed);

        return result;
    }
}
