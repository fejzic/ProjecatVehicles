package ba.unsa.etf.rs.zadaca5;

public class Vehicle {
    private int id;
    private Manufacturer manufacturer;
    private String model;
    private String chasisNumber;
    private String plateNumber;
    private Owner owner;

    public Vehicle(int id, Manufacturer manufacturer, String model, String chasisNumber, String plateNumber, Owner owner) {
        this.id = id;
        this.manufacturer = manufacturer;
        this.model = model;
        this.chasisNumber = chasisNumber;
        this.plateNumber = plateNumber;
        this.owner = owner;
    }

    public Vehicle() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Manufacturer getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(Manufacturer manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getChasisNumber() {
        return chasisNumber;
    }

    public void setChasisNumber(String chasisNumber) {
        this.chasisNumber = chasisNumber;
    }

    public String getPlateNumber() {
        return plateNumber;
    }

    public void setPlateNumber(String plateNumber) {
        this.plateNumber = plateNumber;
    }

    public Owner getOwner() {
        return owner;
    }

    public void setOwner(Owner owner) {
        this.owner = owner;
    }
}
