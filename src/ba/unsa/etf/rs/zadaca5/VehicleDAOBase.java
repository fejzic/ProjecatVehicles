package ba.unsa.etf.rs.zadaca5;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.time.LocalDate;

public class VehicleDAOBase implements VehicleDAO {
    private static VehicleDAOBase instance = null;
    private Connection connection;
    private PreparedStatement getOwnerquery,  getManufacturerQuery, ownerQuery,placeQuery,addNewIdPlaceQuery,addPlaceQuery,addNewIdOwnerQuery,addOwnerQuery,changeOwnerQuery,
            deleteOwnerQuery,addVehicleForOwner,addNewIdManufacturerQuery,addManufacturerQuery,addNewIdVehicleQuery,addVehicleOuery,changeVehicleQuery,deleteVehicleQuery;


    public static VehicleDAOBase getInstance(){
        if(instance == null)
        instance = new VehicleDAOBase();

        return instance;
    }


     public VehicleDAOBase(){
         try {
             connection = DriverManager.getConnection("jdbc:sqlite:vehicles.db");
             // getPlaceQuery = connection.prepareStatement("SELECT * FROM place WHERE id=?");
             getManufacturerQuery = connection.prepareStatement("SELECT * FROM manufacturer WHERE id=?");
             ownerQuery = connection.prepareStatement("SELECT * FROM owner WHERE id=?");

             placeQuery = connection.prepareStatement("SELECT * FROM place where id=? ");

             addNewIdPlaceQuery = connection.prepareStatement("SELECT MAX(id)+1 FROM place");
             addPlaceQuery = connection.prepareStatement("INSERT INTO place VALUES(?,?,?)");

             addNewIdOwnerQuery = connection.prepareStatement("SELECT MAX(id)+1 FROM owner");
             addOwnerQuery = connection.prepareStatement("INSERT INTO owner VALUES(?,?,?,?,?,?,?,?,?)");
             changeOwnerQuery = connection.prepareStatement("UPDATE owner SET name=? ,surname=?, parent_name=?, date_of_birth=?,place_of_birth=?,living_address=?,living_place=?,jmbg=? WHERE id=?");
             deleteOwnerQuery = connection.prepareStatement("DELETE FROM owner WHERE id=?");

             addNewIdManufacturerQuery = connection.prepareStatement("SELECT MAX(id)+1 FROM manufacturer");
             addManufacturerQuery = connection.prepareStatement("INSERT INTO manufacturer VALUES(?,?)");

             addNewIdVehicleQuery = connection.prepareStatement("SELECT MAX(id)+1 FROM vehicle");
             addVehicleOuery = connection.prepareStatement("INSERT INTO vehicle VALUES(?,?,?,?,?,?)");
             changeVehicleQuery = connection.prepareStatement("UPDATE vehicle SET manufacturer=?, model=?, chasis_number=?, plate_number=?, owner=? WHERE id=?");
             deleteVehicleQuery = connection.prepareStatement("DELETE FROM vehicle WHERE id=?");

             addVehicleForOwner = connection.prepareStatement("SELECT COUNT(*) FROM vehicle WHERE owner=?");


         } catch (SQLException e) {
             e.printStackTrace();
         }
     }

