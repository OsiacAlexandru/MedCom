package osiac.ase.ro.medcom.Activities;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.timessquare.CalendarPickerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import osiac.ase.ro.medcom.Classes.Appointment;
import osiac.ase.ro.medcom.Classes.AppointmentAdapter;
import osiac.ase.ro.medcom.R;

public class DoctorCalendar extends AppCompatActivity implements AppointmentAdapter.OnItemClickListener{

    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private String selectedDate;
    private List<Appointment> appointments;
    private AppointmentAdapter adapter;
    private Integer position;
    private Integer patientPos ;
    private List<String> emails;
    private ValueEventListener mDBListner;
    private List<Date> dates;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        setContentView(R.layout.activity_doctor_calendar);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();

        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");

        String firstDayOf2019 = "01-01-2019";
        Date firstOfTheYear= null;
        try {
            firstOfTheYear = simpleDateFormat.parse(firstDayOf2019);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar nextYear = Calendar.getInstance();
        nextYear.add(Calendar.YEAR, 1);

        final CalendarPickerView datePicker = findViewById(R.id.calendar);

        datePicker.setOnInvalidDateSelectedListener(null);

        datePicker.setDateSelectableFilter(new CalendarPickerView.DateSelectableFilter() {

            Calendar cal=Calendar.getInstance();
            @Override
            public boolean isDateSelectable(Date date) {
                boolean isSelectable=true;
                cal.setTime(date);
                int dayOfWeek=cal.get(Calendar.DAY_OF_WEEK);

                if(dayOfWeek==Calendar.SATURDAY || dayOfWeek==Calendar.SUNDAY){
                    isSelectable=false;
                }
                return isSelectable;
            }
        });

        datePicker.setOnDateSelectedListener(new CalendarPickerView.OnDateSelectedListener() {
            @Override
            public void onDateSelected(Date date) {
                //String selectedDate = DateFormat.getDateInstance(DateFormat.FULL).format(date);

                Calendar calSelected = Calendar.getInstance();
                calSelected.setTime(date);

                selectedDate =new SimpleDateFormat("dd-MM-yyyy").format(calSelected.getTime());
                if (mAuth.getCurrentUser() != null) {
                    String docEmail = mAuth.getCurrentUser().getEmail();
                    Appointment appointment = new Appointment(selectedDate, docEmail);
                    dialog(appointment);
                } else {
                    Toast.makeText(getApplicationContext(), "ERROR", Toast.LENGTH_LONG).show();
                }

            }

            @Override
            public void onDateUnselected(Date date) {

            }
        });


        DatabaseReference pop = mDatabase.getReference().child("Appointments");
        mDBListner = pop.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> stringDates = new ArrayList<>();
                dates = new ArrayList<>();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String date = ds.child("calendarDate").getValue(String.class);
                    if(ds.child("doctorEmail").getValue(String.class).equals(mAuth.getCurrentUser().getEmail()))
                        stringDates.add(date);
                }
                for(int i = 0;i<stringDates.size();i++)
                    try
                    {
                        Date date = simpleDateFormat.parse(stringDates.get(i));
                        dates.add(date);
                    }
                    catch (ParseException ex)
                    {
                        System.out.println("Exception "+ex);
                    }
                datePicker.highlightDates(dates);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        datePicker.init(firstOfTheYear, nextYear.getTime());
    }

    void dialog(final Appointment appointment) {
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

        Button button1 = new Button(getApplicationContext());
        button1.setText("ADD APPOINTMENT");
        button1.setTextSize(18);
        button1.setTypeface(title.getTypeface(), Typeface.BOLD);
        button1.setTextColor(Color.WHITE);
        button1.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(600, LinearLayout.LayoutParams.MATCH_PARENT);
        params.setMargins(0, 30, 0, 10);
        button1.setLayoutParams(params);
        button1.setPadding(15, 15, 15, 15);
        Drawable drawable = ContextCompat.getDrawable(getApplicationContext(), R.drawable.dialog_button);
        button1.setBackground(drawable);

        layout.addView(button1);

        Button button2 = new Button(getApplicationContext());
        button2.setText("VIEW APPOINTMENTS");
        button2.setTextSize(18);
        button2.setTypeface(title.getTypeface(), Typeface.BOLD);
        button2.setTextColor(Color.WHITE);
        button2.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(600, LinearLayout.LayoutParams.MATCH_PARENT);
        params2.setMargins(0, 0, 0, 0);
        button2.setLayoutParams(params2);
        button2.setPadding(15, 15, 15, 15);
        button2.setBackground(drawable);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogViewApp();
            }
        });


        layout.addView(button2);


        layout.setGravity(Gravity.CENTER);

        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setCustomTitle(title);
        dialog.setView(layout);

        dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getApplicationContext(), getString(R.string.cancelPassRes), Toast.LENGTH_LONG).show();
            }
        });

        final AlertDialog alertDialog = dialog.create();

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogAddApp(appointment);
            }
        });

        alertDialog.getWindow().getAttributes().windowAnimations = R.style.DialogTheme;
        alertDialog.show();

    }

    void dialogAddApp(final Appointment appointment) {
        // DIALOG LAYOUT
        LayoutInflater inflater = LayoutInflater.from(DoctorCalendar.this);
        View view = inflater.inflate(R.layout.dialog_calendar_layout, null);
        final Spinner spinner = view.findViewById(R.id.spinnerIntervals);

        // VALUE LIST
        final List<String> intervalList = new ArrayList<>();
        intervalList.add("No selection!");
        intervalList.add("10:00-10:30");
        intervalList.add("10:30-11:00");
        intervalList.add("11:00-11:30");
        intervalList.add("11:30-12:00");
        intervalList.add("12:00-12:30");
        intervalList.add("12:30-13:00");
        intervalList.add("13:00-13:30");
        intervalList.add("13:30-14:00");
        intervalList.add("14:00-14:30");
        intervalList.add("14:30-15:00");

        // FIRE BASE CHECK
        final Query check = mDatabase.getReference().child("Appointments").orderByChild("calendarDate").equalTo(selectedDate);
        if (check != null) {
            check.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    // TAKE THE DATA OUT OF THE DATABASE
                    List<Appointment> appointments = new ArrayList<>();
                    for (DataSnapshot appointment : dataSnapshot.getChildren()) {
                        appointments.add(appointment.getValue(Appointment.class));
                    }
                    // List of the existent values
                    List<String> existing = new ArrayList<>();
                    for(int i = 0;i<appointments.size();i++)
                    {
                        existing.add(appointments.get(i).getBeginAppointment()+"-"+appointments.get(i).getEndAppointment());
                    }
                    intervalList.removeAll(existing);
                    ArrayAdapter<String> adapter1 = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item,intervalList);
                    adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinner.setAdapter(adapter1);
                }


                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {


                }
            });

            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int _position, long id) {
                    position = _position;
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    position = 0;
                }
            });

            TextView title = new TextView(this);
            title.setText("Generate appointments");
            int color = ResourcesCompat.getColor(getResources(), R.color.colorPrimary, getTheme());
            title.setBackgroundColor(color);
            title.setPadding(15, 15, 15, 15);
            title.setGravity(Gravity.CENTER);
            title.setTextColor(Color.WHITE);
            title.setTypeface(title.getTypeface(), Typeface.BOLD);
            title.setTextSize(20);

            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setCustomTitle(title);
            dialog.setView(view);
            dialog.setPositiveButton("ADD", null);

            dialog.setNegativeButton("DONE", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(getApplicationContext(), getString(R.string.cancelPassRes), Toast.LENGTH_LONG).show();
                }
            });

            AlertDialog alertDialog = dialog.create();
            alertDialog.getWindow().getAttributes().windowAnimations = R.style.DialogTheme;

            alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(final DialogInterface dialog) {
                    Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (checkSpinner() == true) {
                                String hours = intervalList.get(position);
                                String[] parts = hours.split("-");
                                String begin = parts[0];
                                String end = parts[1];
                                appointment.setBeginAppointment(begin);
                                appointment.setEndAppointment(end);
                                DatabaseReference appointmentRef = mDatabase.getReference().child("Appointments").push();
                                appointment.setUniqueId(appointmentRef.getKey());
                                appointmentRef.setValue(appointment);
                                dialog.dismiss();
                            } else {
                                Toast.makeText(getApplicationContext(), "Please select an option!", Toast.LENGTH_LONG).show();
                            }


                        }
                    });
                }

            });
            alertDialog.show();

        }
    }

    void dialogViewApp() {

        // DIALOG LAYOUT
        LayoutInflater inflater = LayoutInflater.from(DoctorCalendar.this);
        View view = inflater.inflate(R.layout.dialog_rv, null);
        final RecyclerView rv = view.findViewById(R.id.recycler_view_app);
        rv.setHasFixedSize(true);
        rv.setLayoutManager(new LinearLayoutManager(this));
        final ProgressBar pb = view.findViewById(R.id.pb_app);
        appointments = new ArrayList<>();
        adapter = new AppointmentAdapter(getApplicationContext(),appointments);
        adapter.setOnItemClickListener(DoctorCalendar.this);
        rv.setAdapter(adapter);

        final Query check = mDatabase.getReference().child("Appointments").orderByChild("calendarDate").equalTo(selectedDate);
        if (check != null) {
            check.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    appointments.clear();
                    for (DataSnapshot appointment : dataSnapshot.getChildren()) {
                        Appointment app = appointment.getValue(Appointment.class);
                        if(app.getDoctorEmail().equals(mAuth.getCurrentUser().getEmail())) {
                            app.setUniqueId(appointment.getKey());
                            appointments.add(app);
                        }
                    }
                    adapter.notifyDataSetChanged();
                    pb.setVisibility(View.INVISIBLE);
                }


                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {


                }
            });
            pb.setVisibility(View.INVISIBLE);

            TextView title = new TextView(this);
            title.setText("View appointments from "+selectedDate+".");
            int color = ResourcesCompat.getColor(getResources(), R.color.colorPrimary, getTheme());
            title.setBackgroundColor(color);
            title.setPadding(15, 15, 15, 15);
            title.setGravity(Gravity.CENTER);
            title.setTextColor(Color.WHITE);
            title.setTypeface(title.getTypeface(), Typeface.BOLD);
            title.setTextSize(20);

            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setCustomTitle(title);
            dialog.setView(view);
            dialog.setPositiveButton("ADD", null);

            dialog.setPositiveButton("DONE", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(getApplicationContext(), getString(R.string.cancelPassRes), Toast.LENGTH_LONG).show();
                }
            });

            AlertDialog alertDialog = dialog.create();
            alertDialog.getWindow().getAttributes().windowAnimations = R.style.DialogTheme;
            alertDialog.show();

        }
    }

    void dialogSelectPatient(final int position){
        LayoutInflater inflater = LayoutInflater.from(DoctorCalendar.this);
        View view = inflater.inflate(R.layout.dialog_pat_select, null);
        final Spinner spinner = view.findViewById(R.id.spinnerIntervals);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String id = mAuth.getUid();

        DatabaseReference getId = mDatabase.getReference().child("Doctors").child(id).child("accessCode");

        getId.addListenerForSingleValueEvent(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final Long accessL = dataSnapshot.getValue(Long.class);
                final Integer access = Math.toIntExact(accessL);
                final Query check = mDatabase.getReference("Patients");
                if (check != null) {
                    check.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            emails = new ArrayList<>();
                            emails.add("None selected");
                            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                if(ds.child("doctor").getValue(Integer.class).equals(access)) {
                                    String email = ds.child("email").getValue(String.class);
                                    emails.add(email);
                                }
                            }

                            ArrayAdapter<String> adapter1 = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item,emails);
                            adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinner.setAdapter(adapter1);
                        }


                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {


                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // FIRE BASE CHECK

            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int _position, long id) {
                    patientPos = _position;
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    patientPos = 0;
                }
            });

            TextView title = new TextView(this);
            title.setText("Add patient");
            int color = ResourcesCompat.getColor(getResources(), R.color.colorPrimary, getTheme());
            title.setBackgroundColor(color);
            title.setPadding(15, 15, 15, 15);
            title.setGravity(Gravity.CENTER);
            title.setTextColor(Color.WHITE);
            title.setTypeface(title.getTypeface(), Typeface.BOLD);
            title.setTextSize(20);

            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setCustomTitle(title);
            dialog.setView(view);
            dialog.setPositiveButton("ADD", null);

            dialog.setNegativeButton("DONE", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });

            AlertDialog alertDialog = dialog.create();
            alertDialog.getWindow().getAttributes().windowAnimations = R.style.DialogTheme;

            alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(final DialogInterface dialog) {
                    Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (checkPatSpinner() == true) {
                                Appointment selectedItem = appointments.get(position);
                                final String selectedKey = selectedItem.getUniqueId();
                                appointments.get(position).setPatientEmail(emails.get(patientPos));
                                adapter.notifyDataSetChanged();
                                DatabaseReference refEmail = mDatabase.getReference().child("Appointments").child(selectedKey).child("patientEmail");
                                refEmail.setValue(emails.get(patientPos));
                                Toast.makeText(getApplicationContext(),"Patient added!",Toast.LENGTH_LONG).show();
                                dialog.dismiss();
                            } else {
                                Toast.makeText(getApplicationContext(), "Please select an option!", Toast.LENGTH_LONG).show();
                            }
                        }});
                }

            });
            alertDialog.show();

        }

    // CHECKS
    boolean checkSpinner() {
        if (position == 0)
            return false;
        else
            return true;
    }

    private boolean checkPatSpinner() {
        if (patientPos == 0)
            return false;
        else
            return true;
    }

    // ON CLICK EVENTS
    @Override
    public void onItemClick(int position) {

    }

    @Override
    public void onDeleteClick(int position) {
        Appointment selectedItem = appointments.get(position);
        final String selectedKey = selectedItem.getUniqueId();
        appointments.remove(appointments.get(position));
        adapter.notifyDataSetChanged();
        DatabaseReference ref = mDatabase.getReference().child("Appointments").child(selectedKey);
        ref.removeValue();
        Toast.makeText(getApplicationContext(),"Appointment deleted!",Toast.LENGTH_LONG).show();
    }

    @Override
    public void onEditClick(int position) {
        dialogSelectPatient(position);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDatabase.getReference().removeEventListener(mDBListner);
    }
}



