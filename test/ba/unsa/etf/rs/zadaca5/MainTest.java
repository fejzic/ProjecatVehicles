package ba.unsa.etf.rs.zadaca5;

import static org.junit.jupiter.api.Assertions.*;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static javafx.scene.layout.Region.USE_COMPUTED_SIZE;

import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@ExtendWith(ApplicationExtension.class)
class MainTest {
    Stage theStage;
    VehicleDAO dao;
    Controller controller;

    @BeforeEach
    public void initDAO() {
        dao = new VehicleDAOBase();
    }

    @AfterEach
    public void closeDAO() {
        if (dao != null) {
            dao.close();
        }
        dao = null;
    }

    @Start
    public void start (Stage stage) throws Exception {
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

        try {
            initFile("places.xml");
            initFile("manufacturers.xml");
            initFile("owners.xml");
            initFile("vehicles.xml");
        } catch (IOException e) {
            e.printStackTrace();
            fail("Ne mogu kreirati datoteku");
        }

        dao = new VehicleDAOBase();

        // Ovo bi trebalo da iskopira fajl iz resources u test-resources, a ipak radi i sa mavenom
        File fxml = new File("resources/fxml/main.fxml");
        if (fxml.exists()) {
            File rsrc = new File("test-resources/fxml/main.fxml");
            if (rsrc.exists()) rsrc.delete();
            Files.copy(fxml.toPath(), rsrc.toPath());
        }

        Parent root = FXMLLoader.load(getClass().getResource("/fxml/main.fxml"));
        stage.setTitle("Auto-moto klub");
        stage.setScene(new Scene(root, 800, 600));
        stage.show();
        stage.toFront();

        theStage = stage;
    }

    private void initFile(String file) throws IOException {
        File dbfile = new File(file);
        ClassLoader classLoader = getClass().getClassLoader();
        File srcfile = new File(classLoader.getResource("xml/" + file).getFile());
        dbfile.delete();
        Files.copy(srcfile.toPath(), dbfile.toPath());
    }

    @Test
    public void testRemoveOwner (FxRobot robot) {
        robot.clickOn("#ownersTab");
        robot.clickOn("#tableOwners");

        // Selektujemo Mehu Mehića
        //robot.press(KeyCode.DOWN).release(KeyCode.DOWN);
        //robot.clickOn("#tabelaVlasnici");
        robot.clickOn("Meho Mehic");

        robot.clickOn("#tbRemoveOwner");

        // Čekamo da dijalog postane vidljiv
        robot.lookup(".dialog-pane").tryQuery().isPresent();

        // Klik na dugme Ok
        DialogPane dialogPane = robot.lookup(".dialog-pane").queryAs(DialogPane.class);
        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        robot.clickOn(okButton);


        // Čekamo da se pojavi novi dijalog koji kaže da nije moguće brisati
        robot.lookup(".dialog-pane").tryQuery().isPresent();
        dialogPane = robot.lookup(".dialog-pane").queryAs(DialogPane.class);
        System.out.println(dialogPane.getGraphic());

        // Klik na dugme Ok
        dialogPane = robot.lookup(".dialog-pane").queryAs(DialogPane.class);
        okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        robot.clickOn(okButton);

        // Nije obrisan
        ObservableList<Owner> owners = dao.getOwners();
        assertEquals(1, owners.size());
    }

