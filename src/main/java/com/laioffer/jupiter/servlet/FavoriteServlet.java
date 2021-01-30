package com.laioffer.jupiter.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.laioffer.jupiter.db.MySQLConnection;
import com.laioffer.jupiter.db.MySQLException;
import com.laioffer.jupiter.entity.FavoriteRequestBody;
import com.laioffer.jupiter.entity.Item;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet(name = "FavoriteServlet", urlPatterns = {"/favorite", "/history"})
public class FavoriteServlet extends HttpServlet {

    //doPost() method will invoke "setFavoriteItem()" in MySQLConnection
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
       HttpSession session = request.getSession(false);
       if (session == null) {//这个用户没登录
               response.setStatus(HttpServletResponse.SC_FORBIDDEN);//返回403 error
               return;
           }
       //此处就不用用户输入的user id了，只需要从我们登录的session里查看user id
        String userId = (String) session.getAttribute("user_id");//user id这个attribute是我们loginservlet里面设置的

        /*ObjectMapper mapper = new ObjectMapper();
        //mapper帮助convert json->java obj。读取前端的request body，存成FavoriteRequestBody类型
        FavoriteRequestBody body = mapper.readValue(request.getReader(), FavoriteRequestBody.class);*/
        FavoriteRequestBody body = ServletUtil.readRequestBody(FavoriteRequestBody.class, request);
        if (body == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        MySQLConnection connection = null;
        try {
            connection = new MySQLConnection();
            connection.setFavoriteItem(userId, body.getFavoriteItem());
        } catch (MySQLException e) {
            throw new ServletException(e);
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }

    //doDelete() method will invoke "unsetFavoritedItem()" in MySQLConnection
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
       HttpSession session = request.getSession(false);
       if (session == null) {
               response.setStatus(HttpServletResponse.SC_FORBIDDEN);
               return;
           }
        String userId = (String) session.getAttribute("user_id");

        //ServletUtil里的readRequestBody进行了request进来的数据转换类型
        FavoriteRequestBody body = ServletUtil.readRequestBody(FavoriteRequestBody.class, request);
        if (body == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        MySQLConnection connection = null;
        try {
            connection = new MySQLConnection();
            connection.unsetFavoriteItem(userId, body.getFavoriteItem().getId());
        } catch (MySQLException e) {
            throw new ServletException(e);
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }

    //doGet() method will invoke "getFavoriteItems()" in MySQLConnection
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
       HttpSession session = request.getSession(false);
       if (session == null) {
               response.setStatus(HttpServletResponse.SC_FORBIDDEN);
               return;
           }
        String userId = (String) session.getAttribute("user_id");

        Map<String, List<Item>> itemMap;
        MySQLConnection connection = null;
        try {
            connection = new MySQLConnection();
            itemMap = connection.getFavoriteItems(userId);
            /*response.setContentType("application/json;charset=UTF-8");
            response.getWriter().print(new ObjectMapper().writeValueAsString(itemMap));*/
            ServletUtil.writeItemMap(response, itemMap);//调用的servletUtil里的function给前端返回
        } catch (MySQLException e) {
            throw new ServletException(e);
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }
}
