package ba.unsa.etf.rs.zadaca5;

import javafx.collections.ObservableList;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class VehicleDAOBaseTest {
    VehicleDAOBase dao = null;

    @Test
    void initDb() {
        if (dao != null) dao.close();

        File dbfile = new File("vehicles.db");
        ClassLoader classLoader = getClass().getClassLoader();
        File srcfile = new File(classLoader.getResource("db/vehicles.db").getFile());
        try {
            dbfile.delete();
            Files.copy(srcfile.toPath(), dbfile.toPath());
        } catch (IOException e) {
            e.printStackTrace();
            fail("Ne mogu kreirati bazu");
        }

        dao = new VehicleDAOBase();
    }


    @Test
    void getOwners() {
        initDb();
        ObservableList<Owner> owners = dao.getOwners();
        assertEquals(1, owners.size());
        assertEquals("Meho", owners.get(0).getName());
        assertEquals("Mehaga", owners.get(0).getParentName());
        assertEquals(LocalDate.of(1970,1,2), owners.get(0).getDateOfBirth());
        assertEquals("Sarajevo", owners.get(0).getPlaceOfBirth().getName());
        assertEquals("Zmaja od Bosne bb", owners.get(0).getLivingAddress());
        assertEquals("Sarajevo", owners.get(0).getLivingPlace().getName());
    }

    @Test
    void getVehicles() {
        initDb();
        ObservableList<Vehicle> vehicles = dao.getVehicles();
        assertEquals(1, vehicles.size());
        assertEquals("Volkswagen", vehicles.get(0).getManufacturer().getName());
        assertEquals("Golf", vehicles.get(0).getModel());
        assertEquals("A12-O-123", vehicles.get(0).getPlateNumber());
        assertEquals("Mehic", vehicles.get(0).getOwner().getSurname());
    }

    @Test
    void getPlaces() {
        initDb();
        ObservableList<Place> places = dao.getPlaces();
        assertEquals(2, places.size());
        assertEquals("71000", places.get(0).getPostalNumber());
        assertEquals("Tuzla", places.get(1).getName());
    }

    @Test
    void getManufacturers() {
        initDb();
        ObservableList<Manufacturer> manufacturers = dao.getManufacturers();
        assertEquals(3, manufacturers.size());
        // Ovo će vratiti abecedno
        assertEquals("Ford", manufacturers.get(0).getName());
        assertEquals("Renault", manufacturers.get(1).getName());
    }

    @Test
    void addOwner() {
        initDb();

        Place sarajevo = new Place(1, "Sarajevo", "71000");
        Place zenica = new Place(0, "Zenica", "73000");
        Owner owner = new Owner(0, "Test", "Testovic", "Te", LocalDate.now(), sarajevo,
                "Prva ulica 1", zenica, "1234567890");
        dao.addOwner(owner);

        // Provjera dodavanja
        ObservableList<Owner> owners = dao.getOwners();
        // Broj vlasnika je sada 2
        assertEquals(2, owners.size());
        // Na indeksu 1 je Test Testovic
        assertEquals("Testovic", owners.get(1).getSurname());
        // Novi vlasnik je dobio id 2
        assertEquals(2, owners.get(1).getId());
        // jmbg
        assertEquals("1234567890", owners.get(1).getJmbg());
        // Mjesto rođenja je Sarajevo
        assertEquals("Sarajevo", owners.get(1).getPlaceOfBirth().getName());
        // Ovo mjesto je postojeće mjesto u bazi (ima id 1)
        assertEquals(1, owners.get(1).getPlaceOfBirth().getId());
        // Mjesto prebivališta je Zenica
        assertEquals("Zenica", owners.get(1).getLivingPlace().getName());
        // Zenica je dodata u bazu podataka pod ID-om 3
        assertEquals(3, owners.get(1).getLivingPlace().getId());

        // Provjeravamo da li je Zenica zaista dodata u mjesta
        ObservableList<Place> places = dao.getPlaces();
        assertEquals(3, places.size());
        assertEquals(3, places.get(2).getId());
        assertEquals("Zenica", places.get(2).getName());
    }

    @Test
    void changeOwner() {
        initDb();

        ObservableList<Owner> owners = dao.getOwners();
        Owner owner = owners.get(0);
        owner.setJmbg("1234567890");
        Place zenica = new Place(0, "Zenica", "73000");
        owner.setPlaceOfBirth(zenica);

        dao.changeOwner(owner);

        // Provjeravamo da li je postojeći promijenjen
        ObservableList<Owner> owners2 = dao.getOwners();
        assertEquals(1, owners2.size());
        assertEquals("1234567890", owners2.get(0).getJmbg());
        assertEquals("Meho", owners2.get(0).getName());
        assertEquals("Zenica", owners2.get(0).getPlaceOfBirth().getName());

        // Provjeravamo da li je Zenica zaista dodata u mjesta
        ObservableList<Place> places = dao.getPlaces();
        assertEquals(3, places.size());
        assertEquals(3, places.get(2).getId());
        assertEquals("Zenica", places.get(2).getName());
    }

    @Test
    void deleteOwner() {
        initDb();

        ObservableList<Owner> owners = dao.getOwners();
        Owner mehoMehic = owners.get(0);

        // Ne može se obrisati jer ima vozilo!
        assertThrows(IllegalArgumentException.class, () -> dao.deleteOwner(mehoMehic));

        // Dodajemo novog vlasnika
        Place sarajevo = new Place(1, "Sarajevo", "71000");
        Place zenica = new Place(0, "Zenica", "73000");
        Owner testTestovic = new Owner(0, "Test", "Testović", "Te", LocalDate.now(), sarajevo,
                "Prva ulica 1", zenica, "1234567890");
        dao.addOwner(testTestovic);

        // Detaljna provjera dodavanja bi trebala biti urađena u testu dodajVlasnika()
        ObservableList<Owner> owners2 = dao.getOwners();
        assertEquals(2, owners2.size());
        testTestovic = owners2.get(1); // osiguravamo da je id ispravan

        // Postavljamo Testa Testovića za vlasnika vozila 1
        ObservableList<Vehicle> vehicles = dao.getVehicles();
        Vehicle vehicle = vehicles.get(0);
        vehicle.setOwner(testTestovic);
        dao.changeVehicle(vehicle);

        // Brišemo Mehu Mehića
        dao.deleteOwner(mehoMehic);

        ObservableList<Owner> owners3 = dao.getOwners();
        assertEquals(1, owners3.size());
        assertEquals("Testović", owners3.get(0).getSurname());
        assertEquals(2, owners3.get(0).getId());

        // Da li je vlasnik vozila stvarno promijenjen?
        ObservableList<Vehicle> vehicles2 = dao.getVehicles();
        assertEquals("Test", vehicles2.get(0).getOwner().getName());
    }

    @Test
    void addVehicle() {
        initDb();

        Manufacturer renault = new Manufacturer(2, "Renault");
        Place sarajevo = new Place(1, "Sarajevo", "71000");
        Owner owner = new Owner(2, "Test", "Testovic", "Te", LocalDate.now(), sarajevo,
                "Prva ulica 1", sarajevo, "1234567890");
        Vehicle vehicle = new Vehicle(0, renault, "Megane", "98765", "E12-K-987", owner);

        // Ne može se dodati jer je vlasnik nepostojeći
        assertThrows(IllegalArgumentException.class, () -> dao.addVehicle(vehicle));

        // Ovaj vlasnik je identičan onom u bazi
        Owner mehoMehic = new Owner(1, "Meho", "Mehic", "Mehaga",
                LocalDate.of(1970,2,1), sarajevo, "Zmaja od Bosne bb",
                sarajevo, "123453452345");
        vehicle.setOwner(mehoMehic);
        dao.addVehicle(vehicle);

        ObservableList<Vehicle> vehicles = dao.getVehicles();
        assertEquals(2, vehicles.size());
        assertEquals("Renault", vehicles.get(1).getManufacturer().getName());
        assertEquals("Megane", vehicles.get(1).getModel());
        assertEquals("E12-K-987", vehicles.get(1).getPlateNumber());
        assertEquals("Meho", vehicles.get(1).getOwner().getName());
    }

    @Test
    void addVehicle2() {
        initDb();

        // Testiramo da li će se dodati novi proizvođač
        Manufacturer hyundai = new Manufacturer(0, "Hyundai");
        Place sarajevo = new Place(1, "Sarajevo", "71000");
        Owner mehoMehic = new Owner(1, "Meho", "Mehic", "Mehaga",
                LocalDate.of(1970,2,1), sarajevo, "Zmaja od Bosne bb",
                sarajevo, "123453452345");
        Vehicle vehicle = new Vehicle(0, hyundai, "i30", "98765",
                "E12-K-987", mehoMehic);

        dao.addVehicle(vehicle);

        ObservableList<Vehicle> vehicles = dao.getVehicles();
        assertEquals(2, vehicles.size());
        assertEquals("Hyundai", vehicles.get(1).getManufacturer().getName());

        // Da li se hyundai zaista dodao u listu proizvođača?
        ObservableList<Manufacturer> manufacturers = dao.getManufacturers();
        assertEquals(4, manufacturers.size());
        // Ovo će vratiti abecedno, tako da će Hyundai biti na indeksu 1 (poslije Ford a prije Renault)
        assertEquals("Hyundai", manufacturers.get(1).getName());
        // Trebao bi dobiti Id 4
        assertEquals(4, manufacturers.get(1).getId());
    }

    @Test
    void changeVehicle() {
        initDb();

        ObservableList<Vehicle> vehicles = dao.getVehicles();
        Vehicle vehicle = vehicles.get(0);
        Manufacturer hyundai = new Manufacturer(0, "Hyundai");
        vehicle.setManufacturer(hyundai);
        vehicle.setModel("i30");
        dao.changeVehicle(vehicle);

        ObservableList<Vehicle> vehicles2 = dao.getVehicles();
        assertEquals(1, vehicles2.size());
        assertEquals("Hyundai", vehicles2.get(0).getManufacturer().getName());
        assertEquals("i30", vehicles2.get(0).getModel());

        // Da li se hyundai zaista dodao u listu proizvođača?
        ObservableList<Manufacturer> manufacturers = dao.getManufacturers();
        assertEquals(4, manufacturers.size());
        // Ovo će vratiti abecedno, tako da će Hyundai biti na indeksu 1 (poslije Ford a prije Renault)
        assertEquals("Hyundai", manufacturers.get(1).getName());
        // Trebao bi dobiti Id 4
        assertEquals(4, manufacturers.get(1).getId());
    }

    @Test
    void deleteVehicle() {
        initDb();

        ObservableList<Vehicle> vehicles = dao.getVehicles();
        Vehicle vehicle = vehicles.get(0);
        dao.deleteVehicle(vehicle);

        ObservableList<Vehicle> vehicles2 = dao.getVehicles();
        assertEquals(0, vehicles2.size());
    }
}

