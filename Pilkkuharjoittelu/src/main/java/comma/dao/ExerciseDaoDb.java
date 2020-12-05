/**
 *
 * @author sallasal
 */
package comma.dao;

import java.io.*;
import java.sql.*;
import java.util.*;
import comma.domain.*;
import java.net.URL;

public class ExerciseDaoDb implements ExerciseDao {

    private final Connection connection;

    public ExerciseDaoDb() {
        this.connection = this.connect();
    }

    @Override
    public final Connection connect() {
        String url = "jdbc:sqlite:commas.db";

        Connection connection = null;

        try {
            connection = DriverManager.getConnection(url);
            DatabaseMetaData meta = connection.getMetaData();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return connection;
    }

    @Override
    public void initialize() throws Exception {
        String sqlCreateExs = "CREATE TABLE IF NOT EXISTS Exercises (firstpart TEXT, secondpart TEXT, comma INTEGER, category INTEGER, creator TEXT)";
        String sqlCreateUsers = "CREATE TABLE IF NOT EXISTS Users (username TEXT, name TEXT, completedCtg1 INTEGER, completedCtg2 INTEGER, completedCtg3 INTEGER)";

        try (Statement stm = connection.createStatement()) {
            stm.execute(sqlCreateExs);
            stm.execute(sqlCreateUsers);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        if (checkIfEmpty()) {
            bringExercises();
        }

    }

    private void bringExercises() throws Exception {

        String sqlExercise = "INSERT INTO Exercises (firstpart, secondpart, comma, category, creator) VALUES (?,?,?,?,?)";

        InputStream inputStream = null;
        try {
            ClassLoader classLoader = this.getClass().getClassLoader();
            inputStream = classLoader.getResourceAsStream("exercises.csv");
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String row;
            while ((row = reader.readLine()) != null) {

                if (row.trim().length() == 0) {
                    continue;
                }

                String[] exParts = row.split("\\|");

                try (PreparedStatement stm = connection.prepareStatement(sqlExercise)) {
                    stm.setString(1, exParts[0]);
                    stm.setString(2, exParts[1]);
                    stm.setInt(3, Integer.parseInt(exParts[2]));
                    stm.setInt(4, Integer.parseInt(exParts[3]));
                    stm.setString(5, "program");
                    stm.executeUpdate();
                } catch (SQLException e) {
                    System.out.println(e.getMessage());
                }

            }
            reader.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            inputStream.close();
        }
    }

    private boolean checkIfEmpty() throws Exception {
        boolean isEmpty = false;
        String sqlCount = "SELECT COUNT(*) AS count FROM Exercises";

        try (PreparedStatement stm = connection.prepareStatement(sqlCount)) {
            ResultSet results = stm.executeQuery();
            while (results.next()) {
                if (results.getInt("count") == 0) {
                    isEmpty = true;
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return isEmpty;
    }

    @Override
    public void add(Exercise exercise) {

        String sqlAdd = "INSERT INTO Exercises (firstpart, secondpart, comma, category, creator) VALUES (?,?,?,?,?)";
        int comma;

        if (exercise.getComma()) {
            comma = 1;
        } else {
            comma = 0;
        }

        try (PreparedStatement stm = connection.prepareStatement(sqlAdd)) {
            stm.setString(1, exercise.getFirstPart());
            stm.setString(2, exercise.getSecondPart());
            stm.setInt(3, comma);
            stm.setInt(4, exercise.getCategory());
            stm.setString(5, exercise.getCreator());
            stm.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

    }

    @Override
    public ArrayList<Exercise> listAll() {

        String sqlList = "SELECT firstpart, secondpart, comma, category, creator FROM Exercises";
        ArrayList<Exercise> resultList = new ArrayList<>();

        try (PreparedStatement stm = connection.prepareStatement(sqlList)) {
            ResultSet results = stm.executeQuery();

            while (results.next()) {
                String firstpart = results.getString("firstpart");
                String secondpart = results.getString("secondpart");
                int category = results.getInt("category");
                String creator = results.getString("creator");
                boolean comma = false;

                if (results.getInt("comma") == 1) {
                    comma = true;
                }

                resultList.add(new Exercise(firstpart, secondpart, comma, category, creator));
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return resultList;
    }

}
