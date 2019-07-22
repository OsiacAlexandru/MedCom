package osiac.ase.ro.medcom.Classes;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import osiac.ase.ro.medcom.R;

public class PatientAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<Patient> patients;
    private ArrayList<Patient> patientsCopy;
    private HashMap<String,Integer> itemStates;

    public PatientAdapter(Context mContext, ArrayList<Patient> patients) throws CloneNotSupportedException {
        this.mContext = mContext;
        this.patients=patients;

        patientsCopy = new ArrayList<>();
        for(Patient p : patients){
            Patient copy = p.clone();
            patientsCopy.add(copy);
        }

        this.itemStates=new HashMap<>();

        for(Patient patient:patients) {
            itemStates.put(patient.getHealthSecurityCode(),patient.getDoctor());
        }
    }

    @Override
    public int getCount()
    {
        return patients.size();
    }

    @Override
    public Object getItem(int position)
    {
        return patients.get(position);
    }

    @Override
    public long getItemId(int position) {
        return patients.get(position).getDoctor();
    }

    @Override
    public View getView(int position, View list_view, ViewGroup parent)
    {
        if(list_view==null)
        {
            list_view=LayoutInflater.from(mContext).inflate(R.layout.list_item_patients,parent,false);
        }

        TextView txtId=list_view.findViewById(R.id.id);
        TextView txtEmail = list_view.findViewById(R.id.email);
        TextView txtName=list_view.findViewById(R.id.name);

        final Patient patient =(Patient)getItem(position);

        txtId.setText(patient.getHealthSecurityCode());
        txtId.setPadding(10, 20, 15, 15);
        txtId.setTextSize(18);

        txtName.setText(patient.getName());
        txtName.setPadding(10,10,10,10);
        txtName.setTextSize(16);

        txtEmail.setText(patient.getEmail());
        txtEmail.setPadding(10,10,10,10);
        txtEmail.setTextSize(16);

        return list_view;
    }

    public void filter(String queryText)
    {
        patients.clear();
        if(queryText.isEmpty()) {
            patients.addAll(patientsCopy);
        }
        else {
            for(Patient p: patientsCopy) {
                if(p.getName().toLowerCase().contains(queryText.toLowerCase())) {
                    patients.add(p);
                }
            }

        }
        notifyDataSetChanged();
    }
}

