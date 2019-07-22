package osiac.ase.ro.medcom.Classes;


public class Patient extends User implements Cloneable {

    private Integer DoctorId;
    private String HealthSecurityCode;

    public Patient(String healthSecurity,String name, String password, String email,Integer doc)
    {
        super(name, password,email);
        DoctorId=doc;
        HealthSecurityCode = healthSecurity;
    }

    public String getHealthSecurityCode()
    {
        return HealthSecurityCode;
    }

    public void setHealthSecurityCode(String healthSecurityCode)
    {
        HealthSecurityCode = healthSecurityCode;
    }

    public Integer getDoctor()
    {
        return DoctorId;
    }

    public void setDoctor(Integer doctor)
    {
        DoctorId = doctor;
    }

    @Override
    public String getName()
    {
        return super.getName();
    }

    @Override
    public void setName(String name)
    {
        super.setName(name);
    }

    @Override
    public String getPassword()
    {
        return super.getPassword();
    }

    @Override
    public void setPassword(String password)
    {
        super.setPassword(password);
    }

    @Override
    public String getEmail()
    {
        return super.getEmail();
    }

    @Override
    public void setEmail(String email)
    {
        super.setEmail(email);
    }

    @Override
    public String toString() {
        return "Patient:" + getName() + "\n" +
                "Email:" + getEmail() +  "\n" +
                "Health Security Code:" + HealthSecurityCode + "\n" +
                "Doctor ID:" + DoctorId  ;
    }

    @Override
    protected Patient clone() throws CloneNotSupportedException {
        Patient p = (Patient)super.clone();
        p.setDoctor(this.DoctorId);
        p.setEmail(new String(this.getEmail()));
        p.setName(new String(this.getName()));
        p.setPassword(new String(this.getPassword()));
        p.setHealthSecurityCode(new String(this.HealthSecurityCode));
        return p;
    }
}
