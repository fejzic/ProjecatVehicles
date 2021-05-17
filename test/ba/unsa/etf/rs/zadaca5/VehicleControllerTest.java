package ba.unsa.etf.rs.zadaca5;

import static org.junit.jupiter.api.Assertions.*;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.stage.Stage;
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
class VehicleControllerTest {
    Stage theStage;
    VehicleDAO dao;
    VehicleController controller;

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

        dao = new VehicleDAOBase();

        // Ovo bi trebalo da iskopira fajl iz resources u test-resources, a ipak radi i sa mavenom
        File fxml = new File("resources/fxml/vehicle.fxml");
        if (fxml.exists()) {
            File rsrc = new File("test-resources/fxml/vehicle.fxml");
            if (rsrc.exists()) rsrc.delete();
            Files.copy(fxml.toPath(), rsrc.toPath());
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/vehicle.fxml"));
        VehicleController vehicleController = new VehicleController(dao, null);
        loader.setController(vehicleController);
        Parent root = loader.load();
        stage.setTitle("Vehicle");
        stage.setScene(new Scene(root, USE_COMPUTED_SIZE, USE_COMPUTED_SIZE));
        stage.setResizable(false);
        stage.show();
        stage.toFront();

        theStage = stage;
    }

    @Test
    public void testCancel (FxRobot robot) {
        robot.clickOn("#cancelButton");
        assertFalse(theStage.isShowing());
    }

    @Test
    public void testOk (FxRobot robot) {
        robot.clickOn("#okButton");
        // Forma nije validna i neće se zatvoriti!
        assertTrue(theStage.isShowing());

        TextField ime = robot.lookup("#modelField").queryAs(TextField.class);
        Background bg = ime.getBackground();
        boolean colorFound = false;
        for (BackgroundFill bf : bg.getFills())
            if (bf.getFill().toString().contains("ffb6c1"))
                colorFound = true;
        assertTrue(colorFound);
    }

    @Test
    public void testModelValidation (FxRobot robot) {
        // Ovim testom provjeravamo sva polja čiji je uslov validnosti da polje nije prazno
        robot.clickOn("#modelField");
        robot.write("abc");
        robot.clickOn("#chasisNumberField");
        robot.write("d");

        robot.clickOn("#okButton");
        // Forma nije validna i neće se zatvoriti!

        TextField model = robot.lookup("#modelField").queryAs(TextField.class);
        Background bg = model.getBackground();
        boolean colorFound = false;
        for (BackgroundFill bf : bg.getFills()) {
            if (bf.getFill().toString().contains("adff2f"))
                colorFound = true;
        }
        assertTrue(colorFound);

        model = robot.lookup("#chasisNumberField").queryAs(TextField.class);
        bg = model.getBackground();
        colorFound = false;
        for (BackgroundFill bf : bg.getFills())
            if (bf.getFill().toString().contains("adff2f"))
                colorFound = true;
        assertTrue(colorFound);
    }



