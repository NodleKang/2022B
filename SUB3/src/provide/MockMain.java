package provide;

import com.google.gson.Gson;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import provide.gson.Configuration;
import provide.gson.InputQueueData;
import provide.gson.OutputQueueData;

public class MockMain {
    private static final int QUEUE_CAPACITY = 100;

    private static final int MAX_INPUT_QUEUE_COUNT = 20;

    private static final String CONTROLLER_URI = "http://127.0.0.1:8080/queueInfo";

    private String configurationString;

    private Configuration configuration;

    private boolean scenarioStarted;

    private List<BlockingQueue<String>> inputQueues = new ArrayList<>();

    private BlockingQueue<String> outputQueue = new ArrayBlockingQueue<>(100);

    public static void main(String[] args) throws Exception {
        (new MockMain()).start();
    }

    public void start() throws Exception {
        loadConfiguration();
        createInputQueue();
        startInputQueueServer();
        startServer(this.configuration.getOutputQueueURI(), (Servlet)new OutputQueueServlet());
        startServer("http://127.0.0.1:8080/queueInfo", (Servlet)new ControllerServlet());
        System.out.println();
        System.out.println("[Controller] ��û ����� (Controller ��û/������ ����Ǹ� ä�� �ó������� ���� Input Queue �Է��� ���۵�)");
        (new Thread(() -> {
            try {
                Thread.sleep(10000L);
            } catch (InterruptedException interruptedException) {}
            if (!this.scenarioStarted)
                finalFail("[����]\n�� Controller�� ���� ���� ���α׷�(SP_TEST)�� ��û ����(�ִ� 10��)");
        })).start();
    }

    private void startServer(String urlString, Servlet servlet) throws Exception {
        URL url = new URL(urlString);
        Server server = new Server();
        ServerConnector http = new ServerConnector(server);
        http.setHost(url.getHost());
        http.setPort(url.getPort());
        server.addConnector((Connector)http);
        ServletHandler servletHandler = new ServletHandler();
        servletHandler.addServletWithMapping(new ServletHolder(servlet), url.getPath());
        server.setHandler((Handler)servletHandler);
        server.start();
    }

    private void createInputQueue() {
        for (int i = 0; i < 20; i++)
            this.inputQueues.add(new ArrayBlockingQueue<>(100));
    }

    private void startInputQueueServer() throws Exception {
        for (int i = 0; i < this.configuration.getInputQueueCount(); i++) {
            String inputQueueURI = this.configuration.getInputQueueURIs().get(i);
            InputQueueServlet inputQueueServlet = new InputQueueServlet(i, inputQueueURI);
            startServer(inputQueueURI, (Servlet)inputQueueServlet);
        }
    }

    private void loadConfiguration() throws Exception {
        this.configurationString = new String(Files.readAllBytes(Paths.get("../mock/sub3/CONFIGURATION.JSON", new String[0])));
        this.configuration = (Configuration)(new Gson()).fromJson(this.configurationString, Configuration.class);
    }