    @Test
    public void testAddRemoveOwner (FxRobot robot) {
        robot.clickOn("#ownersTab");
        robot.clickOn("#tbAddOwner");

        // Čekamo da prozor postane vidljiv
        robot.lookup("#nameField").tryQuery().isPresent();
        robot.clickOn("#nameField");
        robot.write("abc");
        robot.clickOn("#surnameField");
        robot.write("d");
        robot.clickOn("#parentNameField");
        robot.write("e");
        robot.clickOn("#addressField");
        robot.write("f");
        robot.clickOn("#dateField");
        robot.write("1/8/2003");
        robot.press(KeyCode.ENTER).release(KeyCode.ENTER);
        robot.clickOn("#jmbgField");
        robot.write("0801003500007");

        ComboBox placeOfBirth = robot.lookup("#placeOfBirth").queryAs(ComboBox.class);
        Platform.runLater(() -> placeOfBirth.show());

        // Čekamo da se pojavi meni
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        robot.clickOn("Sarajevo");

        robot.clickOn("#addressPlace");
        robot.write("Mostar");

        robot.clickOn("#postalNumberField");
        robot.write("88000");

        // Sve validno, prozor se zatvara
        robot.clickOn("#okButton");

        // Čekamo da se doda vlasnik - malo duže jer mora da se validira poštanski broj
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ObservableList<Owner> owners = dao.getOwners();
        assertEquals(2, owners.size());
        assertEquals(2, owners.get(1).getId());
        assertEquals("abc", owners.get(1).getName());
        assertEquals("Mostar", owners.get(1).getLivingPlace().getName());

        // Brišemo vlasnika
        robot.clickOn("#ownersTab");
        robot.clickOn("#tableOwners");

        // Selektujemo Mehu Mehića
        robot.press(KeyCode.DOWN).release(KeyCode.DOWN);
        robot.press(KeyCode.DOWN).release(KeyCode.DOWN);
        robot.clickOn("#tableOwners");

        robot.clickOn("#tbRemoveOwner");

        // Čekamo da dijalog postane vidljiv
        robot.lookup(".dialog-pane").tryQuery().isPresent();

        // Klik na dugme Ok
        DialogPane dialogPane = robot.lookup(".dialog-pane").queryAs(DialogPane.class);
        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        robot.clickOn(okButton);

        // Čekamo da se obriše vlasnik
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Obrisan
        ObservableList<Owner> owners2 = dao.getOwners();
        assertEquals(1, owners2.size());
        assertEquals("Meho", owners2.get(0).getName());

        // Mostar je i dalje u bazi
        ObservableList<Place> places = dao.getPlaces();
        assertEquals(3, places.size());
        // Ovo će vratiti abecedno, tako da će Mostar biti na indeksu 0 (prije Tuzle i Sarajeva)
        assertEquals("Mostar", places.get(0).getName());
        assertEquals("88000", places.get(0).getPostalNumber());
    }

    @Test
    public void testAddRemoveOwnerXml (FxRobot robot) {
        // Prebacujemo na Xml
        robot.clickOn("#menuOptions");
        robot.clickOn("#menuXml");

        robot.clickOn("#ownersTab");
        robot.clickOn("#tbAddOwner");

        // Čekamo da prozor postane vidljiv
        robot.lookup("#nameField").tryQuery().isPresent();
        robot.clickOn("#nameField");
        robot.write("abc");
        robot.clickOn("#surnameField");
        robot.write("d");
        robot.clickOn("#parentNameField");
        robot.write("e");
        robot.clickOn("#addressField");
        robot.write("f");
        robot.clickOn("#dateField");
        robot.write("1/8/2003");
        robot.press(KeyCode.ENTER).release(KeyCode.ENTER);
        robot.clickOn("#jmbgField");
        robot.write("0801003500007");

        ComboBox placeOfBirth = robot.lookup("#placeOfBirth").queryAs(ComboBox.class);
        Platform.runLater(() -> placeOfBirth.show());

        // Čekamo da se pojavi meni
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        robot.clickOn("Sarajevo");

        robot.clickOn("#addressPlace");
        robot.write("Mostar");

        robot.clickOn("#postalNumberField");
        robot.write("88000");

        // Sve validno, prozor se zatvara
        robot.clickOn("#okButton");

        // Čekamo da se doda vlasnik - malo duže jer mora da se validira poštanski broj
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Provjerićemo broj vlasnika tako što ćemo napraviti posebnu instancu dao klase
        // (u nekim implementacijama bi ovo moglo pasti, ali ne pada mi na pamet kojim)
        VehicleDAO mydao = new VehicleDAOXML();

        ObservableList<Owner> owners = mydao.getOwners();
        assertEquals(2, owners.size());
        assertEquals(2, owners.get(1).getId());
        assertEquals("abc", owners.get(1).getName());
        assertEquals("Mostar", owners.get(1).getLivingPlace().getName());
        mydao.close();

        // Brišemo vlasnika
        robot.clickOn("#ownersTab");
        robot.clickOn("#tableOwners");

        // Selektujemo Mehu Mehića
        robot.press(KeyCode.DOWN).release(KeyCode.DOWN);
        robot.press(KeyCode.DOWN).release(KeyCode.DOWN);
        robot.clickOn("#tableOwners");

        robot.clickOn("#tbRemoveOwner");

        // Čekamo da dijalog postane vidljiv
        robot.lookup(".dialog-pane").tryQuery().isPresent();

        // Klik na dugme Ok
        DialogPane dialogPane = robot.lookup(".dialog-pane").queryAs(DialogPane.class);
        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        robot.clickOn(okButton);

        // Čekamo da se obriše vlasnik
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Obrisan
        mydao = new VehicleDAOXML();
        ObservableList<Owner> owners2 = mydao.getOwners();
        assertEquals(1, owners2.size());
        assertEquals("Meho", owners2.get(0).getName());

        // Mostar je i dalje u bazi
        ObservableList<Place> places = mydao.getPlaces();
        assertEquals(3, places.size());
        // Ovo će vratiti abecedno, tako da će Mostar biti na indeksu 0 (prije Tuzle i Sarajeva)
        assertEquals("Mostar", places.get(0).getName());
        assertEquals("88000", places.get(0).getPostalNumber());
        mydao.close();

        // Vraćam se na Db
        robot.clickOn("#menuOptions");
        robot.clickOn("#menuDb");
    }

