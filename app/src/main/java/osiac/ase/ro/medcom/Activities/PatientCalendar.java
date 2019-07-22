package osiac.ase.ro.medcom.Activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.timessquare.CalendarCellDecorator;
import com.squareup.timessquare.CalendarCellView;
import com.squareup.timessquare.CalendarPickerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import osiac.ase.ro.medcom.Classes.Appointment;
import osiac.ase.ro.medcom.R;

public class PatientCalendar extends AppCompatActivity {

    private CalendarPickerView datePicker;
    private FirebaseDatabase mDatabase;
    private FirebaseAuth mAuth;
    private String selectedDate;
    private ArrayList<String> stringDates;
    private ArrayList<Appointment> docAppointments;
    private Button bttnViewApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        setContentView(R.layout.activity_patient_calendar);

        bttnViewApp=findViewById(R.id.buttonViewApp);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();

        stringDates = new ArrayList<>();
        docAppointments = new ArrayList<>();

        // INIT DATE-PICKER
        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
        String today2019 = "01-01-2019";
        Date today = null;
        try {
            today = simpleDateFormat.parse(today2019);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar nextYear = Calendar.getInstance();
        nextYear.add(Calendar.YEAR, 1);
        datePicker = findViewById(R.id.calendarPat);

        // INIT CALENDAR
        datePicker.init(today, nextYear.getTime());

        // CREATE THE LIST OF DATES
        getDoctorEmailAndPopulate();

        // SET ON DATE SELECTED LISTENER
        datePicker.setOnDateSelectedListener(new CalendarPickerView.OnDateSelectedListener() {
            @Override
            public void onDateSelected(Date date) {
                Calendar calSelected = Calendar.getInstance();
                calSelected.setTime(date);
                selectedDate = new SimpleDateFormat("dd-MM-yyyy").format(calSelected.getTime());
                String id = mAuth.getUid();
                DatabaseReference ref1 = mDatabase.getReference().child("Patients").child(id).child("doctor");
                ref1.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final Integer docI = dataSnapshot.getValue(Integer.class);
                        Query ref2 = mDatabase.getReference().child("Doctors").orderByChild("accessCode");
                        ref2.addValueEventListener(new ValueEventListener() {
                            @RequiresApi(api = Build.VERSION_CODES.N)
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                    Long acc = ds.child("accessCode").getValue(Long.class);
                                    Integer accI = Math.toIntExact(acc);
                                    if (accI.equals(docI)) {
                                        String docEmail = ds.child("email").getValue(String.class);
                                        dialog(docEmail,selectedDate);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onDateUnselected(Date date) {
            }
        });

        bttnViewApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),PatientViewOwnAppointments.class);
                startActivity(intent);
            }
        });
    }

    void getDoctorEmailAndPopulate() {
        String id = mAuth.getUid();
        DatabaseReference ref1 = mDatabase.getReference().child("Patients").child(id).child("doctor");
        ref1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final Integer docI = dataSnapshot.getValue(Integer.class);
                Query ref2 = mDatabase.getReference().child("Doctors").orderByChild("accessCode");
                ref2.addValueEventListener(new ValueEventListener() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            Long acc = ds.child("accessCode").getValue(Long.class);
                            Integer accI = Math.toIntExact(acc);
                            if (accI.equals(docI)) {
                                String docEmail = ds.child("email").getValue(String.class);
                                populateWithAvailableDates(docEmail);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    void populateWithAvailableDates(final String docEmail) {
        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");

        DatabaseReference pop = mDatabase.getReference().child("Appointments");

        pop.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot ds, @Nullable String s) {
                String check = ds.child("doctorEmail").getValue(String.class);
                String checkEmail = ds.child("patientEmail").getValue(String.class);
                if (docEmail.equals(check) && checkEmail.equals("null")) {
                    String sDate = ds.child("calendarDate").getValue(String.class);
                    stringDates.add(sDate);
                    Appointment appointment = ds.getValue(Appointment.class);
                    docAppointments.add(appointment);
                }
                Date date = Calendar.getInstance().getTime();
                String current = simpleDateFormat.format(date);
                ArrayList<String> empty = new ArrayList<>();
                empty.add(current);
                if(stringDates==null){
                    makeOnlyAvailableDatesSelectable(datePicker,empty);
                }
                makeOnlyAvailableDatesSelectable(datePicker, stringDates);
                // DECORATE TIME PICKER
                List<CalendarCellDecorator> decoratorList = new ArrayList<>();
                decoratorList.add(new DateDecorator());
                datePicker.setDecorators(decoratorList);
            }


            @Override
            public void onChildChanged(@NonNull DataSnapshot ds, @Nullable String s) {
                String sDate = ds.child("calendarDate").getValue(String.class);
                String patientEmail = ds.child("patientEmail").getValue(String.class);
                if (!patientEmail.equals("null")) {
                    stringDates.remove(sDate);
                    docAppointments.remove(ds.getValue(Appointment.class));
                }
                makeOnlyAvailableDatesSelectable(datePicker, stringDates);
                // DECORATE TIME PICKER
                List<CalendarCellDecorator> decoratorList = new ArrayList<>();
                decoratorList.add(new DateDecorator());
                datePicker.setDecorators(decoratorList);
            }


            @Override
            public void onChildRemoved(@NonNull DataSnapshot ds) {
                String sDate = ds.child("calendarDate").getValue(String.class);
                stringDates.remove(sDate);
                docAppointments.remove(ds.getValue(Appointment.class));
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    void makeOnlyAvailableDatesSelectable(CalendarPickerView calendarView, final List<String> dates) {
        calendarView.setOnInvalidDateSelectedListener(null);

        calendarView.setDateSelectableFilter(new CalendarPickerView.DateSelectableFilter() {

            Calendar cal = Calendar.getInstance();

            @Override
            public boolean isDateSelectable(Date date) {
                boolean isSelectable = true;
                cal.setTime(date);
                String dateCompare = new SimpleDateFormat("dd-MM-yyyy").format(cal.getTime());

                if (dates.size() == 0) {
                    isSelectable = false;
                    return isSelectable;
                }

                for (int i = 0; i < dates.size(); i++) {
                    if (dateCompare.equals(dates.get(i))) {
                        isSelectable = true;
                        return isSelectable;
                    } else {
                        isSelectable = false;
                    }
                }
                return isSelectable;
            }
        });
    }

    private void dialog(final String docEmail, final String selectedDate) {
        TextView title = new TextView(this);
        title.setText(selectedDate);
        int color = ResourcesCompat.getColor(getResources(), R.color.colorPrimary, getTheme());
        title.setBackgroundColor(color);
        title.setPadding(15, 15, 15, 15);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Color.WHITE);
        title.setTypeface(title.getTypeface(), Typeface.BOLD);
        title.setTextSize(20);

        Context context = getApplicationContext();
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);

        TextView textView = new TextView(context);
        textView .setText("Do you wish to see the available appointment intervals from "+selectedDate+"?");
        textView .setTypeface(textView .getTypeface(), Typeface.BOLD);
        textView .setTextSize(18);
        textView .setTextColor(Color.BLACK);
        textView .setGravity(Gravity.CENTER_HORIZONTAL);
        textView .setPadding(15, 30, 15, 15);
        layout.addView(textView );

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setCustomTitle(title);
        dialog.setView(layout);

        dialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent=new Intent(getApplicationContext(),PatientViewAppointments.class);
                intent.putExtra("email",docEmail);
                intent.putExtra("date",selectedDate);
                startActivity(intent);
            }
        });

        dialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getApplicationContext(), getString(R.string.cancelPassRes), Toast.LENGTH_LONG).show();
            }
        });

        AlertDialog alertDialog = dialog.create();
        alertDialog.getWindow().getAttributes().windowAnimations = R.style.DialogTheme;
        alertDialog.show();
    }

    public class DateDecorator implements CalendarCellDecorator {
        @Override
        public void decorate(CalendarCellView cellView, Date date) {
            final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
            String sDate = simpleDateFormat.format(date);
            if (stringDates.contains(sDate)) {
                cellView.setHighlighted(true);
            }
        }
    }
}
