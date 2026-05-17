package com.garagegenius.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.garagegenius.model.Vehicle;
import com.garagegenius.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for vehicles ({@code vehicles} table).
 *
 * <p>Vehicles are linked to customers via {@code customer_id}. Queries typically join
 * through {@code customers} → {@code users} to show customer name in admin views.</p>
 */
public class VehicleDAO {
    private static final Logger logger = LoggerFactory.getLogger(VehicleDAO.class);


    /**
     * Inserts a new vehicle.
     *
     * @param vehicle vehicle record
     * @return generated vehicle id, or -1 on failure
     */
    public int addVehicle(Vehicle vehicle) {
        int generatedId = -1;
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            String sql = "INSERT INTO vehicles (customer_id, make, model, year, license_plate, color, vin_number, mileage, fuel_type) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, vehicle.getCustomerId());
            stmt.setString(2, vehicle.getMake());
            stmt.setString(3, vehicle.getModel());
            stmt.setInt(4, vehicle.getYear());
            stmt.setString(5, vehicle.getLicensePlate());
            stmt.setString(6, vehicle.getColor());
            stmt.setString(7, vehicle.getVinNumber());
            stmt.setInt(8, vehicle.getMileage());
            stmt.setString(9, vehicle.getFuelType());

            if (stmt.executeUpdate() > 0) {
                rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    generatedId = rs.getInt(1);
                    vehicle.setVehicleId(generatedId);
                }
            }
        } catch (SQLException e) {
            logger.error("Database error occurred", e);
        } finally {
            closeResources(conn, stmt, rs);
        }
        return generatedId;
    }

    /**
     * Returns vehicles for a customer.
     *
     * @param customerId customer id
     * @return list of vehicles
     */
    public List<Vehicle> getVehiclesByCustomerId(int customerId) {
        List<Vehicle> vehicles = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            String sql = "SELECT v.*, u.full_name as customer_name FROM vehicles v JOIN customers c ON v.customer_id = c.customer_id JOIN users u ON c.user_id = u.user_id WHERE v.customer_id = ? ORDER BY v.vehicle_id DESC";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, customerId);
            rs = stmt.executeQuery();
            while (rs.next()) {
                vehicles.add(extractVehicleFromResultSet(rs));
            }
        } catch (SQLException e) {
            logger.error("Database error occurred", e);
        } finally {
            closeResources(conn, stmt, rs);
        }
        return vehicles;
    }

    /**
     * @return all vehicles ordered by most recent first
     */
    public List<Vehicle> getAllVehicles() {
        List<Vehicle> vehicles = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            String sql = "SELECT v.*, u.full_name as customer_name FROM vehicles v JOIN customers c ON v.customer_id = c.customer_id JOIN users u ON c.user_id = u.user_id ORDER BY v.vehicle_id DESC";
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            while (rs.next()) {
                vehicles.add(extractVehicleFromResultSet(rs));
            }
        } catch (SQLException e) {
            logger.error("Database error occurred", e);
        } finally {
            closeResources(conn, stmt, rs);
        }
        return vehicles;
    }

    /**
     * Loads a single vehicle by id.
     *
     * @param vehicleId vehicle id
     * @return vehicle or {@code null}
     */
    public Vehicle getVehicleById(int vehicleId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            String sql = "SELECT v.*, u.full_name as customer_name FROM vehicles v JOIN customers c ON v.customer_id = c.customer_id JOIN users u ON c.user_id = u.user_id WHERE v.vehicle_id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, vehicleId);
            rs = stmt.executeQuery();
            if (rs.next()) return extractVehicleFromResultSet(rs);
        } catch (SQLException e) {
            logger.error("Database error occurred", e);
        } finally {
            closeResources(conn, stmt, rs);
        }
        return null;
    }

    /**
     * Updates a vehicle record.
     *
     * @param vehicle updated vehicle
     * @return {@code true} if updated
     */
    public boolean updateVehicle(Vehicle vehicle) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DBConnection.getConnection();
            String sql = "UPDATE vehicles SET make=?, model=?, year=?, license_plate=?, color=?, vin_number=?, mileage=?, fuel_type=? WHERE vehicle_id=?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, vehicle.getMake());
            stmt.setString(2, vehicle.getModel());
            stmt.setInt(3, vehicle.getYear());
            stmt.setString(4, vehicle.getLicensePlate());
            stmt.setString(5, vehicle.getColor());
            stmt.setString(6, vehicle.getVinNumber());
            stmt.setInt(7, vehicle.getMileage());
            stmt.setString(8, vehicle.getFuelType());
            stmt.setInt(9, vehicle.getVehicleId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Database error occurred", e);
        } finally {
            closeResources(conn, stmt, null);
        }
        return false;
    }

    /**
     * Deletes a vehicle record.
     *
     * @param vehicleId vehicle id
     * @return {@code true} if deleted
     */
    public boolean deleteVehicle(int vehicleId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DBConnection.getConnection();
            String sql = "DELETE FROM vehicles WHERE vehicle_id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, vehicleId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Database error occurred", e);
        } finally {
            closeResources(conn, stmt, null);
        }
        return false;
    }

    /**
     * Checks whether a license plate is already present (unique constraint helper).
     *
     * @param plate license plate string
     * @return {@code true} if exists
     */
    public boolean licensePlateExists(String plate) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            String sql = "SELECT COUNT(*) FROM vehicles WHERE license_plate = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, plate);
            rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            logger.error("Database error occurred", e);
        } finally {
            closeResources(conn, stmt, rs);
        }
        return false;
    }

    private Vehicle extractVehicleFromResultSet(ResultSet rs) throws SQLException {
        Vehicle v = new Vehicle();
        v.setVehicleId(rs.getInt("vehicle_id"));
        v.setCustomerId(rs.getInt("customer_id"));
        v.setMake(rs.getString("make"));
        v.setModel(rs.getString("model"));
        v.setYear(rs.getInt("year"));
        v.setLicensePlate(rs.getString("license_plate"));
        v.setColor(rs.getString("color"));
        v.setVinNumber(rs.getString("vin_number"));
        v.setMileage(rs.getInt("mileage"));
        v.setFuelType(rs.getString("fuel_type"));
        v.setCustomerName(rs.getString("customer_name"));
        return v;
    }

    private void closeResources(Connection conn, PreparedStatement stmt, ResultSet rs) {
        try {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) DBConnection.closeConnection(conn);
        } catch (SQLException e) {
            logger.error("Database error occurred", e);
        }
    }
}
