package ba.unsa.etf.rs.zadaca5;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.stream.IntStream;

public class VehicleDAOXML implements VehicleDAO {
    private ArrayList<Owner> owners = new ArrayList<>();
    private ArrayList<Vehicle> vehicles = new ArrayList<>();
    private ArrayList<Place> places = new ArrayList<>();
    private ArrayList<Manufacturer> manufacturers = new ArrayList<>();

    VehicleDAOXML() {
        readFiles();
        writeFiles();
    }

    private void readFiles() {
        owners.clear();
        vehicles.clear();
        try {
            XMLDecoder decoder = new XMLDecoder(new FileInputStream("owners.xml"));
            owners = (ArrayList<Owner>)decoder.readObject();
            decoder.close();
            decoder = new XMLDecoder(new FileInputStream("vehicles.xml"));
            vehicles = (ArrayList<Vehicle>)decoder.readObject();
            decoder.close();
            decoder = new XMLDecoder(new FileInputStream("places.xml"));
            places = (ArrayList<Place>)decoder.readObject();
            decoder.close();
            decoder = new XMLDecoder(new FileInputStream("manufacturers.xml"));
            manufacturers = (ArrayList<Manufacturer>)decoder.readObject();
            decoder.close();


            places.sort((m1, m2) -> m1.getName().compareTo(m2.getName()));
            manufacturers.sort((p1, p2) -> p1.getName().compareTo(p2.getName()));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void writeFiles() {
        try {
            XMLEncoder encoder = new XMLEncoder(new FileOutputStream("owners.xml"));
            encoder.writeObject(owners);
            encoder.close();
            encoder = new XMLEncoder(new FileOutputStream("vehicles.xml"));
            encoder.writeObject(vehicles);
            encoder.close();
            encoder = new XMLEncoder(new FileOutputStream("places.xml"));
            encoder.writeObject(places);
            encoder.close();
            encoder = new XMLEncoder(new FileOutputStream("manufactures.xml"));
            encoder.writeObject(manufacturers);
            encoder.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }
    @Override
    public ObservableList<Owner> getOwners() {
        return FXCollections.observableArrayList(owners);
    }

    @Override
    public ObservableList<Vehicle> getVehicles() {
        return FXCollections.observableArrayList(vehicles);
    }

    @Override
    public ObservableList<Place> getPlaces() {
        return FXCollections.observableArrayList(places);
    }

    @Override
    public ObservableList<Manufacturer> getManufacturers() {
        return FXCollections.observableArrayList(manufacturers);
    }

    public void addOwner(Owner owner) {
        owner.setPlaceOfBirth( addPlaceIfDoesntExist(owner.getPlaceOfBirth()) );
        owner.setLivingPlace( addPlaceIfDoesntExist(owner.getLivingPlace()) );
        int maxId = owners.stream().mapToInt(Owner::getId).filter(v -> v >= 0).max().orElse(0);
        owner.setId(maxId+1);

        owners.add(owner);
        writeFiles();

    }

    private Place addPlaceIfDoesntExist(Place mjesto) {
        int maxId = 0;
        int i = 0;
        if (i >= places.size()) {
        } else {
            do {
                if (places.get(i).getId() != mjesto.getId()) {
                    if (places.get(i).getId() > maxId)
                        maxId = places.get(i).getId();
                } else {
                    places.set(i, mjesto);
                    return mjesto;
                }
                i++;
            } while (i < places.size());
        }
        mjesto.setId(maxId + 1);
        places.add(mjesto);
        places.sort((m1, m2) -> m1.getName().compareTo(m2.getName()));
        return mjesto;
    }

    @Override
    public void changeOwner(Owner owner) {
        owner.setPlaceOfBirth( addPlaceIfDoesntExist(owner.getPlaceOfBirth()) );
        owner.setLivingPlace( addPlaceIfDoesntExist(owner.getLivingPlace()) );
        IntStream.range(0, owners.size()).filter(i -> owners.get(i).getId() == owner.getId()).forEach(i -> owners.set(i, owner));
        writeFiles();
    }

    @Override
    public void deleteOwner(Owner owner) {

        {
            int i = 0;
            if (i < vehicles.size()) {
                do {
                    Vehicle v = vehicles.get(i);
                    if (v.getOwner().getId() != owner.getId()) {
                        i++;
                        continue;
                    }
                    throw new IllegalArgumentException("Vlasnik ima vehicles!");

                } while (i < vehicles.size());
            }
        }

        int i = 0;
        while (true) {
            if (i >= owners.size()) break;
            if (owners.get(i).getId() == owner.getId())
                owners.remove(i);
            i++;
        }
        writeFiles();
    }

    @Override
    public void addVehicle(Vehicle vehicle) {
        vehicle.setManufacturer( dodajProizvodjacaAkoNePostoji(vehicle.getManufacturer()) );


        boolean ima = false;
        for (Iterator<Owner> iterator = owners.iterator(); iterator.hasNext(); ) {
            Owner v = iterator.next();
            if (v.getId() == vehicle.getOwner().getId())
                ima = true;
        }
        if (ima) {
            int maxId = 0;
            for (Vehicle v : vehicles)
                if (v.getId() > maxId) maxId = v.getId();
            vehicle.setId(maxId + 1);

            vehicles.add(vehicle);
            writeFiles();
        } else {
            throw new IllegalArgumentException("Nepoznat vlasnik sa id-om " + vehicle.getOwner().getId());
        }

    }


    private Manufacturer dodajProizvodjacaAkoNePostoji(Manufacturer proizvodjac) {
        int maxId = 0;
        int i = manufacturers.size() - 1;
        while (i >= 0) {
            if (manufacturers.get(i).getId() != proizvodjac.getId()) {
                if (manufacturers.get(i).getId() <= maxId) {
                } else {
                    maxId = manufacturers.get(i).getId();
                }
            } else {
                manufacturers.set(i, proizvodjac);
                return proizvodjac;
            }
            i--;
        }
        proizvodjac.setId(maxId+1);
        manufacturers.add(proizvodjac);
        manufacturers.sort((p1, p2) -> p1.getName().compareTo(p2.getName()));
        return proizvodjac;
    }

    @Override
    public void changeVehicle(Vehicle vehicle) {
        vehicle.setManufacturer( dodajProizvodjacaAkoNePostoji(vehicle.getManufacturer()) );

        // Provjera vlasnika
        boolean exist = false;
        {
            int i = 0;
            while (true) {
                if (i >= owners.size()) break;
                Owner v = owners.get(i);
                if (v.getId() == vehicle.getOwner().getId())
                    exist = true;
                i++;
            }
        }
        if (exist) {
            int i = 0;
            while (true) {
                if (i >= vehicles.size()) break;
                if (vehicles.get(i).getId() == vehicle.getId())
                    vehicles.set(i, vehicle);
                i++;
            }
            writeFiles();
        } else {
            throw new IllegalArgumentException("Unknown owner with id " + vehicle.getOwner().getId());
        }

    }

    @Override
    public void deleteVehicle(Vehicle vehicle) {
        int i = 0;
        while (i< vehicles.size()) {
            if (vehicles.get(i).getId() != vehicle.getId()) {
            } else {
                vehicles.remove(i);
            }
            i++;
        }
        writeFiles();
    }

    @Override
    public void close() {

    }
}
