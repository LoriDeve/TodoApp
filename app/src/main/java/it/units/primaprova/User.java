package it.units.primaprova;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class User implements Parcelable {
    private String username;        // nome utente
    private String email;           // email utente
    private String uid;             // id utente (coincide con id in FirebaseAuth)

    public User () {}

    public User (String username, String email, String uid){
        this.username = username;
        this.email = email;
        this.uid = uid;
    }

    public String getUsername () { return this.username; }

    public String getEmail () { return this.email; }

    public String getUid () { return this.uid; }

    public void setUsername (String username) { this.username = username; }

    public void setEmail (String email) { this.email = email; }

    public void setUid (String uid) { this.uid = uid; }

    public String toString() {
        return username + "--" + email + "--" + "--"  + uid;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(username);
        dest.writeString(email);
        dest.writeString(uid);
    }

    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
        @Override
        public User createFromParcel(Parcel pc) {
            return new User(pc);
        }
        @Override
        public User[] newArray(int size) { return new User[size]; }
    };

    public User(Parcel pc){
        username = pc.readString();
        email = pc.readString();
        uid = pc.readString();
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("username", username);
        result.put("email", email);
        result.put("uid", uid);

        return result;
    }
}
