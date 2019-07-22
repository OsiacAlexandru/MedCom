package osiac.ase.ro.medcom.Classes;


public class Doctor extends User {

    private String AccessCode;

    public Doctor(String name, String password, String email, String accessCode) {
        super(name, password, email);
        AccessCode = accessCode;
    }

    @Override
    public String getName() {
        return super.getName();
    }

    @Override
    public void setName(String name) {
        super.setName(name);
    }

    @Override
    public String getPassword() {
        return super.getPassword();
    }

    @Override
    public void setPassword(String password) {
        super.setPassword(password);
    }

    @Override
    public String getEmail() {
        return super.getEmail();
    }

    @Override
    public void setEmail(String email) {
        super.setEmail(email);
    }

    public String getAccessCode() {
        return AccessCode;
    }
}
