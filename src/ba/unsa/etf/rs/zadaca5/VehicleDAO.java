package ba.unsa.etf.rs.zadaca5;

import javafx.beans.Observable;
import javafx.collections.ObservableList;

public interface VehicleDAO {

    ObservableList<Owner> getOwners();
    ObservableList<Vehicle> getVehicles();
    ObservableList<Place> getPlaces();
    ObservableList<Manufacturer> getManufacturers();
    void addOwner(Owner owner);
    void changeOwner(Owner owner);
    void deleteOwner(Owner owner);
    void addVehicle(Vehicle vehicle);
    void changeVehicle(Vehicle vehicle);
    void deleteVehicle(Vehicle vehicle);
    void close();
}
