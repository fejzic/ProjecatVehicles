package ba.unsa.etf.rs.zadaca5;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
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

public class OwnerController {
    private VehicleDAO dao;
    private Owner owner;
    public ComboBox placeOfBirth;
    public ComboBox addressPlace;

    public TextField nameField, surnameField, parentNameField, postalNumberField, addressField, jmbgField;
    public DatePicker dateField;

    private ObservableList<Place> placeObservableList;

    public OwnerController() {
        dao = null;
        owner = null;
    }

    public OwnerController(VehicleDAO dao, Owner vlasnik) {
        this.dao = dao;
        this.owner = vlasnik;
        placeObservableList = dao.getPlaces();
    }

    @FXML
    public void initialize() {
        placeOfBirth.setItems(placeObservableList);
        addressPlace.setItems(placeObservableList);

        if (owner != null) {
            nameField.setText(owner.getName());
            surnameField.setText(owner.getSurname());
            parentNameField.setText(owner.getParentName());
            dateField.setValue(owner.getDateOfBirth());
            placeOfBirth.setValue(owner.getLivingPlace().getName());
            addressField.setText(owner.getLivingAddress());
            addressPlace.setValue(owner.getLivingPlace().getName());
            postalNumberField.setText(owner.getLivingPlace().getPostalNumber());
            jmbgField.setText(owner.getJmbg());
        }
    }


