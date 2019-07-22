package osiac.ase.ro.medcom.Classes;

public class Message {

        private boolean isSeen;
        private String sender;
        private String receiver;
        private String text;

    public Message(boolean isSeen, String sender, String receiver, String text) {
        this.isSeen = isSeen;
        this.sender = sender;
        this.receiver = receiver;
        this.text = text;
    }

    public boolean isSeen() {
        return isSeen;
    }

    public void setSeen(boolean seen) {
        isSeen = seen;
    }


        public Message() {
        }

        public String getSender() {
            return sender;
        }

        public void setSender(String sender) {
            this.sender = sender;
        }

        public String getReceiver() {
            return receiver;
        }

        public void setReceiver(String receiver) {
            this.receiver = receiver;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
}
