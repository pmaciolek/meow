package co.llective;

import co.llective.mq.MeowMQ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class MessageServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    final Logger logger = LoggerFactory.getLogger(HelloWorldServlet.class);
    private static final MeowMQ meowMQ = new MeowMQ();

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        logger.info("GET on URI: "+request.getRequestURI());

        meowMQ.produce(request.getParameter("msg"));

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().println("{ \"result\": \"OK\"}");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String message = request.getParameter("msg");
        meowMQ.produce(message);
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().println("{ \"result\": \"OK\"}");
    }
}
