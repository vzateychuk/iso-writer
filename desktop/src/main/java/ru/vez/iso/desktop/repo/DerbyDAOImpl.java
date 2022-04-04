package ru.vez.iso.desktop.repo;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import ru.vez.iso.shared.dao.ExCardDAO;
import ru.vez.iso.shared.model.ExCard;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

public class DerbyDAOImpl implements ExCardDAO {

    public static final String dbConnString = "jdbc:derby:desktop/db";

    private Connection conn;
    private QueryRunner dbAccess = new QueryRunner();

    public DerbyDAOImpl() throws Exception {
        setup();
        connect();
    }

    public void setup() throws Exception {
        conn = DriverManager.getConnection(dbConnString + ";create=true");
        String sql = "CREATE TABLE ExCard (\n" +
                "\tid INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),\n" +
                "\tname varchar(30) NOT NULL,\n" +
                "\tstate varchar(50) NOT NULL,\n" +
                "\tSTART timestamp NOT NULL,\n" +
                "\tDESCRIPTION varchar (255),\n" +
                "PRIMARY KEY (id) )";
        System.out.println(sql);
        dbAccess.update(conn, sql);
    }

    public void connect() throws Exception {
        conn = DriverManager.getConnection(dbConnString);
    }

    public void close() throws Exception {
        conn.close();
        try {
            conn = DriverManager.getConnection(dbConnString + ";shutdown=true");
        } catch (SQLException ex) {
            System.out.println("DerbyDAOImpl.close: " + ex.getMessage());
        }
    }

    @Override
    public ExCard upsert(ExCard exCard) {
        try {
        if (exCard.getId()!=null) {
            return update(exCard);
        } else {
            return insert(exCard);
        }
        } catch (SQLException ex) {
            throw new RuntimeException("Upsert Error: " + ex.getMessage());
        }
    }

    @Override
    public boolean delete(ExCard card) {
        try {
            dbAccess.update(conn,
                    "DELETE FROM ExCard WHERE id=?",
                    card.getId()
            );
        } catch (SQLException ex) {
            throw new RuntimeException("Delete Error: " + ex.getMessage());
        }
        return true;
    }

    @Override
    public List<ExCard> findAll() {
        return Collections.emptyList();
    }

    @Override
    public ExCard findById(Long id) {
        try {
            return (ExCard) dbAccess.query(conn,
                    "SELECT * FROM ExCard WHERE id=?",
                    new BeanHandler(ExCard.class),
                    id.toString()
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //region Private

    private ExCard insert(ExCard card) throws SQLException {
        long id = dbAccess.insert(conn,
                "INSERT INTO ExCard ( name, state, start, desc) VALUES (?,?,?,?)",
                new ScalarHandler<BigDecimal>(),
                card.getName(), card.getState().name(), card.getStart().toString(), card.getDesc()
        ).longValue();
        return this.findById(card.getId());
    }

    private ExCard update(ExCard card) throws SQLException {
        dbAccess.update(conn,
                "UPDATE ExCard SET name=?, state=?, start=?, desc=? WHERE id=?",
                card.getName(), card.getState(), card.getStart(), card.getDesc(), card.getId()
        );
        return this.findById(card.getId());
    }

    //endregion
}
