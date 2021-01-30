package com.laioffer.jupiter.db;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class MySQLTableCreator {
   /* DROP TABLE IF EXISTS table_name;
    CREATE TABLE table_name (
            column1 datatype,
            column2 datatype,
            column3 datatype,
  ....
    );
    INSERT INTO table_name (column1, column2, column3, ...) VALUES (value1, value2, value3, ...);
*/
    // Run this as a Java application to reset the database.
    public static void main(String[] args) {
        try {

            // Step 1 Connect to MySQL.
            System.out.println("Connecting to " + MySQLDBUtil.getMySQLAddress());
                //new 一个driver，用于后面建立连接。我们的library里可能有瑕疵，有些corner case没考虑到，这句话防止报错？
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
                //建立连接
            Connection conn = DriverManager.getConnection(MySQLDBUtil.getMySQLAddress());

            if (conn == null) {
                return;
            }

            // Step 2 Drop tables in case they exist.
            // 通过建立的连接statement来执行sql语句
            Statement statement = conn.createStatement();
            String sql = "DROP TABLE IF EXISTS favorite_records";//删除的顺序先删有foreign key的
            statement.executeUpdate(sql);//执行sql语句。executeUpdate()是一个写操作。executeQuery()是读操作 通常需要存储下来

            sql = "DROP TABLE IF EXISTS items";
            statement.executeUpdate(sql);

            sql = "DROP TABLE IF EXISTS users";
            statement.executeUpdate(sql);

            // Step 3 Create new tables.创建的顺序先创建primary key的
            sql = "CREATE TABLE items ("
                    + "id VARCHAR(255) NOT NULL,"//VARCHAR(255)代表string
                    + "title VARCHAR(255),"
                    + "url VARCHAR(255),"
                    + "thumbnail_url VARCHAR(255),"
                    + "broadcaster_name VARCHAR(255),"
                    + "game_id VARCHAR(255),"
                    + "type VARCHAR(255) NOT NULL,"
                    + "PRIMARY KEY (id)"
                    + ")";
            statement.executeUpdate(sql);

            sql = "CREATE TABLE users ("
                    + "id VARCHAR(255) NOT NULL,"
                    + "password VARCHAR(255) NOT NULL,"
                    + "first_name VARCHAR(255),"
                    + "last_name VARCHAR(255),"
                    + "PRIMARY KEY (id)"
                    + ")";
            statement.executeUpdate(sql);

            sql = "CREATE TABLE favorite_records ("
                    + "user_id VARCHAR(255) NOT NULL,"
                    + "item_id VARCHAR(255) NOT NULL,"
                    + "last_favor_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                    + "PRIMARY KEY (user_id, item_id),"
                    + "FOREIGN KEY (user_id) REFERENCES users(id),"
                    + "FOREIGN KEY (item_id) REFERENCES items(id)"
                    + ")";
            statement.executeUpdate(sql);

            // Step 4: insert fake user 1111/3229c1097c00d497a0fd282d586be050.
            sql = "INSERT INTO users VALUES('1111', '3229c1097c00d497a0fd282d586be050', 'John', 'Smith')";
            statement.executeUpdate(sql);

            conn.close();
            System.out.println("Import done successfully");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


