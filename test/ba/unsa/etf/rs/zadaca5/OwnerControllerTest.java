package ba.unsa.etf.rs.zadaca5;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static javafx.scene.layout.Region.USE_COMPUTED_SIZE;
import static org.junit.jupiter.api.Assertions.*;

import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;

@ExtendWith(ApplicationExtension.class)
class OwnerControllerTest {
    Stage theStage;
    VehicleDAO dao;
    OwnerController controller;

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
        File fxml = new File("resources/fxml/owner.fxml");
        if (fxml.exists()) {
            File rsrc = new File("test-resources/fxml/owner.fxml");
            if (rsrc.exists()) rsrc.delete();
            Files.copy(fxml.toPath(), rsrc.toPath());
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/owner.fxml"));
        OwnerController ownerController = new OwnerController(dao, null);
        loader.setController(ownerController);
        Parent root = loader.load();
        stage.setTitle("Vlasnik");
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

        TextField ime = robot.lookup("#nameField").queryAs(TextField.class);
        Background bg = ime.getBackground();
        boolean colorFound = false;
        for (BackgroundFill bf : bg.getFills())
            if (bf.getFill().toString().contains("ffb6c1"))
                colorFound = true;
        assertTrue(colorFound);
    }

    @Test
    public void testNameValidation (FxRobot robot) {
        // Ovim testom provjeravamo sva polja čiji je uslov validnosti da polje nije prazno
        robot.clickOn("#nameField");
        robot.write("abc");
        robot.clickOn("#surnameField");
        robot.write("d");
        robot.clickOn("#parentNameField");
        robot.write("e");
        robot.clickOn("#addressField");
        robot.write("f");

        robot.clickOn("#okButton");
        // Forma nije validna i neće se zatvoriti!

        TextField ime = robot.lookup("#nameField").queryAs(TextField.class);
        Background bg = ime.getBackground();
        boolean colorFound = false;
        for (BackgroundFill bf : bg.getFills()) {
            if (bf.getFill().toString().contains("adff2f"))
                colorFound = true;
        }
        assertTrue(colorFound);

        ime = robot.lookup("#surnameField").queryAs(TextField.class);
        bg = ime.getBackground();
        colorFound = false;
        for (BackgroundFill bf : bg.getFills())
            if (bf.getFill().toString().contains("adff2f"))
                colorFound = true;
        assertTrue(colorFound);

        ime = robot.lookup("#parentNameField").queryAs(TextField.class);
        bg = ime.getBackground();
        colorFound = false;
        for (BackgroundFill bf : bg.getFills())
            if (bf.getFill().toString().contains("adff2f"))
                colorFound = true;
        assertTrue(colorFound);

        ime = robot.lookup("#addressField").queryAs(TextField.class);
        bg = ime.getBackground();
        colorFound = false;
        for (BackgroundFill bf : bg.getFills())
            if (bf.getFill().toString().contains("adff2f"))
                colorFound = true;
        assertTrue(colorFound);

    }

    @Test
    public void testDateValidation (FxRobot robot) {
        robot.clickOn("#dateField");
        // Datum u budućnosti
        robot.write("1/1/2021");

        robot.clickOn("#okButton");

        DatePicker date = robot.lookup("#dateField").queryAs(DatePicker.class);
        Background bg = date.getEditor().getBackground();
        boolean colorFound = false;
        for (BackgroundFill bf : bg.getFills())
            if (bf.getFill().toString().contains("ffb6c1"))
                colorFound = true;
        assertTrue(colorFound);

        robot.clickOn("#dateField");
        robot.press(KeyCode.CONTROL).press(KeyCode.A).release(KeyCode.A).release(KeyCode.CONTROL);
        robot.write("1/1/2019");
        robot.press(KeyCode.ENTER).release(KeyCode.ENTER);

        robot.clickOn("#okButton");

        date = robot.lookup("#dateField").queryAs(DatePicker.class);
        bg = date.getEditor().getBackground();
        colorFound = false;
        for (BackgroundFill bf : bg.getFills())
            if (bf.getFill().toString().contains("adff2f"))
                colorFound = true;
        assertTrue(colorFound);
    }

    @Test
    public void testNewPlace (FxRobot robot) {
        // Ako se unese novo mjesto prebivališta, polje poštanski broj ne smije biti prazno
        robot.clickOn("#addressPlace");
        robot.write("Zenica");

        robot.clickOn("#okButton");

        TextField postalNumber = robot.lookup("#postalNumberField").queryAs(TextField.class);
        Background bg = postalNumber.getBackground();
        boolean colorFound = false;
        for (BackgroundFill bf : bg.getFills())
            if (bf.getFill().toString().contains("ffb6c1"))
                colorFound = true;
        assertTrue(colorFound);

        robot.clickOn("#postalNumberField");
        robot.press(KeyCode.CONTROL).press(KeyCode.A).release(KeyCode.A).release(KeyCode.CONTROL);
        robot.write("75000");

        robot.clickOn("#okButton");

        // Dajemo vremena da se validira poštanski broj
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        postalNumber = robot.lookup("#postalNumberField").queryAs(TextField.class);
        bg = postalNumber.getBackground();
        colorFound = false;
        for (BackgroundFill bf : bg.getFills())
            if (bf.getFill().toString().contains("adff2f"))
                colorFound = true;
        assertTrue(colorFound);
    }