    @Test
    public void testPlateValidation (FxRobot robot) {
        robot.clickOn("#plateNumberField");
        robot.write("1234");

        robot.clickOn("#okButton");

        TextField plateNumber = robot.lookup("#plateNumberField").queryAs(TextField.class);
        Background bg = plateNumber.getBackground();
        boolean colorFound = false;
        for (BackgroundFill bf : bg.getFills())
            if (bf.getFill().toString().contains("ffb6c1"))
                colorFound = true;
        assertTrue(colorFound);

        robot.clickOn("#plateNumberField");
        robot.press(KeyCode.CONTROL).press(KeyCode.A).release(KeyCode.A).release(KeyCode.CONTROL);
        robot.write("A12345678");

        robot.clickOn("#okButton");

        plateNumber = robot.lookup("#plateNumberField").queryAs(TextField.class);
        bg = plateNumber.getBackground();
        colorFound = false;
        for (BackgroundFill bf : bg.getFills())
            if (bf.getFill().toString().contains("ffb6c1"))
                colorFound = true;
        assertTrue(colorFound);

        robot.clickOn("#plateNumberField");
        robot.press(KeyCode.CONTROL).press(KeyCode.A).release(KeyCode.A).release(KeyCode.CONTROL);
        robot.write("A12-B-678"); // slovo B ne može

        robot.clickOn("#okButton");

        plateNumber = robot.lookup("#plateNumberField").queryAs(TextField.class);
        bg = plateNumber.getBackground();
        colorFound = false;
        for (BackgroundFill bf : bg.getFills())
            if (bf.getFill().toString().contains("ffb6c1"))
                colorFound = true;
        assertTrue(colorFound);

        robot.clickOn("#plateNumberField");
        robot.press(KeyCode.CONTROL).press(KeyCode.A).release(KeyCode.A).release(KeyCode.CONTROL);
        robot.write("123-A-456");

        robot.clickOn("#okButton");

        plateNumber = robot.lookup("#plateNumberField").queryAs(TextField.class);
        bg = plateNumber.getBackground();
        colorFound = false;
        for (BackgroundFill bf : bg.getFills())
            if (bf.getFill().toString().contains("ffb6c1"))
                colorFound = true;
        assertTrue(colorFound);

        robot.clickOn("#plateNumberField");
        robot.press(KeyCode.CONTROL).press(KeyCode.A).release(KeyCode.A).release(KeyCode.CONTROL);
        robot.write("M23-K-456");

        robot.clickOn("#okButton");

        plateNumber = robot.lookup("#plateNumberField").queryAs(TextField.class);
        bg = plateNumber.getBackground();
        colorFound = false;
        for (BackgroundFill bf : bg.getFills())
            if (bf.getFill().toString().contains("adff2f"))
                colorFound = true;
        assertTrue(colorFound);
    }

    @Test
    public void testManufacturer (FxRobot robot) {
        ComboBox manufacturerCombo = robot.lookup("#manufacturerCombo").queryAs(ComboBox.class);
        Platform.runLater(() -> manufacturerCombo.show());

        // Čekamo da se pojavi meni
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        robot.clickOn("Renault");

        robot.clickOn("#okButton");

        ComboBox manufacturer = robot.lookup("#manufacturerCombo").queryAs(ComboBox.class);
        Background bg = manufacturer.getEditor().getBackground();
        boolean colorFound = false;
        for (BackgroundFill bf : bg.getFills())
            if (bf.getFill().toString().contains("adff2f"))
                colorFound = true;
        assertTrue(colorFound);

        robot.clickOn("#manufacturerCombo");
        robot.press(KeyCode.CONTROL).press(KeyCode.A).release(KeyCode.A).release(KeyCode.CONTROL);
        robot.press(KeyCode.DELETE).release(KeyCode.DELETE);

        robot.clickOn("#okButton");

        manufacturer = robot.lookup("#manufacturerCombo").queryAs(ComboBox.class);
        bg = manufacturer.getEditor().getBackground();
        colorFound = false;
        for (BackgroundFill bf : bg.getFills())
            if (bf.getFill().toString().contains("ffb6c1"))
                colorFound = true;
        assertTrue(colorFound);
    }

    @Test
    public void testAdd (FxRobot robot) {
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
        assertFalse(theStage.isShowing()); // Prozor se zatvorio

        // Da li je novo vozilo u bazi
        ObservableList<Vehicle> vehicles = dao.getVehicles();
        assertEquals(2, vehicles.size());
        assertEquals(2, vehicles.get(1).getId());
        assertEquals("Skoda", vehicles.get(1).getManufacturer().getName());
        assertEquals("Fabia", vehicles.get(1).getModel());
        assertEquals("1234193459845", vehicles.get(1).getChasisNumber());
        assertEquals("M23-K-456", vehicles.get(1).getPlateNumber());

        // Provjeravamo da li je Skoda zaista dodata u proizvodjace
        ObservableList<Manufacturer> manufacturers = dao.getManufacturers();
        assertEquals(4, manufacturers.size());
        // Ovo će vratiti abecedno, tako da će škoda biti na indeksu 2 (poslije Renault a prije Volskwagen)
        assertEquals(4, manufacturers.get(2).getId());
        assertEquals("Skoda", manufacturers.get(2).getName());
    }
}

