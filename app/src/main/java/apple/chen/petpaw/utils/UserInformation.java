package apple.chen.petpaw.utils;

/**
 * Created by User on 2/8/2017.
 */

public class UserInformation {

    private String name;
    private String email;
    private String petName;
    private String petWeight;
    private String petAge;

    public UserInformation(){

    }
    public UserInformation(String name, String email ,String petName, String petWeight, String petAge) {
        this.email = email;
        this.name = name;

        this.petName = petName;
        this.petWeight = petWeight;
        this.petAge = petAge;

    }

    public UserInformation(String petName, String petWeight, String petAge){
        this.petName = petName;
        this.petWeight = petWeight;
        this.petAge = petAge;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPetName() {
        return petName;
    }

    public void setPetName(String petName) {
        this.petName = petName;
    }

    public String getPetWeight() {
        return petWeight;
    }

    public void setPetWeight(String petWeight) {
        this.petWeight = petWeight;
    }

    public String getPetAge() {
        return petAge;
    }

    public void setPetAge(String petAge) {
        this.petAge = petAge;
    }

}