    public void okClick(ActionEvent actionEvent) {

        boolean allOk = valid(nameField);
        allOk &= valid(surnameField);
        allOk &= valid(parentNameField);
        allOk &= valid(addressField);

        if (dateField.getValue() == null || dateField.getValue().isAfter(LocalDate.now())) {
            dateField.getStyleClass().removeAll("poljeIspravno");
            dateField.getStyleClass().add("poljeNijeIspravno");
            allOk = false;
        } else {
            dateField.getStyleClass().removeAll("poljeNijeIspravno");
            dateField.getStyleClass().add("poljeIspravno");
        }


        if (!validJmbg(jmbgField.getText())) {
            jmbgField.getStyleClass().removeAll("poljeIspravno");
            jmbgField.getStyleClass().add("poljeNijeIspravno");
            allOk = false;
        } else {
            jmbgField.getStyleClass().removeAll("poljeNijeIspravno");
            jmbgField.getStyleClass().add("poljeIspravno");
        }


        Place palceOfBirth = validPlace(placeOfBirth, null);
        if (palceOfBirth == null)
            allOk = false;


        Place placeOfLive = validPlace(addressPlace, postalNumberField);
        if (placeOfLive != null) {
        } else {
            allOk = false;
        }

        if (allOk) {
            for (int i = 0; i < placeObservableList.size(); i++) {
                Place p = placeObservableList.get(i);
                if (!p.getName().equals(placeOfLive.getName())) {
                    continue;
                }
                addChangeOwner(palceOfBirth, placeOfLive);
                return;
            }


            try {
                URL lokacija = new URL("http://c9.etf.unsa.ba/proba/postanskiBroj.php?postanskiBroj=" + postalNumberField.getText());
                postalNumberField.getStyleClass().removeAll("poljeIspravno");
                postalNumberField.getStyleClass().removeAll("poljeNijeIspravno");
                postalNumberField.getStyleClass().add("poljeProvjeraUToku");
                new Thread(() -> {
                    String a = "";
                    String line = null;
                    BufferedReader ulaz = null;
                    try {
                        ulaz = new BufferedReader(new InputStreamReader(lokacija.openStream(),
                                StandardCharsets.UTF_8));
                        while ((line = ulaz.readLine()) != null)
                            a = a + line;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if (!a.equals("OK")) {
                        Platform.runLater(() -> {
                            postalNumberField.getStyleClass().removeAll("poljeProvjeraUToku");
                            postalNumberField.getStyleClass().add("poljeNijeIspravno");
                        });

                    } else {
                        addChangeOwner(palceOfBirth, placeOfLive);

                        Platform.runLater(() -> {
                            postalNumberField.getStyleClass().removeAll("poljeProvjeraUToku");
                            postalNumberField.getStyleClass().add("poljeIspravno");
                            Stage stage = (Stage) placeOfBirth.getScene().getWindow();
                            stage.close();
                        });
                    }
                }).start();
            } catch (Exception e) {

            }
        } else {
            return;
        }


    }

    private void addChangeOwner(Place palceOfBirth, Place placeOfLive) {

        if (owner != null) {
            owner.setName(nameField.getText());
            owner.setSurname(surnameField.getText());
            owner.setParentName(parentNameField.getText());
            owner.setDateOfBirth(dateField.getValue());
            owner.setPlaceOfBirth(palceOfBirth);
            owner.setLivingAddress(addressField.getText());
            owner.setLivingPlace(placeOfLive);
            owner.setJmbg(jmbgField.getText());
            dao.changeOwner(owner);
        } else {
            owner = new Owner(0, nameField.getText(), surnameField.getText(), parentNameField.getText(),
                    dateField.getValue(), palceOfBirth, addressField.getText(), placeOfLive, jmbgField.getText());
            dao.addOwner(owner);

        }
    }

    private Place validPlace(ComboBox placeCombo, TextField postalNumbFld) {
        // ComboBox je prazan (null)
        if (placeCombo.getValue() != null) {
            Place place = null;
            String namePlace = placeCombo.getValue().toString().trim();
            int i = 0;
            while (i < placeObservableList.size()) {
                Place m = placeObservableList.get(i);
                if (!m.getName().equals(namePlace)) {
                    i++;
                    continue;
                }
                place  = m;
                i++;
            }
            if (place != null) {

                placeCombo.getStyleClass().removeAll("poljeNijeIspravno");
                placeCombo.getStyleClass().add("poljeIspravno");


                if (postalNumbFld != null && (!valid(postalNumbFld) || !postalNumbFld.getText().equals(place.getPostalNumber()))) {
                    postalNumbFld.getStyleClass().removeAll("poljeIspravno");
                    postalNumbFld.getStyleClass().add("poljeNijeIspravno");
                    return null;
                }
            } else {

                if (namePlace.trim().isEmpty()) {
                    placeCombo.getStyleClass().removeAll("poljeIspravno");
                    placeCombo.getStyleClass().add("poljeNijeIspravno");
                } else {
                    placeCombo.getStyleClass().removeAll("poljeNijeIspravno");
                    placeCombo.getStyleClass().add("poljeIspravno");


                    String postNumber = "";
                    if (postalNumbFld != null) {
                        if (!valid(postalNumbFld)) {
                            postalNumbFld.getStyleClass().removeAll("poljeIspravno");
                            postalNumbFld.getStyleClass().add("poljeNijeIspravno");
                            return place;
                        }


                        try {
                            Integer.parseInt(postalNumbFld.getText());
                        } catch (Exception e) {
                            postalNumbFld.getStyleClass().removeAll("poljeIspravno");
                            postalNumbFld.getStyleClass().add("poljeNijeIspravno");
                            return place;
                        }
                        postNumber = postalNumbFld.getText();
                    }
                    place = new Place(0, namePlace, postNumber);
                }

            }
            return place;
        } else {
            placeCombo.getStyleClass().removeAll("poljeIspravno");
            placeCombo.getStyleClass().add("poljeNijeIspravno");
            return null;
        }

    }

    private boolean validJmbg(String jmbg) {



            return true;



    }

    private boolean valid(TextField polje) {
        if (polje.getText().trim().isEmpty()) {
            polje.getStyleClass().removeAll("poljeIspravno");
            polje.getStyleClass().add("poljeNijeIspravno");
            return false;
        } else {
            polje.getStyleClass().removeAll("poljeNijeIspravno");
            polje.getStyleClass().add("poljeIspravno");
        }
        return true;
    }

    public void cancelClick(ActionEvent actionEvent) {
        Stage stage = (Stage) placeOfBirth.getScene().getWindow();
        stage.close();
    }

    public void placeChange(ActionEvent actionEvent) {
        String namePlace = addressPlace.getValue().toString().trim();
        int i = placeObservableList.size() - 1;
        while (i >= 0) {
            Place m = placeObservableList.get(i);
            if (!m.getName().equals(namePlace)) {
                i--;
                continue;
            }
            postalNumberField.setText(m.getPostalNumber());
            i--;
        }

    }
}
