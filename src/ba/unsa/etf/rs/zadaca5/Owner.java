package ba.unsa.etf.rs.zadaca5;

import java.time.LocalDate;

public class Owner {

    private int id;
    private String name, surname, parentName;
    private LocalDate dateOfBirth;
    private Place placeOfBirth;
    private String livingAddress;
    private Place livingPlace;
    private String jmbg;
    private long dateOfBirthDays;



    public Owner(int id, String name, String surname, String parentName, LocalDate dateOfBirth, Place placeOfBirth, String livingAddress, Place livingPlace, String jmbg) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.parentName = parentName;
        this.dateOfBirth = dateOfBirth;
        this.placeOfBirth = placeOfBirth;
        this.livingAddress = livingAddress;
        this.livingPlace = livingPlace;
        this.jmbg = jmbg;

    }

    public Owner() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
        this.dateOfBirthDays = dateOfBirth.toEpochDay();

    }

    public Place getPlaceOfBirth() {
        return placeOfBirth;
    }

    public void setPlaceOfBirth(Place placeOfBirth) {
        this.placeOfBirth = placeOfBirth;
    }

    public String getLivingAddress() {
        return livingAddress;
    }

    public void setLivingAddress(String livingAddress) {
        this.livingAddress = livingAddress;
    }

    public Place getLivingPlace() {
        return livingPlace;
    }

    public void setLivingPlace(Place livingPlace) {
        this.livingPlace = livingPlace;
    }

    public String getJmbg() {
        return jmbg;
    }

    public void setJmbg(String jmbg) {
        this.jmbg = jmbg;
    }

    public long getDateOfBirthDays() {
        return dateOfBirthDays;
    }

    public void setDateOfBirthDays(long dateOfBirthDays) {
        this.dateOfBirthDays = dateOfBirthDays;
        this.dateOfBirth = LocalDate.ofEpochDay(dateOfBirthDays);
    }

    @Override
    public String toString() {
        return  surname + " " + name ;
    }
}
