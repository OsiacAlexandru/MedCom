package osiac.ase.ro.medcom.Classes;

public class Document {
    private String name;
    private String patientEmail;
    private String imageUrl;
    private String mKey;
    private String description;

    public Document() {
    }

    public Document(String _name, String _imageUrl,String _patientEmail,String _desc) {
        if (_name.trim().equals("")) {
            name = "No Name";
        } else {
            name = _name;
        }
        if (_desc.trim().equals("")) {
            description = "No Description";
        }
        else {
            description=_desc;
        }
        imageUrl = _imageUrl;
        patientEmail = _patientEmail;
    }

    public String getName() {
        return name;
    }

    public void setName(String _name) {
        name = _name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String _imageUrl) {
        imageUrl = _imageUrl;
    }

    public String getmPatientEmail() {
        return patientEmail;
    }

    public void setmPatientEmail(String _patientEmail) {
        this.patientEmail = _patientEmail;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getmKey() {
        return mKey;
    }

    public void setmKey(String mKey) {
        this.mKey = mKey;
    }
}

