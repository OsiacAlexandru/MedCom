package osiac.ase.ro.medcom.Classes;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.squareup.picasso.Picasso;

import java.util.List;

import osiac.ase.ro.medcom.Activities.DoctorMessageActivity;
import osiac.ase.ro.medcom.R;

public class PatientForChatAdapter extends RecyclerView.Adapter<PatientForChatAdapter.ViewHolder> {
    private Context mContext;
    private List<Patient> patients;
    private List<ProfilePicture> pictures;
    DatabaseReference mRef;

    public PatientForChatAdapter(Context mContext, List<Patient> patients,List<ProfilePicture> pic) {
        this.patients = patients;
        this.mContext = mContext;
        this.pictures = pic;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.chat_patients_item, parent, false);
        return new PatientForChatAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

        final Patient p = patients.get(position);
        holder.username.setText(p.getName());

        final String email = p.getEmail();

        for(int i=0;i<pictures.size();i++) {
            if (p.getEmail().equals(pictures.get(i).getEmail())) {
                Picasso.get().load(pictures.get(i).getImageUrl()).fit().into(holder.profile_image);
            } else {
                holder.profile_image.setImageResource(R.drawable.blanck_user);
            }
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext,DoctorMessageActivity.class);
                intent.putExtra("email",p.getEmail());
                mContext.startActivity(intent);
            }
        });
    }


    @Override
    public int getItemCount() {
        return patients.size();
    }

    public  class ViewHolder extends RecyclerView.ViewHolder{

        public TextView username;
        public ImageView profile_image;

        public ViewHolder(View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.username);
            profile_image = itemView.findViewById(R.id.profile_image);
        }
    }
}