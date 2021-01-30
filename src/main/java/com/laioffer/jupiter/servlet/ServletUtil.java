package com.laioffer.jupiter.servlet;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.laioffer.jupiter.entity.Item;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.apache.commons.codec.digest.DigestUtils;

public class ServletUtil {
    public static void writeItemMap(HttpServletResponse response, Map<String, List<Item>> itemMap) throws IOException {
        //告诉前端browser我的body返回形式。charset=UTF-8"表示更全面的ascii，甚至包含韩文，中文等等。写上以后，确保从后端读取到的data可以显示各类特殊字符和语言
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().print(new ObjectMapper().writeValueAsString(itemMap));
    }

    public static String encryptPassword(String userId, String password) throws IOException {
        //DigestUtils.md5Hex： 是DigestUtils库里提供的一个function，md5Hex是用来给密码单向加密的
        //hs256/hs512是常见的方法，用来提供decryption解密 & encryption加密的
        //toLowerCase()在这里只是为了看起来舒服一点，有没有问题不大
        return DigestUtils.md5Hex(userId + DigestUtils.md5Hex(password)).toLowerCase();
    }

    //<T> T是generics表达法，这里可以返回LoginRequestBody，RegisterRequestBody，FavoriteRequestBody。。
    public static <T> T readRequestBody(Class<T> cl, HttpServletRequest request) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        try {
            //request.getReader()返回request body，这里的data type是个stream
            return mapper.readValue(request.getReader(), cl);
        } catch (JsonParseException | JsonMappingException e) {
            return null;
        }
    }
}
