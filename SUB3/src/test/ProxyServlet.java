package test;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Enumeration;

public class ProxyServlet extends HttpServlet {

    private String targetPath = "";

    public void setTargetPath(String targetPath) {
        this.targetPath = targetPath;
    }

    @Override
    public void init() throws ServletException {
        // 서블릿 초기화 시 필요한 작업이 있으면 여기서 처리합니다.
        // 이 메소드는 서블릿이 최초로 실행될 때 한 번만 실행됩니다.
        // 서블릿이 실행되는 동안 필요한 작업이 없으면 이 메소드는 비워둡니다.
        System.out.println("ProxyServlet init");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        proxyRequest(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        proxyRequest(req, resp);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        proxyRequest(req, resp);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        proxyRequest(req, resp);
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 요청된 HTTP Method, Path, Query를 그대로 전달하기 위해 service() 메소드를 오버라이드합니다.
        // super.service()를 호출하지 않고 proxyRequest() 메소드만 호출합니다.
        proxyRequest(req, resp);
    }

    private void proxyRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String requestMethod = req.getMethod();
        String targetUrl = targetPath + req.getRequestURI();

        URL url = new URL(targetUrl);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod(requestMethod);

        // 원본 요청 헤더를 프록시 요청에 복사
        /*req.getHeaderNames().asIterator().forEachRemaining(headerName -> {
            String headerValue = req.getHeader(headerName);
            con.setRequestProperty(headerName, headerValue);
        });*/
        Enumeration<String> headerNames = req.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = req.getHeader(headerName);
            con.setRequestProperty(headerName, headerValue);
        }

        // 원본 요청 바디를 프록시 요청에 복사
        copyRequestBody(req, con);

        int responseCode = con.getResponseCode();

        // 프록시 응답 헤더를 원본 응답에 복사
        for (String headerName : con.getHeaderFields().keySet()) {
            if (headerName != null) {
                String headerValue = con.getHeaderField(headerName);
                resp.setHeader(headerName, headerValue);
            }
        }

        resp.setStatus(responseCode);

        // 프록시 응답 바디를 원본 응답에 복사
        copyResponseBody(con, resp);

        con.disconnect();
    }

    private void copyRequestBody(HttpServletRequest req, HttpURLConnection con) throws IOException {
        if (req.getContentLength() > 0) {
            con.setDoOutput(true);
            try (OutputStream outputStream = con.getOutputStream()) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = req.getInputStream().read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
        }
    }

    private void copyResponseBody(HttpURLConnection con, HttpServletResponse resp) throws IOException {
        try (InputStream inputStream = con.getInputStream();
             OutputStream outputStream = resp.getOutputStream()) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
    }

}
