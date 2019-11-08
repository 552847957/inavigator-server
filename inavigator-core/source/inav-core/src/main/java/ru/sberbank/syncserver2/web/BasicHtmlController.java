package ru.sberbank.syncserver2.web;

import org.springframework.web.servlet.mvc.SimpleFormController;

import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: Admin
 * Date: 27.02.2014
 * Time: 3:03:36
 * To change this template use File | Settings | File Templates.
 */
public class BasicHtmlController {
    public static void showResponse(HttpServletResponse response, String body){
        response.setContentType("text/html");
        PrintWriter out = null;
        try {
            out = response.getWriter();
            if(out!=null){
                out.print("<html><body>"+body+"</body></html>");
            }
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } finally {
            if(out!=null){
                out.flush();
                out.close();
            }
        }
    }

    public static void showResponseInTable(HttpServletResponse response, String body){
        body = "<table border=\"1\">"+body+"</table>";
        showResponse(response,body);
    }

}
