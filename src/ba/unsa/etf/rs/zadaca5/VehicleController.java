package ba.unsa.etf.rs.zadaca5;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.stream.IntStream;

public class VehicleController {
    private VehicleDAO dao;
    private Vehicle vehicle;
    public ComboBox manufacturerCombo;
    public ComboBox ownerCombo;

    public TextField modelField, chasisNumberField, plateNumberField;

    private ObservableList<Owner>owners; // Da ne pravimo upit svaki put
    private ObservableList<Manufacturer> manufacturers; // Da ne pravimo upit svaki put

    public VehicleController() {
        dao = null;
        vehicle = null;
    }

    public VehicleController(VehicleDAO dao, Vehicle vehicle) {
        this.dao = dao;
        this.vehicle = vehicle;
        owners = dao.getOwners();
        manufacturers = dao.getManufacturers();
    }

    @FXML
    public void initialize() {
        ownerCombo.setItems(owners);
        manufacturerCombo.setItems(manufacturers);

        if (vehicle != null) {
            manufacturerCombo.setValue(vehicle.getManufacturer().getName());
            modelField.setText(vehicle.getModel());
            chasisNumberField.setText(vehicle.getChasisNumber());
            plateNumberField.setText(vehicle.getPlateNumber());
            String prezimeIme = vehicle.getOwner().getSurname().concat(" ").concat(vehicle.getOwner().getName());
            ownerCombo.setValue(prezimeIme);
        }
    }

    public void okClick(javafx.event.ActionEvent actionEvent) {
        // Validacija
        boolean allOk = valid(modelField);
        allOk &= valid(chasisNumberField);
        if (validPlate()) {

            plateNumberField.getStyleClass().removeAll("poljeNijeIspravno");
            plateNumberField.getStyleClass().add("poljeIspravno");
        } else {
            plateNumberField.getStyleClass().removeAll("poljeIspravno");
            plateNumberField.getStyleClass().add("poljeNijeIspravno");
            allOk = false;
        }

        Manufacturer manufacturer = validManufacturer(manufacturerCombo);
        if (manufacturer == null)
            allOk = false;

        Owner owner = validOwner(ownerCombo);
        if (owner != null) {
        } else {
            allOk = false;
        }

        if (allOk) {// Dodavanje novog ili promjena
            if (vehicle != null) {
                vehicle.setManufacturer(manufacturer);
                vehicle.setModel(modelField.getText());
                vehicle.setChasisNumber(chasisNumberField.getText());
                vehicle.setPlateNumber(plateNumberField.getText());
                vehicle.setOwner(owner);
                dao.changeVehicle(vehicle);
            } else {
                vehicle = new Vehicle(0, manufacturer, modelField.getText(), chasisNumberField.getText(),
                        plateNumberField.getText(), owner);
                dao.addVehicle(vehicle);

            }

            Stage stage = (Stage) manufacturerCombo.getScene().getWindow();
            stage.close();
        } else {
            return;
        }

    }

    private boolean valid(TextField polje) {
        if (!polje.getText().trim().isEmpty()) {
            polje.getStyleClass().removeAll("poljeNijeIspravno");
            polje.getStyleClass().add("poljeIspravno");
        } else {
            polje.getStyleClass().removeAll("poljeIspravno");
            polje.getStyleClass().add("poljeNijeIspravno");
            return false;
        }
        return true;
    }

    boolean validPlate() {

        if (plateNumberField.getText().length() == 9) {
            if (plateNumberField.getText().charAt(3) == '-' && plateNumberField.getText().charAt(5) == '-') {
                char s = plateNumberField.getText().charAt(0);
                if (s != 'A' && s != 'E' && s != 'O' && s != 'K' && s != 'M')
                    return false;
                s = plateNumberField.getText().charAt(4);
                if (s != 'A' && s != 'E' && s != 'O' && s != 'K' && s != 'M')
                    return false;

                return IntStream.range(1, 9).filter(i -> i < 3 || i > 5).allMatch(i -> Character.isDigit(plateNumberField.getText().charAt(i)));
            } else {
                return false;
            }
        } else {
            return false;
        }

    }

    private Manufacturer validManufacturer(ComboBox manufacturerCombo) {
        // ComboBox je prazan (null)
        if (manufacturerCombo.getValue() != null) {
            Manufacturer manufacturer = null;
            String nameManufacturer = manufacturerCombo.getValue().toString().trim();
            int i = 0;
            while (i < manufacturers.size()) {
                Manufacturer p = manufacturers.get(i);
                if (!p.getName().equals(nameManufacturer)) {
                } else {
                    manufacturer = p;
                }
                i++;
            }
            if (manufacturer != null) {
                manufacturerCombo.getStyleClass().removeAll("poljeNijeIspravno");
                manufacturerCombo.getStyleClass().add("poljeIspravno");
            } else {
                if (!nameManufacturer.trim().isEmpty()) {
                    manufacturerCombo.getStyleClass().removeAll("poljeNijeIspravno");
                    manufacturerCombo.getStyleClass().add("poljeIspravno");
                    manufacturer = new Manufacturer(0, nameManufacturer);
                } else {
                    manufacturerCombo.getStyleClass().removeAll("poljeIspravno");
                    manufacturerCombo.getStyleClass().add("poljeNijeIspravno");
                }
            }
            return manufacturer;
        } else {
            manufacturerCombo.getStyleClass().removeAll("poljeIspravno");
            manufacturerCombo.getStyleClass().add("poljeNijeIspravno");
            return null;
        }

    }

    private Owner validOwner(ComboBox OwnerCombo) {
        // ComboBox je prazan (null)
        if (OwnerCombo.getValue() != null) {
            Owner owner = null;
            String nameOwner = OwnerCombo.getValue().toString().trim();
            int i = 0;
            if (i < owners.size()) {
                do {
                    Owner v = owners.get(i);
                    if (v.toString().equals(nameOwner)) {
                        owner = v;
                    }
                    i++;
                } while (i < owners.size());
            }
            // Sigurno postoji!
            OwnerCombo.getStyleClass().removeAll("poljeNijeIspravno");
            OwnerCombo.getStyleClass().add("poljeIspravno");
            return owner;
        } else {
            OwnerCombo.getStyleClass().removeAll("poljeIspravno");
            OwnerCombo.getStyleClass().add("poljeNijeIspravno");
            return null;
        }

    }

    public void cancelClick(ActionEvent actionEvent) {
        Stage stage = (Stage) manufacturerCombo.getScene().getWindow();
        stage.close();
    }
}
