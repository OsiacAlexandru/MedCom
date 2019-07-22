package osiac.ase.ro.medcom.Classes;


public class Appointment {
    private String calendarDate;
    private String beginAppointment;
    private String endAppointment;
    private String doctorEmail;
    private String patientEmail;
    private String uniqueId;

    public Appointment() {

    }

    public Appointment(String calendarDate,String docEmail) {
        doctorEmail = docEmail;
        this.calendarDate = calendarDate;
        this.patientEmail = "null";
        this.uniqueId = "null";
    }

    public String getCalendarDate() {
        return calendarDate;
    }

    public void setCalendarDate(String calendarDate) {
        this.calendarDate = calendarDate;
    }

    public String getBeginAppointment() {
        return beginAppointment;
    }

    public void setBeginAppointment(String beginAppointment) {
            this.beginAppointment = beginAppointment;

    }

    public String getEndAppointment() {
        return endAppointment;
    }

    public void setEndAppointment(String endAppointment) {
            this.endAppointment = endAppointment;
    }

    public String getDoctorEmail() {
        return doctorEmail;
    }

    public void setDoctorEmail(String doctorEmail) {
        this.doctorEmail = doctorEmail;
    }

    public String getPatientEmail() {
        return patientEmail;
    }

    public void setPatientEmail(String patientEmail) {
        this.patientEmail = patientEmail;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }



    @Override
    public String toString() {
       return   new String(calendarDate+"\n"+beginAppointment+"\n"+endAppointment+"\n"+doctorEmail+"\n"+patientEmail+"\n"+uniqueId);
    }
}
