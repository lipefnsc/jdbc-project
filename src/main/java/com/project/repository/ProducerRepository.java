package com.project.repository;

import com.project.conn.ConnectionFactory;
import com.project.domain.Producer;
import lombok.extern.log4j.Log4j2;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Log4j2
public class ProducerRepository {

    // Inserting data into DB
    public static void save(Producer producer) {
        String sql = String.format("INSERT INTO `anime_store`.`producer` (`name`) VALUES ('%s');", producer.getName());
        try (Connection conn = ConnectionFactory.getConnection();
             Statement statement = conn.createStatement()) {
            int rowsAffected = statement.executeUpdate(sql);
            log.info("Inserted producer '{}' in the database, rows affected '{}'", producer.getName(), rowsAffected);
        } catch (SQLException e) {
            log.error("Error while trying to insert producer '{}'", producer.getName(), e);
        }
    }

    public static void saveTransaction(List<Producer> producers) {
        try (Connection conn = ConnectionFactory.getConnection()) {
            conn.setAutoCommit(false); // O banco de dados só salva as informações se não houver nenhuma exception
            preparedStatementSaveTransaction(conn, producers);
            conn.commit();
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            log.error("Error while trying to save producer '{}'", producers, e);
        }
    }

    private static void preparedStatementSaveTransaction(Connection conn, List<Producer> producers) throws SQLException {
        String sql = "INSERT INTO `anime_store`.`producer` (`name`) VALUES (?);";
        boolean shouldRollback = false;
        for (Producer p : producers) {
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                log.info("Saving producer '{}'", p.getName());
                ps.setString(1, p.getName());
                ps.execute();
            } catch (SQLException e) {
                log.error("Error while trying to save producer.");
                shouldRollback = true;
            }
        }
        if(shouldRollback) {
            log.warn("Transaction is going be rollback");
            conn.rollback(); // Se houver alguma exceção durante a inserção dos dados, volte ao estágio inicial (não insira nada)
        }
    }

    // Deleting data from DB
    public static void delete(int id) {
        String sql = String.format("DELETE FROM `anime_store`.`producer` WHERE (`id` = '%d');", id);
        try (Connection conn = ConnectionFactory.getConnection();
             Statement statement = conn.createStatement()) {
            int rowsAffected = statement.executeUpdate(sql);
            log.info("Deleted producer '{}' from the database, rows affected '{}'", id, rowsAffected);
        } catch (SQLException e) {
            log.error("Error while trying to delete producer '{}'", id, e);
        }
    }

    // Updating DB data
    public static void update(Producer producer) {
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = preparedStatementUpdate(conn, producer)) {
            int rowsAffected = ps.executeUpdate();
            log.info("Updated producer '{}', rows affected '{}'", producer.getId(), rowsAffected);
        } catch (SQLException e) {
            log.error("Error while trying to update producer '{}'", producer.getId(), e);
        }
    }

    // Método para não ter que aninhar dois try with resources
    private static PreparedStatement preparedStatementUpdate(Connection conn, Producer producer) throws SQLException {
        String sql = "UPDATE `anime_store`.`producer` SET `name` = ? WHERE (`id` = ?);";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, producer.getName());
        ps.setInt(2, producer.getId());
        return ps;
    }

    // Finding all data in DB
    public static List<Producer> findAll() {
        return findByName("");
    }

    // Finding data by name
    public static List<Producer> findByName(String name) {
        log.info("Finding producers");
        List<Producer> producers = new ArrayList<>();
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = preparedStatementFindByName(conn, name);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Producer producer = Producer.builder()
                        .id(rs.getInt("id"))
                        .name(rs.getString("name"))
                        .build();
                producers.add(producer);
            }

        } catch (SQLException e) {
            log.error("Error while trying to find producers", e);
        }
        return producers;
    }

    // Método para não ter que aninhar dois try with resources
    private static PreparedStatement preparedStatementFindByName(Connection conn, String name) throws SQLException {
        String sql = "select * from anime_store.producer where name like ?;";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, String.format("%%%s%%", name));
        return ps;
    }

    // Showing producer MetaData
    public static void showProducerMetaData() {
        log.info("Showing producer metadata");
        String sql = "select * from anime_store.producer";
        try (Connection conn = ConnectionFactory.getConnection();
             Statement statement = conn.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            ResultSetMetaData rsMetaData = rs.getMetaData();
            int columnCount = rsMetaData.getColumnCount();
            log.info("Columns count '{}'", columnCount);
            for (int i = 1; i <= columnCount; i++) {
                log.info("Table name '{}'", rsMetaData.getTableName(i));
                log.info("Column name '{}'", rsMetaData.getColumnName(i));
                log.info("Column size '{}'", rsMetaData.getColumnDisplaySize(i));
                log.info("Column type '{}'", rsMetaData.getColumnTypeName(i));
            }
        } catch (SQLException e) {
            log.error("Error while trying to find producers", e);
        }
    }

    // Showing driver MetaData
    public static void showDriverMetaData() {
        log.info("Showing producer metadata");
        try (Connection conn = ConnectionFactory.getConnection()) {
            DatabaseMetaData dbMetaData = conn.getMetaData();
            if (dbMetaData.supportsResultSetType(ResultSet.TYPE_FORWARD_ONLY)) {
                log.info("Supports TYPE_FORWARD_ONLY");
                if (dbMetaData.supportsResultSetConcurrency(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE))
                    log.info("And Supports CONCUR_UPDATABLE");
            }
            if (dbMetaData.supportsResultSetType(ResultSet.TYPE_SCROLL_INSENSITIVE)) {
                log.info("Supports TYPE_SCROLL_INSENSITIVE");
                if (dbMetaData.supportsResultSetConcurrency(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE))
                    log.info("And Supports CONCUR_UPDATABLE");
            }
            if (dbMetaData.supportsResultSetType(ResultSet.TYPE_SCROLL_SENSITIVE)) {
                log.info("Supports TYPE_SCROLL_SENSITIVE");
                if (dbMetaData.supportsResultSetConcurrency(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE))
                    log.info("And Supports CONCUR_UPDATABLE");
            }
        } catch (SQLException e) {
            log.error("Error while trying to find producers", e);
        }
    }

    // Testing TYPE SCROLL methods
    public static void showTypeScrollWorking() {
        log.info("Showing producer metadata");
        String sql = "select * from anime_store.producer";
        try (Connection conn = ConnectionFactory.getConnection();
             Statement statement = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
             ResultSet rs = statement.executeQuery(sql)) {
            log.info("Last row? '{}'", rs.last());
            log.info("Row number '{}'", rs.getRow());
            log.info(Producer.builder().id(rs.getInt("id")).name(rs.getString("name")).build());

            log.info("First row? '{}'", rs.first());
            log.info("Row number '{}'", rs.getRow());
            log.info(Producer.builder().id(rs.getInt("id")).name(rs.getString("name")).build());

            log.info("Row absoulte? '{}'", rs.absolute(2));
            log.info("Row number '{}'", rs.getRow());
            log.info(Producer.builder().id(rs.getInt("id")).name(rs.getString("name")).build());

        } catch (SQLException e) {
            log.error("Error while trying to find producers", e);
        }
    }

    // Updating column name to UpperCase with ResultSet
    public static List<Producer> findByNameAndUpdateToUpperCase(String name) {
        log.info("Finding producers");
        String sql = String.format("select * from producer where name like '%%%s%%';", name);
        List<Producer> producers = new ArrayList<>();
        try (Connection conn = ConnectionFactory.getConnection();
             Statement statement = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
             ResultSet rs = statement.executeQuery(sql)) {
            while (rs.next()) {
                rs.updateString("name", rs.getString("name").toUpperCase());
                rs.updateRow();
                Producer producer = Producer.builder()
                        .id(rs.getInt("id"))
                        .name(rs.getString("name"))
                        .build();
                producers.add(producer);
            }

        } catch (SQLException e) {
            log.error("Error while trying to find producers", e);
        }
        return producers;
    }

    // Search producer by name, if not found insert
    public static List<Producer> findByNameAndInsertWhenNotFound(String name) {
        log.info("Finding producers");
        String sql = String.format("select * from producer where name like '%%%s%%';", name);
        List<Producer> producers = new ArrayList<>();
        try (Connection conn = ConnectionFactory.getConnection();
             Statement statement = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
             ResultSet rs = statement.executeQuery(sql)) {
            if (rs.next()) return producers;

            rs.moveToInsertRow(); // Movendo cursor para uma linha temporária
            rs.updateString("name", name);
            rs.insertRow();
            rs.beforeFirst();
            rs.next();
            Producer producer = Producer.builder()
                    .id(rs.getInt("id"))
                    .name(rs.getString("name"))
                    .build();
            producers.add(producer);
        } catch (SQLException e) {
            log.error("Error while trying to find producers", e);
        }
        return producers;
    }

    public static void findByNameAndDelete(String name) {
        log.info("Finding producers");
        String sql = String.format("select * from producer where name like '%%%s%%';", name);
        try (Connection conn = ConnectionFactory.getConnection();
             Statement statement = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
             ResultSet rs = statement.executeQuery(sql)) {
            while (rs.next()) {
                log.info("Deleting '{}'", rs.getString("name"));
                rs.deleteRow();
            }
        } catch (SQLException e) {
            log.error("Error while trying to find producers", e);
        }
    }
}