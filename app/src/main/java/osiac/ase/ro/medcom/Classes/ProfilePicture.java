package osiac.ase.ro.medcom.Classes;

public class ProfilePicture {

    private String uid;
    private String imageUrl;
    private String email;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    private ProfilePicture() {

    }

    public ProfilePicture(String url,String uid,String email) {
        this.imageUrl=url;
        this.uid=uid;
        this.email=email;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
