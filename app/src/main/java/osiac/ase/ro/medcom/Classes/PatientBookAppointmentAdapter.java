package osiac.ase.ro.medcom.Classes;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import osiac.ase.ro.medcom.R;

public class PatientBookAppointmentAdapter extends RecyclerView.Adapter<PatientBookAppointmentAdapter.ViewHolder>{

    private Context mContext;
    private List<Appointment> appointments;
    private PatientBookAppointmentAdapter.OnItemClickListener mListener;

    public PatientBookAppointmentAdapter(Context mContext, List<Appointment> appointments) {
        this.mContext = mContext;
        this.appointments = appointments;
    }

    @NonNull
    @Override
    public PatientBookAppointmentAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.list_item_patient_book_appointment,viewGroup, false);
        return new PatientBookAppointmentAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PatientBookAppointmentAdapter.ViewHolder viewHolder,final int position) {
        Appointment appCurrent = appointments.get(position);
        viewHolder.textViewHour.setText(appCurrent.getBeginAppointment()+"-"+appCurrent.getEndAppointment());
        viewHolder.textViewPatient.setText(appCurrent.getPatientEmail());
        viewHolder.textViewDoctor.setText(appCurrent.getDoctorEmail());
        viewHolder.textViewID.setText(appCurrent.getUniqueId());
    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements  View.OnClickListener,View.OnCreateContextMenuListener ,MenuItem.OnMenuItemClickListener {
        public TextView textViewHour;
        public TextView textViewPatient;
        public TextView textViewID;
        public TextView textViewDoctor;

        public ViewHolder(View itemView) {
            super(itemView);
            textViewHour = itemView.findViewById(R.id.text_view_hour);
            textViewPatient = itemView.findViewById(R.id.text_view_patient);
            textViewDoctor = itemView.findViewById(R.id.text_view_doctor);
            textViewID = itemView.findViewById(R.id.text_view_uniqueID);

            itemView.setOnClickListener(this);
            itemView.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mListener != null) {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    mListener.onItemClick(position);
                }
            }
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            MenuItem book = menu.add(Menu.NONE, 1, 1, "Book appointment");
            book.setOnMenuItemClickListener(this);
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            if (mListener != null) {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    switch (item.getItemId()) {
                        case 1:
                            mListener.onBookClick(position);
                            return true;
                    }
                }
            }
            return false;
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
        void onBookClick(int position);

    }

    public void setOnItemClickListener(PatientBookAppointmentAdapter.OnItemClickListener listener) {
        mListener = listener;
    }
}

