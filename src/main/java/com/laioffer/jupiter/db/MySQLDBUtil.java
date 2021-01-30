package com.laioffer.jupiter.db;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

// 帮我们生成一个连接到 mysql instance 的地址
public class MySQLDBUtil {
    private static final String INSTANCE = "laiproject-instance.c1hux7gcp8pj.us-east-1.rds.amazonaws.com";
    private static final String PORT_NUM = "3306";
    private static final String DB_NAME = "jupiter";

    public static String getMySQLAddress() throws IOException {
        //Properties内部实现就是hash table
        Properties prop = new Properties();
        String propFileName = "config.properties";

        //为什么读取这个prop file是存成stream？因为这个文件也是有大小的，一口气读不完的话，一段一段处理
        InputStream inputStream = MySQLDBUtil.class.getClassLoader().getResourceAsStream(propFileName);
        prop.load(inputStream);

        String username = prop.getProperty("user");
        String password = prop.getProperty("password");
        //createDatabaseIfNotExist=true 是说在aws上如果db name写错了或者忘了写的话就创建一个。如果写了，这句话就起不到作用
        //jdbc 是java database connector
        return String.format("jdbc:mysql://%s:%s/%s?user=%s&password=%s&autoReconnect=true&serverTimezone=UTC&createDatabaseIfNotExist=true",
                INSTANCE, PORT_NUM, DB_NAME, username, password);
    }

}
