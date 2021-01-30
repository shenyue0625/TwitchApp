package com.laioffer.jupiter.servlet;

import com.laioffer.jupiter.external.TwitchClient;
import com.laioffer.jupiter.external.TwitchException;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import com.laioffer.jupiter.entity.Game;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "GameServlet", urlPatterns = {"/game"})
public class GameServlet extends HttpServlet {
    //protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    /*    //前端发来的json/stream(101010110)变成string， 然后convert to hashmap type 数据在互联网传输过程中被convert
        //这里必须对传进来的request进行 IOUtils.toString 是因为我们无法确认传进来的request是不是json格式
        //这里的request.getReader()是指request message body吗？
        JSONObject jsonRequest = new JSONObject(IOUtils.toString(request.getReader()));
        String name = jsonRequest.getString("name");
        String developer = jsonRequest.getString("developer");
        String releaseTime = jsonRequest.getString("release_time");
        String website = jsonRequest.getString("website");
        float price = jsonRequest.getFloat("price");

        System.out.println("Name is: " + name);
        System.out.println("Developer is: " + developer);
        System.out.println("Release time is: " + releaseTime);
        System.out.println("Website is: " + website);
        System.out.println("Price is: " + price);

        response.setContentType("application/json");
        JSONObject jsonResponse = new JSONObject();
        jsonResponse.put("status", "ok");
        response.getWriter().print(jsonResponse);
     */
    //}

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
/*        response.setContentType("application/json");
        ObjectMapper mapper = new ObjectMapper();//类似一个改变格式用的转换器？
        Game game = new Game("World of Warcraft", "Blizzard Entertainment", "Feb 11, 2005", "https://www.worldofwarcraft.com", 49.99);
        //把game object变成json string
        response.getWriter().print(mapper.writeValueAsString(game));
*/
        //check if game_name exist and save it as variable
        String gameName = request.getParameter("game_name");
        TwitchClient client = new TwitchClient();

        response.setContentType("application/json;charset=UTF-8");
        try {
            if (gameName != null) {
                response.getWriter().print(new ObjectMapper().writeValueAsString(client.searchGame(gameName)));
            } else {
                response.getWriter().print(new ObjectMapper().writeValueAsString(client.topGames(0)));
            }
        } catch (TwitchException e) {
            throw new ServletException(e);
        }

    }
}
