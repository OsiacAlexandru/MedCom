package osiac.ase.ro.medcom.Classes;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

import osiac.ase.ro.medcom.R;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

        public static  final int MSG_TYPE_OTHER = 0;
        public static  final int MSG_TYPE_YOU = 1;

        private Context mContext;
        private List<Message> messages;

        FirebaseUser fuser;

        public MessageAdapter(Context mContext, List<Message> messages){
            this.messages = messages;
            this.mContext = mContext;
        }

        @NonNull
        @Override
        public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == MSG_TYPE_YOU) {
                View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_you, parent, false);
                return new MessageAdapter.ViewHolder(view);
            } else {
                View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_other, parent, false);
                return new MessageAdapter.ViewHolder(view);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull MessageAdapter.ViewHolder holder, int position) {

            Message message = messages.get(position);

            holder.show_message.setText(message.getText());

            if (position == messages.size()-1){
                if (message.isSeen()==true){
                    holder.seen.setText("Seen");
                } else {
                    holder.seen.setText("Delivered");
                }
            } else {
                holder.seen.setVisibility(View.GONE);
            }

        }

        @Override
        public int getItemCount() {
            return messages.size();
        }

        public  class ViewHolder extends RecyclerView.ViewHolder{

            public TextView show_message;
            public TextView seen;

            public ViewHolder(View itemView) {
                super(itemView);
                show_message = itemView.findViewById(R.id.show_message);
                seen=itemView.findViewById(R.id.txt_seen);
            }
        }

        @Override
        public int getItemViewType(int position) {
            fuser = FirebaseAuth.getInstance().getCurrentUser();
            if (messages.get(position).getSender().equals(fuser.getEmail())){
                return MSG_TYPE_YOU;
            } else {
                return MSG_TYPE_OTHER;
            }
        }
    }
