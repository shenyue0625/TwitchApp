package com.laioffer.jupiter.servlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet(name = "LogoutServlet", urlPatterns = {"/logout"})
public class LogoutServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //log out的时候，session id在server端会销毁，但是在用户端的cookie里也许不会销毁，所以cookie里可能存了很多没用了session id
        //里面的参数false：如果session如果存在就返回，不存在就不要创建新的
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();//后端销毁session
        }
    }

}