    @Test
    public void testPlaces (FxRobot robot) {
        ComboBox addressPlace = robot.lookup("#addressPlace").queryAs(ComboBox.class);
        Platform.runLater(() -> addressPlace.show());

        // Čekamo da se pojavi meni
        try {
            Thread.sleep(400);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        robot.clickOn("Sarajevo");
        //robot.press(KeyCode.DOWN).press(KeyCode.ENTER);

        String mjesto = robot.lookup("#addressPlace").queryAs(ComboBox.class).getValue().toString();
        assertEquals("Sarajevo", mjesto);

        String postanskiBroj = robot.lookup("#postalNumberField").queryAs(TextField.class).getText();
        assertEquals("71000", postanskiBroj);

    }

    @Test
    public void testJmbgValidation (FxRobot robot) {
        robot.clickOn("#dateField");
        robot.write("1/8/2003");
        robot.press(KeyCode.ENTER).release(KeyCode.ENTER);

        robot.clickOn("#jmbgField");
        robot.write("1234");

        robot.clickOn("#okButton");

        TextField jmbg = robot.lookup("#jmbgField").queryAs(TextField.class);
        Background bg = jmbg.getBackground();
        boolean colorFound = false;
        for (BackgroundFill bf : bg.getFills())
            if (bf.getFill().toString().contains("ffb6c1"))
                colorFound = true;
        assertTrue(colorFound);

        robot.clickOn("#jmbgField");
        robot.press(KeyCode.CONTROL).press(KeyCode.A).release(KeyCode.A).release(KeyCode.CONTROL);
        robot.write("080100350");

        robot.clickOn("#okButton");

        jmbg = robot.lookup("#jmbgField").queryAs(TextField.class);
        bg = jmbg.getBackground();
        colorFound = false;
        for (BackgroundFill bf : bg.getFills())
            if (bf.getFill().toString().contains("ffb6c1"))
                colorFound = true;
        assertTrue(colorFound);

        robot.clickOn("#jmbgField");
        robot.press(KeyCode.CONTROL).press(KeyCode.A).release(KeyCode.A).release(KeyCode.CONTROL);
        robot.write("0801003500006");

        robot.clickOn("#okButton");

        jmbg = robot.lookup("#jmbgField").queryAs(TextField.class);
        bg = jmbg.getBackground();
        colorFound = false;
        for (BackgroundFill bf : bg.getFills())
            if (bf.getFill().toString().contains("ffb6c1"))
                colorFound = true;
        assertTrue(colorFound);

        robot.clickOn("#jmbgField");
        robot.press(KeyCode.CONTROL).press(KeyCode.A).release(KeyCode.A).release(KeyCode.CONTROL);
        robot.write("0801003500007");

        robot.clickOn("#okButton");

        jmbg = robot.lookup("#jmbgField").queryAs(TextField.class);
        bg = jmbg.getBackground();
        colorFound = false;
        for (BackgroundFill bf : bg.getFills())
            if (bf.getFill().toString().contains("adff2f"))
                colorFound = true;
        assertTrue(colorFound);
    }

    @Test
    public void testAdding (FxRobot robot) {
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
        robot.write("Zenica");

        robot.clickOn("#postalNumberField");
        robot.write("75000");

        // Sve validno, prozor se zatvara
        robot.clickOn("#okButton");

        // Dajemo vremena da se validira poštanski broj
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertFalse(theStage.isShowing());

        // Da li je novi vlasnik u bazi
        ObservableList<Owner> owners = dao.getOwners();
        assertEquals(2, owners.size());
        assertEquals(2, owners.get(1).getId());
        assertEquals("abc", owners.get(1).getName());
        assertEquals("d", owners.get(1).getSurname());
        assertEquals("e", owners.get(1).getParentName());
        assertEquals("f", owners.get(1).getLivingAddress());
        assertEquals(LocalDate.of(2003,1,8), owners.get(1).getDateOfBirth());
        assertEquals("0801003500007", owners.get(1).getJmbg());
        assertEquals("Sarajevo", owners.get(1).getPlaceOfBirth().getName());
        assertEquals(1, owners.get(1).getPlaceOfBirth().getId());
        assertEquals("Zenica", owners.get(1).getLivingPlace().getName());
        assertEquals(3, owners.get(1).getLivingPlace().getId());

        // Provjeravamo da li je Zenica zaista dodata u mjesta
        ObservableList<Place> places = dao.getPlaces();
        assertEquals(3, places.size());
        assertEquals(3, places.get(2).getId());
        assertEquals("Zenica", places.get(2).getName());
        assertEquals("75000", places.get(2).getPostalNumber());
    }
}