    @Test
    public void testRemoveVehicle (FxRobot robot) {
        dao.close();

        robot.clickOn("#vehiclesTab");

        // Dodajemo vozilo
        robot.clickOn("#tbAddVehicle");

        // Čekamo da se pojavi prozor
        robot.lookup("#manufacturerCombo").tryQuery().isPresent();

        robot.clickOn("#manufacturerCombo");
        robot.write("Skoda");
        robot.clickOn("#modelField");
        robot.write("Fabia");
        robot.clickOn("#chasisNumberField");
        robot.write("1234193459845");
        robot.clickOn("#plateNumberField");
        robot.write("M23-K-456");

        ComboBox ownerCombo = robot.lookup("#ownerCombo").queryAs(ComboBox.class);
        Platform.runLater(() -> ownerCombo.show());

        // Čekamo da se pojavi meni
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        robot.clickOn("Mehic Meho");

        robot.clickOn("#okButton");

        // Čekamo da se doda vozilo
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Da li je dodano
        dao = new VehicleDAOBase();
        ObservableList<Vehicle> vehicles = dao.getVehicles();
        dao.close();
        assertEquals(2, vehicles.size());
        assertEquals("Skoda", vehicles.get(1).getManufacturer().getName());
        assertEquals("Fabia", vehicles.get(1).getModel());
        assertEquals("1234193459845", vehicles.get(1).getChasisNumber());
        assertEquals("M23-K-456", vehicles.get(1).getPlateNumber());
        assertEquals("Mehic", vehicles.get(1).getOwner().getSurname());

        robot.clickOn("#tableVehicles");

        // Selektujemo Škodu
        robot.press(KeyCode.DOWN).release(KeyCode.DOWN);
        robot.press(KeyCode.DOWN).release(KeyCode.DOWN);
        robot.clickOn("#tableVehicles");

        robot.clickOn("#tbRemoveVehicle");

        // Čekamo da dijalog postane vidljiv
        robot.lookup(".dialog-pane").tryQuery().isPresent();

        // Klik na dugme Ok
        DialogPane dialogPane = robot.lookup(".dialog-pane").queryAs(DialogPane.class);
        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        robot.clickOn(okButton);

        // Čekamo da se obriše vozilo
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Nije obrisan
        dao = new VehicleDAOBase();
        ObservableList<Vehicle> vehicles2 = dao.getVehicles();
        assertEquals(1, vehicles2.size());
        assertEquals("Golf", vehicles2.get(0).getModel());
    }
}

