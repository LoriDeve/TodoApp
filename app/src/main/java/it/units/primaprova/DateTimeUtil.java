package it.units.primaprova;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

public class DateTimeUtil {

    final static private String TAG = "DateTimeUtil";

    public DateTimeUtil(){}

    /* Metodo formatTimeDate: formatta data in modo opportuno tornando un oggetto della classe
    Date */
    public Date formatTimeDate(String timeString, String dateString) {

        Date returnDate = new Date();
        SimpleDateFormat dateFormat;
        String timeDateString = timeString + " " + dateString;
        Log.i(TAG, "UnformattedDate: " + timeDateString);

        if (timeDateString.contains("/") && timeDateString.contains(":")) {
            dateFormat = new SimpleDateFormat("HH:mm dd/MM/yyyy");
            try {
                returnDate = dateFormat.parse(timeDateString);
            } catch (ParseException e) {e.printStackTrace();}
        } else if (!timeDateString.contains("/")) {
            if (!timeDateString.contains(":")) {
                return null;
            } else {
                dateFormat = new SimpleDateFormat("HH:mm");
                try {
                    returnDate = dateFormat.parse(timeDateString);
                } catch (ParseException e) {e.printStackTrace();}
            }
        } else if (!timeDateString.contains(":")) {
            dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            try {
                returnDate = dateFormat.parse(timeDateString);
            } catch (ParseException e) {e.printStackTrace();}
        } else {
            Log.i(TAG, "VoidDate: " + returnDate);
        }
        Log.i(TAG, "FormattedDate: " + returnDate);

        return returnDate;
    }

    /* Metodo sortTaskByDate: ordina un ArrayList<TodoTask> in base alla data */
    public void sortTaskByDate(ArrayList<TodoTask> todoTaskList) {
        Collections.sort(todoTaskList, new TaskListSortByDate());
        Log.i(TAG, "sortingByDate:DONE");
    }

    /* Metodo sortTaskByDate: ordina un ArrayList<TodoTask> in base all'ora */
    public void sortTaskByTime(ArrayList<TodoTask> todoTaskList) {
        Collections.sort(todoTaskList, new TaskListSortByTime());
        Log.i(TAG, "sortingByTime:DONE");
    }

    /* Definisce Comparator da usare nella chiamata a Collections.sort(List, Comparator) in
    sortTaskByDate */
    public class TaskListSortByDate implements java.util.Comparator<TodoTask> {
        @Override
        public int compare(TodoTask o1, TodoTask o2) {
            Date date1 = formatTimeDate(o1.getTime(), o1.getDate());
            Date date2 = formatTimeDate(o2.getTime(), o2.getDate());

            Calendar cal1 = Calendar.getInstance();
            Calendar cal2 = Calendar.getInstance();
            if (date1 == null) {
                return -1;
            } else {
                cal1.setTime(date1);
            }

            if (date2 == null) {
                return 1;
            } else {
                cal2.setTime(date2);
            }

            int YY1 = cal1.get(Calendar.YEAR);
            int MM1 = cal1.get(Calendar.MONTH);
            int YY2 = cal2.get(Calendar.YEAR);
            int MM2 = cal2.get(Calendar.MONTH);

            if (YY1 < YY2) {
                return -1;
            } else if (YY1 == YY2) {
                if (MM1 < MM2) {
                    return -1;
                } else if (MM1 == MM2) {
                    return cal1.get(Calendar.DAY_OF_MONTH) - cal2.get(Calendar.DAY_OF_MONTH);
                } else return 1;
            } else return 1;
        }
    }

    /* Definisce Comparator da usare nella chiamata a Collections.sort(List, Comparator) in
    sortTaskByTime */
    public class TaskListSortByTime implements java.util.Comparator<TodoTask> {
        @Override
        public int compare(TodoTask o1, TodoTask o2) {
            Date date1 = formatTimeDate(o1.getTime(), o1.getDate());
            Date date2 = formatTimeDate(o2.getTime(), o2.getDate());

            Calendar cal1 = Calendar.getInstance();
            Calendar cal2 = Calendar.getInstance();
            if (date1 == null) {
                return -1;
            } else {
                cal1.setTime(date1);
            }

            if (date2 == null) {
                return 1;
            } else {
                cal2.setTime(date2);
            }

            int HH1 = cal1.get(Calendar.HOUR);
            int MM1 = cal1.get(Calendar.MINUTE);
            int HH2 = cal2.get(Calendar.HOUR);
            int MM2 = cal2.get(Calendar.MINUTE);

            if (HH1 < HH2) {
                return -1;
            } else if (HH1 == HH2) {
                if (MM1 < MM2) {
                    return -1;
                } else if (MM1 == MM2) {
                    return 1;
                } else return 1;
            } else return 1;
        }
    }

    /* Metodo displayFormatTime: restituisce stringa contenente l'ora formattata come HH:mm con
    zero di padding se HH o mm hanno una sola cifra */
    public String displayFormatTime (String time) {
        Date date = new Date();
        String newFormatTime;
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        try {
            date = format.parse(time);
        } catch (Exception e) {e.printStackTrace();}
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int HH = cal.get(Calendar.HOUR_OF_DAY);
        int mm = cal.get(Calendar.MINUTE);
        newFormatTime = String.format("%02d:%02d", HH, mm);
        return newFormatTime;
    }

    /* Metodo displayFormatTime: restituisce stringa contenente la data corrente formattata come
    dd/MM/yyyy */
    public String getCurrentDate() {
        Calendar cal = Calendar.getInstance();
        int YY = cal.get(Calendar.YEAR);
        int MM = cal.get(Calendar.MONTH) + 1;
        int dd = cal.get(Calendar.DAY_OF_MONTH);

        return dd + "/" + MM + "/" + YY;
    }

    /* Metodo compareDate: compara due stringhe contenenti la data. Restituisce true se le due
    date coincidono */
    public boolean compareDate(String date1, String date2) {

        if (date1 == null || date2 == null) {
            return false;
        }

        Date fDate1 = new Date();
        Date fDate2 = new Date();
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        try {
            fDate1 = format.parse(date1);
            fDate2 = format.parse(date2);
        } catch (ParseException e) {e.printStackTrace();}

        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();

        if ((fDate1 == null) || (fDate2 == null)) {
            return false;
        } else {
            cal1.setTime(fDate1);
            cal2.setTime(fDate2);
        }

        int YY1 = cal1.get(Calendar.YEAR);
        int MM1 = cal1.get(Calendar.MONTH);
        int dd1 = cal1.get(Calendar.DAY_OF_MONTH);

        int YY2 = cal2.get(Calendar.YEAR);
        int MM2 = cal2.get(Calendar.MONTH);
        int dd2 = cal2.get(Calendar.DAY_OF_MONTH);

        if (YY1 == YY2 && MM1 == MM2 && dd1 == dd2) {
            return true;
        } else return false;
    }
}
