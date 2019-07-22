package osiac.ase.ro.medcom.Classes;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import osiac.ase.ro.medcom.R;

public class PatientAppointmentAdapter extends RecyclerView.Adapter<PatientAppointmentAdapter.ViewHolder>{

    private Context mContext;
    private List<Appointment> appointments;

    public PatientAppointmentAdapter(Context mContext, List<Appointment> appointments) {
        this.mContext = mContext;
        this.appointments = appointments;
    }

    @NonNull
    @Override
    public PatientAppointmentAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.list_item_patient_own_appointments,viewGroup, false);
        return new PatientAppointmentAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PatientAppointmentAdapter.ViewHolder viewHolder,final int position) {
        Appointment appCurrent = appointments.get(position);
        viewHolder.textViewDate.setText(appCurrent.getCalendarDate());
        viewHolder.textViewHour.setText(appCurrent.getBeginAppointment()+"-"+appCurrent.getEndAppointment());
        viewHolder.textViewPatient.setText(appCurrent.getPatientEmail());
        viewHolder.textViewDoctor.setText(appCurrent.getDoctorEmail());
        viewHolder.textViewID.setText(appCurrent.getUniqueId());
    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textViewDate;
        public TextView textViewHour;
        public TextView textViewPatient;
        public TextView textViewID;
        public TextView textViewDoctor;

        public ViewHolder(View itemView) {
            super(itemView);
            textViewDate = itemView.findViewById(R.id.text_view_date);
            textViewHour = itemView.findViewById(R.id.text_view_hour);
            textViewPatient = itemView.findViewById(R.id.text_view_patient);
            textViewDoctor = itemView.findViewById(R.id.text_view_doctor);
            textViewID = itemView.findViewById(R.id.text_view_uniqueID);

        }
    }
}