    @Override
    public ObservableList<Owner> getOwners() {
        ObservableList<Owner> owners = FXCollections.observableArrayList();
        Statement statement = null;
        try {
            statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM owner");
            while (true) {
                if (resultSet.next()) {
                    Owner owner = getOwnerByResultSet(resultSet);
                    owners.add(owner);
                } else {
                    break;
                }

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return owners;
    }

    private Owner getOwnerByResultSet(ResultSet resultSet) throws SQLException {

            placeQuery.setInt(1, resultSet.getInt(6));
            ResultSet resultSet1 = placeQuery.executeQuery();
            Place birth = null;
            while (true) {
                if (!resultSet1.next()) break;
                birth = new Place(resultSet1.getInt(1), resultSet1.getString(2), resultSet1.getString(3));
            }

            placeQuery.setInt(1, resultSet.getInt(8));
            resultSet1 = placeQuery.executeQuery();
            Place residence = null;
            while (true) {
                if (!resultSet1.next()) break;
                residence = new Place(resultSet1.getInt(1), resultSet1.getString(2), resultSet1.getString(3));
            }
            LocalDate dateOfBirth = resultSet.getDate(5).toLocalDate();

            Owner owner = new Owner(resultSet.getInt(1), resultSet.getString(2), resultSet.getString(3), resultSet.getString(4), dateOfBirth, birth, resultSet.getString(7), residence, resultSet.getString(9));


            return owner;



    }



    @Override
    public ObservableList<Vehicle> getVehicles() {
        ObservableList<Vehicle> vehicles = FXCollections.observableArrayList();
        Statement statement = null;
        try {
            statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM vehicle ");
            while(true){
                if (!resultSet.next()) break;
                getManufacturerQuery.setInt(1,resultSet.getInt(2));
                ResultSet resultSet1 = getManufacturerQuery.executeQuery();
                Manufacturer manufacturer = null;
                while (resultSet1.next()){
                    manufacturer = new Manufacturer(resultSet1.getInt(1),resultSet1.getString(2));
                }
                ownerQuery.setInt(1,resultSet.getInt(6));
                ResultSet resultSet2 = ownerQuery.executeQuery();
                Owner owner  = null;
                if (!resultSet2.next()) {
                } else {
                    owner = getOwnerByResultSet(resultSet2);
                }
                Vehicle vehicle = new Vehicle(resultSet.getInt(1),manufacturer, resultSet.getString(3),resultSet.getString(4),resultSet.getString(5),owner);
                vehicles.add(vehicle);

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }


        return vehicles;
    }

    @Override
    public ObservableList<Place> getPlaces() {
        ObservableList<Place> places = FXCollections.observableArrayList();
        Statement statement = null;
        try {
            statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM place ORDER BY name");
            if (resultSet.next()) {
                do {
                    Place place = new Place(resultSet.getInt(1), resultSet.getString(2), resultSet.getString(3));
                    places.add(place);
                } while (resultSet.next());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return places;
    }

    @Override
    public ObservableList<Manufacturer> getManufacturers() {
        ObservableList<Manufacturer> manufacturers = FXCollections.observableArrayList();
        Statement statement = null;
        try {
            statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM manufacturer ORDER BY name");
            if (resultSet.next()) {
                do {
                    Manufacturer manufacturer = new Manufacturer(resultSet.getInt(1), resultSet.getString(2));
                    manufacturers.add(manufacturer);
                } while (resultSet.next());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return manufacturers;
    }

    private Place addPlaceIfDoesntExist(Place place){

        try {
            placeQuery.setInt(1,place.getId());
            ResultSet resultSet = placeQuery.executeQuery();
            if (resultSet.next()) {
                return place;
            } else {
                int newId = 1;
                ResultSet resultSet1 = addNewIdPlaceQuery.executeQuery();
                if (!resultSet1.next()) {
                } else {
                    newId = resultSet1.getInt(1);
                }
                addPlaceQuery.setInt(1, newId);
                addPlaceQuery.setString(2, place.getName());
                addPlaceQuery.setString(3, place.getPostalNumber());
                addPlaceQuery.executeUpdate();
                place.setId(newId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return place;
    }

    @Override
    public void addOwner(Owner owner) {

        Statement statement=null;
        try {
            System.out.println(owner.getPlaceOfBirth().getName());
            owner.setPlaceOfBirth(addPlaceIfDoesntExist(owner.getPlaceOfBirth()));
            owner.setLivingPlace(addPlaceIfDoesntExist(owner.getLivingPlace()));
            statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT MAX(id)+1 FROM owner");
            int id=1;
            if (!resultSet.next()) {
            } else {
                id = resultSet.getInt(1);
            }
            owner.setId(id);

            addOwnerQuery.setInt(1,owner.getId());
            addOwnerQuery.setString(2,owner.getName());
            addOwnerQuery.setString(3,owner.getSurname());
            addOwnerQuery.setString(4,owner.getParentName());
            addOwnerQuery.setDate(5,Date.valueOf(owner.getDateOfBirth()));
            addOwnerQuery.setInt(6,owner.getPlaceOfBirth().getId());
            addOwnerQuery.setString(7,owner.getLivingAddress());
            addOwnerQuery.setInt(8,owner.getLivingPlace().getId());
            addOwnerQuery.setString(9,owner.getJmbg());
            addOwnerQuery.executeUpdate();


        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void changeOwner(Owner owner) {

        try {
           owner.setPlaceOfBirth( addPlaceIfDoesntExist(owner.getPlaceOfBirth()));
           owner.setLivingPlace( addPlaceIfDoesntExist(owner.getLivingPlace()));
            changeOwnerQuery.setInt(9,owner.getId());
            changeOwnerQuery.setString(1,owner.getName());
            changeOwnerQuery.setString(2,owner.getSurname());
            changeOwnerQuery.setString(3,owner.getParentName());
            changeOwnerQuery.setDate(4,Date.valueOf(owner.getDateOfBirth()));
            changeOwnerQuery.setInt(5,owner.getPlaceOfBirth().getId());
            changeOwnerQuery.setString(6,owner.getLivingAddress());
            changeOwnerQuery.setInt(7,owner.getLivingPlace().getId());
            changeOwnerQuery.setString(8,owner.getJmbg());
            changeOwnerQuery.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void deleteOwner(Owner owner) {

        try {
            addVehicleForOwner.setInt(1,owner.getId());
            ResultSet resultSet = addVehicleForOwner.executeQuery();
            if (resultSet.next() && resultSet.getInt(1) > 0) throw new IllegalArgumentException("Owner have vehicles!");
            deleteOwnerQuery.setInt(1,owner.getId());
            deleteOwnerQuery.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }


    }

    private Manufacturer addManufacturerIfDoesntExist(Manufacturer manufacturer){
        try {
            getManufacturerQuery.setInt(1,manufacturer.getId());
            ResultSet resultSet = getManufacturerQuery.executeQuery();
            if (resultSet.next()) {
                return manufacturer;
            } else {
                int newId = 1;
                ResultSet resultSet1 = addNewIdManufacturerQuery.executeQuery();
                if (!resultSet1.next()) {
                } else {
                    newId = resultSet1.getInt(1);
                }

                addManufacturerQuery.setInt(1, newId);
                addManufacturerQuery.setString(2, manufacturer.getName());
                addManufacturerQuery.executeUpdate();
                manufacturer.setId(newId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return manufacturer;
    }

    @Override
    public void addVehicle(Vehicle vehicle) {

        vehicle.setManufacturer(addManufacturerIfDoesntExist(vehicle.getManufacturer()));
        try {
            ownerQuery.setInt(1,vehicle.getOwner().getId());
            ResultSet resultSet = ownerQuery.executeQuery();
            if (!resultSet.next()) throw new IllegalArgumentException("Nepoznat vlasnik sa id-om " + vehicle.getOwner().getId());
            resultSet = addNewIdVehicleQuery.executeQuery();
            int id = 1;
            if (!resultSet.next()) {
            } else {
                id=resultSet.getInt(1);
            }
            vehicle.setId(id);

            addVehicleOuery.setInt(1,vehicle.getId());
            addVehicleOuery.setInt(2,vehicle.getManufacturer().getId());
            addVehicleOuery.setString(3,vehicle.getModel());
            addVehicleOuery.setString(4,vehicle.getChasisNumber());
            addVehicleOuery.setString(5,vehicle.getPlateNumber());
            addVehicleOuery.setInt(6,vehicle.getOwner().getId());
            addVehicleOuery.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void changeVehicle(Vehicle vehicle) {

        vehicle.setManufacturer(addManufacturerIfDoesntExist(vehicle.getManufacturer()));

        try {
            ownerQuery.setInt(1,vehicle.getOwner().getId());
            ResultSet resultSet = ownerQuery.executeQuery();

            assert resultSet.next() : "Nepoznat vlasnik sa id-om " + vehicle.getOwner().getId();
            changeVehicleQuery.setInt(6,vehicle.getId());
            changeVehicleQuery.setInt(1,vehicle.getManufacturer().getId());
            changeVehicleQuery.setString(2,vehicle.getModel());
            changeVehicleQuery.setString(3,vehicle.getChasisNumber());
            changeVehicleQuery.setString(4,vehicle.getPlateNumber());
            changeVehicleQuery.setInt(5,vehicle.getOwner().getId());
            changeVehicleQuery.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void deleteVehicle(Vehicle vehicle) {

        try {
            deleteVehicleQuery.setInt(1,vehicle.getId());
            deleteVehicleQuery.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void close() {

        try {

            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}
