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
public class OwnerEditTest {
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

        // Dodajemo novog vlasnika
        Place sarajevo = new Place(1, "Sarajevo", "71000");
        Owner testTestovic = new Owner(0, "Test", "Testović", "Te", LocalDate.now(), sarajevo,
                "Zmaja od Bosne bb", sarajevo, "1234567890");
        dao.addOwner(testTestovic);

        // Detaljna provjera dodavanja bi trebala biti urađena u testu dodajVlasnika()
        ObservableList<Owner> owners = dao.getOwners();
        testTestovic = owners.get(1);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/owner.fxml"));
        OwnerController ownerController = new OwnerController(dao, testTestovic);
        loader.setController(ownerController);
        Parent root = loader.load();
        stage.setTitle("Owner");
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
        Platform.runLater(() -> theStage.show());
    }

    @Test
    public void testOk (FxRobot robot) {
        robot.clickOn("#nameField");
        robot.press(KeyCode.CONTROL).press(KeyCode.A).release(KeyCode.A).release(KeyCode.CONTROL);
        robot.press(KeyCode.DELETE).release(KeyCode.DELETE);

        robot.clickOn("#okButton");
        // Forma nije validna i neće se zatvoriti!
        assertTrue(theStage.isShowing());

        TextField name = robot.lookup("#nameField").queryAs(TextField.class);
        Background bg = name.getBackground();
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
        robot.press(KeyCode.CONTROL).press(KeyCode.A).release(KeyCode.A).release(KeyCode.CONTROL);
        robot.press(KeyCode.DELETE).release(KeyCode.DELETE);

        robot.clickOn("#surnameField");
        robot.press(KeyCode.CONTROL).press(KeyCode.A).release(KeyCode.A).release(KeyCode.CONTROL);
        robot.write("d");
        robot.clickOn("#parentNameField");
        robot.press(KeyCode.CONTROL).press(KeyCode.A).release(KeyCode.A).release(KeyCode.CONTROL);
        robot.write("e");
        robot.clickOn("#addressField");
        robot.press(KeyCode.CONTROL).press(KeyCode.A).release(KeyCode.A).release(KeyCode.CONTROL);
        robot.write("f");

        robot.clickOn("#okButton");
        // Forma nije validna i neće se zatvoriti!

        TextField surname = robot.lookup("#surnameField").queryAs(TextField.class);
        Background bg = surname.getBackground();
        boolean colorFound = false;
        for (BackgroundFill bf : bg.getFills())
            if (bf.getFill().toString().contains("adff2f"))
                colorFound = true;
        assertTrue(colorFound);

        surname = robot.lookup("#parentNameField").queryAs(TextField.class);
        bg = surname.getBackground();
        colorFound = false;
        for (BackgroundFill bf : bg.getFills())
            if (bf.getFill().toString().contains("adff2f"))
                colorFound = true;
        assertTrue(colorFound);

        surname = robot.lookup("#addressField").queryAs(TextField.class);
        bg = surname.getBackground();
        colorFound = false;
        for (BackgroundFill bf : bg.getFills())
            if (bf.getFill().toString().contains("adff2f"))
                colorFound = true;
        assertTrue(colorFound);

    }

    @Test
    public void testDateValidation (FxRobot robot) {
        robot.clickOn("#dateField");
        robot.press(KeyCode.CONTROL).press(KeyCode.A).release(KeyCode.A).release(KeyCode.CONTROL);
        // Datum u budućnosti
        robot.write("1/1/2021");
        robot.press(KeyCode.ENTER).release(KeyCode.ENTER);

        robot.clickOn("#okButton");
        assertTrue(theStage.isShowing());

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
        robot.clickOn("#nameField");
        robot.press(KeyCode.CONTROL).press(KeyCode.A).release(KeyCode.A).release(KeyCode.CONTROL);
        robot.press(KeyCode.DELETE).release(KeyCode.DELETE);

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
    public void testDeletePlace (FxRobot robot) {
        robot.clickOn("#addressPlace");
        robot.press(KeyCode.CONTROL).press(KeyCode.A).release(KeyCode.A).release(KeyCode.CONTROL);
        robot.press(KeyCode.DELETE).release(KeyCode.DELETE);

        robot.clickOn("#okButton");
        assertTrue(theStage.isShowing());

        ComboBox ime = robot.lookup("#addressPlace").queryAs(ComboBox.class);
        Background bg = ime.getEditor().getBackground();
        boolean colorFound = false;
        for (BackgroundFill bf : bg.getFills())
            if (bf.getFill().toString().contains("ffb6c1"))
                colorFound = true;
        assertTrue(colorFound);
    }

    @Test
    public void testJmbgValidation (FxRobot robot) {
        robot.clickOn("#dateField");
        robot.press(KeyCode.CONTROL).press(KeyCode.A).release(KeyCode.A).release(KeyCode.CONTROL);
        robot.write("1/8/2003");
        robot.press(KeyCode.ENTER).release(KeyCode.ENTER);

        robot.clickOn("#jmbgField");
        robot.press(KeyCode.CONTROL).press(KeyCode.A).release(KeyCode.A).release(KeyCode.CONTROL);
        robot.write("1234");

        robot.clickOn("#okButton");
        assertTrue(theStage.isShowing());

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
        robot.clickOn("#nameField");
        robot.press(KeyCode.CONTROL).press(KeyCode.A).release(KeyCode.A).release(KeyCode.CONTROL);
        robot.press(KeyCode.DELETE).release(KeyCode.DELETE);

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
    public void testChange (FxRobot robot) {
        robot.clickOn("#nameField");
        robot.press(KeyCode.CONTROL).press(KeyCode.A).release(KeyCode.A).release(KeyCode.CONTROL);
        robot.write("abc");

        robot.clickOn("#addressPlace");
        robot.press(KeyCode.CONTROL).press(KeyCode.A).release(KeyCode.A).release(KeyCode.CONTROL);
        robot.write("Zenica");

        robot.clickOn("#postalNumberField");
        robot.press(KeyCode.CONTROL).press(KeyCode.A).release(KeyCode.A).release(KeyCode.CONTROL);
        robot.write("75000");
        robot.clickOn("#dateField");
        robot.press(KeyCode.CONTROL).press(KeyCode.A).release(KeyCode.A).release(KeyCode.CONTROL);
        robot.write("1/8/2003");
        robot.press(KeyCode.ENTER).release(KeyCode.ENTER);
        robot.clickOn("#jmbgField");
        robot.press(KeyCode.CONTROL).press(KeyCode.A).release(KeyCode.A).release(KeyCode.CONTROL);
        robot.write("0801003500007");

        // Sve validno, prozor se zatvara
        robot.clickOn("#okButton");

        // Čekamo da se validira poštanski broj
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
        assertEquals("Testović", owners.get(1).getSurname());
        assertEquals(LocalDate.of(2003,1,8), owners.get(1).getDateOfBirth());
        assertEquals("0801003500007", owners.get(1).getJmbg());
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


