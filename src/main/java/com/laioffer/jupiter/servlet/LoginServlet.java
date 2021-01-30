package com.laioffer.jupiter.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.laioffer.jupiter.db.MySQLConnection;
import com.laioffer.jupiter.db.MySQLException;
import com.laioffer.jupiter.entity.LoginRequestBody;
import com.laioffer.jupiter.entity.LoginResponseBody;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet(name = "LoginServlet", urlPatterns = {"/login"})
public class LoginServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //ServletUtil里的readRequestBody进行了request进来的数据转换类型
        LoginRequestBody body = ServletUtil.readRequestBody(LoginRequestBody.class, request);
        if (body == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

//第1步： verify login
        String username;
        MySQLConnection connection = null;
        try {
            connection = new MySQLConnection();
            String userId = body.getUserId();
            //verify login的时候还是要用加密后的密码来比对
            String password = ServletUtil.encryptPassword(body.getUserId(), body.getPassword());
            username = connection.verifyLogin(userId, password);//login成功，返回用户名
        } catch (MySQLException e) {
            throw new ServletException(e);
        } finally {
            connection.close();
        }
//第2步：生成一个session，返回给browser
        if (!username.isEmpty()) {
            HttpSession session = request.getSession();//request.getSession()：对于这个request来说的session是什么，如果有则返回这个session，如果没有则创建一个
            //session id 在这里不需要我们创建，因为request.getSession()已经帮我们自动生成了
            session.setAttribute("user_id", body.getUserId());
            session.setMaxInactiveInterval(600);//给这个session设置一个过期时间，时间单位是秒

            //tomcat帮我们实现了session id传输给前端browser（session id 以header的形式返回给前端的）
            LoginResponseBody loginResponseBody = new LoginResponseBody(body.getUserId(), username);

            response.setContentType("application/json;charset=UTF-8");
            ObjectMapper mapper = new ObjectMapper();
            //为了match课件删掉这句：response.getWriter().print(new ObjectMapper().writeValueAsString(loginResponseBody));
            mapper.writeValue(response.getWriter(), loginResponseBody);
        } else {//如果在第1步，verify login的时候失败了，返回401 error
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }


}