    protected class ControllerServlet extends HttpServlet {
        protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
            res.setStatus(200);
            res.getWriter().write(MockMain.this.configurationString);
            System.out.println("[Controller] HTTP ����");
                    System.out.println(MockMain.this.configurationString);
            synchronized (MockMain.this) {
                if (!MockMain.this.scenarioStarted) {
                    MockMain.this.scenarioStarted = true;
                    (new Thread(() -> MockMain.this.startScenario())).start();
                }
            }
        }
    }

    protected class InputQueueServlet extends HttpServlet {
        private int queueNo;

        private String inputQueueURI;

        public InputQueueServlet(int queueNo, String inputQueueURI) {
            this.queueNo = queueNo;
            this.inputQueueURI = inputQueueURI;
        }

        protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
            try {
                String value = ((BlockingQueue<String>)MockMain.this.inputQueues.get(this.queueNo)).take();
                System.out.println(String.format("[Input Queue(%d)] HTTP ���� ('%s')", new Object[] { Integer.valueOf(this.queueNo), value }));
                res.setStatus(200);
                res.getWriter().write(value);
            } catch (Exception exception) {}
        }
    }

    protected class OutputQueueServlet extends HttpServlet {
        protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
            String body = req.getReader().lines().collect(Collectors.joining());
            System.out.println(String.format("[Output Queue] HTTP ��û ('%s')", new Object[] { body }));
            MockMain.this.outputQueue.add(body);
            res.setStatus(200);
        }
    }

    private void finalFail(String message) {
        System.out.println();
        System.out.println(message);
        System.exit(0);
    }

    private void finalSuccess(String message) {
        System.out.println();
        System.out.println(message);
    }

    private void startScenario() {
        try {
            Exception exception2, exception1 = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doScenario(String line) throws Exception {
        String[] dataSplit;
        long timestamp;
        int queueNo;
        String value;
        int i;
        String outputQueueString;
        List<String> outputQueueList;
        String temp;
        if (line.trim().length() == 0)
            return;
        String[] lineSplit = line.split("#");
        String command = lineSplit[0];
        String data = (lineSplit.length > 1) ? lineSplit[1] : "";
        String str1;
        switch ((str1 = command).hashCode()) {
            case -1952183039:
                if (!str1.equals("OUTPUT"))
                    break;
                for (i = 0; i < this.inputQueues.size(); i++) {
                    if (((BlockingQueue)this.inputQueues.get(i)).size() != 0) {
                        String inputQueueString = ((BlockingQueue<String>)this.inputQueues.get(i)).poll();
                        finalFail(String.format("[����]\n�� Input Queue(%d)�� �����Ͱ� �ԷµǾ����� �������α׷�(SP_TEST)�� ��û���� ����\n>Input Queue ������: %s", new Object[] { Integer.valueOf(i), inputQueueString }));
                    }
                }
                if ("NO_OUTPUT".equals(data)) {
                    if (this.outputQueue.size() != 0) {
                        String str = this.outputQueue.poll();
                        finalFail(String.format("[����]\n��Output Queue�� �׽�Ʈ �ó������� ���ǵ��� ���� �����Ͱ� ��û��\n����: ������ ����>\n>������û: %s", new Object[] { str }));
                    }
                    break;
                }
                switch (this.outputQueue.size()) {
                    case 0:
                        finalFail(String.format("[����]\n�� Output Queue�� �׽�Ʈ �ó������� ���ǵ� �����Ͱ� ��û���� ����\n>����: %s\n>������û: ��û ����", new Object[] { data }));
                        break;
                    case 1:
                        outputQueueString = this.outputQueue.poll();
                        try {
                            OutputQueueData scenarioData = (OutputQueueData)(new Gson()).fromJson(data, OutputQueueData.class);
                            OutputQueueData outputQueueData = (OutputQueueData)(new Gson()).fromJson(outputQueueString, OutputQueueData.class);
                            if (!scenarioData.getResult().equals(outputQueueData.getResult()))
                                finalFail(String.format("[����]\n�� Output Queue�� ��û�� �����Ͱ� �׽�Ʈ �ó������� ���ǵ� �����Ϳ� ����ġ\n>����: %s\n>������û: %s", new Object[] { data, outputQueueString }));
                        } catch (Exception e) {
                            finalFail(String.format("[����]\n�� Output Queue�� ��û�� �����Ͱ� �׽�Ʈ �ó������� ���ǵ� �����Ϳ� ����ġ\n>����: %s\n>������û: %s", new Object[] { data, outputQueueString }));
                        }
                        break;
                }
                outputQueueList = new ArrayList<>();
                while ((temp = this.outputQueue.poll()) != null)
                    outputQueueList.add(temp);
                finalFail(String.format("[����]\n�� Output Queue�� 1���� �ʰ��ϴ� �����Ͱ� ��û��\n>����: %s\n>������û: %s", new Object[] { data, String.join(",", (Iterable)outputQueueList) }));
                break;
            case 68795:
                if (!str1.equals("END"))
                    break;
                finalSuccess("[����]\n" + data);
                break;
            case 69820330:
                if (!str1.equals("INPUT"))
                    break;
                dataSplit = data.split(" ");
                timestamp = Long.valueOf(dataSplit[0]).longValue();
                queueNo = Integer.valueOf(dataSplit[1]).intValue();
                value = dataSplit[2];
                sendInputQueueData(queueNo, timestamp, value);
                break;
            case 76397197:
                if (!str1.equals("PRINT"))
                    break;
                System.out.println(data);
                break;
            case 78984887:
                if (!str1.equals("SLEEP"))
                    break;
                Thread.sleep(Long.parseLong(data));
                break;
        }
    }

    private void sendInputQueueData(int queueNo, long timestamp, String value) {
        String json = (new Gson()).toJson(new InputQueueData(timestamp, value));
        System.out.println(String.format("\n[Input Queue(%d)] �Է� ('%s')", new Object[] { Integer.valueOf(queueNo), json }));
        ((BlockingQueue<String>)this.inputQueues.get(queueNo)).add(json);
    }
}
