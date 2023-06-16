package provide;

import com.google.gson.Gson;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import provide.gson.Configuration;
import provide.gson.InputQueueData;
import provide.gson.WorkerCall;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;

public class MockMain {
    private static final int QUEUE_CAPACITY = 100;

    private static final int MAX_INPUT_QUEUE_COUNT = 20;

    private static final String CONTROLLER_URI = "http://127.0.0.1:8080/queueInfo";

    private static final String WORKER_CALL_URI = "http://127.0.0.1:9999/workerCall";

    private Gson gson = new Gson();

    private int scenarioNo;

    private String configurationString;

    private Configuration configuration;

    private boolean scenarioStarted;

    private List<BlockingQueue<String>> inputQueues = new ArrayList<>();

    private BlockingQueue<String> outputQueue = new ArrayBlockingQueue<>(100);

    private BlockingQueue<WorkerCall> workerCallQueue = new ArrayBlockingQueue<>(100);

    private Map<Integer, Long> processIds = new HashMap<>();

    private Map<Integer, Map<Integer, Long>> processThreadIds = new HashMap<>();

    public static void main(String[] args) throws Exception {
        int scenarioNo = (args.length == 1) ? Integer.parseInt(args[0]) : 1;
        (new MockMain()).start(scenarioNo);
    }

    public void start(int scenarioNo) throws Exception {
        this.scenarioNo = scenarioNo;
        loadConfiguration();
        startServer("http://127.0.0.1:9999/workerCall", (Servlet)new WorkerCallServlet());
        createInputQueue();
        startInputQueueServer();
        startServer(this.configuration.getOutputQueueURI(), (Servlet)new OutputQueueServlet());
        startServer("http://127.0.0.1:8080/queueInfo", (Servlet)new ControllerServlet());
        System.out.println();
        System.out.println(String.format("[시나리오] %d번 시나리오", new Object[] { Integer.valueOf(this.scenarioNo) }));
                System.out.println();
        System.out.println("[Controller] 요청 대기중 (Controller 요청/응답이 수해오디면 채점 시나리오에 따른 Input Queue 입력이 시작됨)");
        (new Thread(() -> {
            try {
                Thread.sleep(10000L);
            } catch (InterruptedException interruptedException) {}
            if (!this.scenarioStarted)
                finalFail("[오답]\n> Controller에 대한 구현 프로그램(SP_TEST)의 요청 없음 (최대 10초)");
        })).start();
    }

    private void printAssginInfo() {
        int processCount = this.configuration.getProcessCount();
        int threadCount = this.configuration.getThreadCount();
        int inputQueueCount = this.configuration.getInputQueueCount();
        for (int m = 0; m < processCount * threadCount; ) {
            System.out.print("-----------");
            m++;
        }
        System.out.println();
        for (int k = 0; k < processCount; k++) {
            System.out.print(String.format("Process #%d ", new Object[] { Integer.valueOf(k) }));
            for (int t = 1; t < threadCount; t++) {
                System.out.print(String.format("           ", new Object[] { Integer.valueOf(k) }));
            }
        }
        System.out.println();
        for (int j = 0; j < processCount * threadCount; ) {
            System.out.print("-----------");
            j++;
        }
        System.out.println();
        for (int p = 0; p < processCount; p++) {
            for (int t = 0; t < threadCount; t++) {
                System.out.print(String.format("Thread #%d  ", new Object[] { Integer.valueOf(t) }));
            }
        }
        System.out.println();
        int i;
        for (i = 0; i < processCount * threadCount; ) {
            System.out.print("-----------");
            i++;
        }
        System.out.println();
        for (i = 0; i < inputQueueCount; i++) {
            if (i >= 10) {
                System.out.print(String.format("Worker(%d) ", new Object[] { Integer.valueOf(i) }));
            } else {
                System.out.print(String.format("Worker(%d)  ", new Object[] { Integer.valueOf(i) }));
            }
            if ((i + 1) % processCount * threadCount == 0)
                System.out.println();
        }
        System.out.println();
        for (i = 0; i < processCount * threadCount; ) {
            System.out.print("-----------");
            i++;
        }
        System.out.println();
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
        this.configurationString = new String(Files.readAllBytes(Paths.get(String.format("../mock/sub4/CONFIGURATION%d.JSON", new Object[] { Integer.valueOf(this.scenarioNo) }), new String[0])));
        this.configuration = (Configuration)(new Gson()).fromJson(this.configurationString, Configuration.class);
    }

    protected class ControllerServlet extends HttpServlet {
        protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
            res.setStatus(200);
            res.getWriter().write(MockMain.this.configurationString);
            System.out.println("[Controller] HTTP 응답");
                    System.out.println(MockMain.this.configurationString);
            MockMain.this.printAssginInfo();
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
                System.out.println(String.format("[Input Queue(%d)] HTTP 응답 ('%s')", new Object[] { Integer.valueOf(this.queueNo), value }));
                res.setStatus(200);
                res.getWriter().write(value);
            } catch (Exception exception) {}
        }
    }

    protected class OutputQueueServlet extends HttpServlet {
        protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
            String body = req.getReader().lines().collect(Collectors.joining());
            System.out.println(String.format("[Output Queue] HTTP 요청 ('%s')", new Object[] { body }));
            MockMain.this.outputQueue.add(body);
            res.setStatus(200);
        }
    }

    protected class WorkerCallServlet extends HttpServlet {
        protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
            String body = req.getReader().lines().collect(Collectors.joining());
            WorkerCall workerCall = (WorkerCall)MockMain.this.gson.fromJson(body, WorkerCall.class);
            System.out.println(String.format("[SP_TEST의 Worker(%d) 실행] Process Id(%d), Thread Id(%d)", new Object[] { Integer.valueOf(workerCall.getQueueNo()), Long.valueOf(workerCall.getProcessId()), Long.valueOf(workerCall.getThreadId()) }));
            MockMain.this.workerCallQueue.add(workerCall);
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
        // Byte code:
        //   0: aload_1
        //   1: invokevirtual trim : ()Ljava/lang/String;
        //   4: invokevirtual length : ()I
        //   7: ifne -> 11
        //   10: return
        //   11: aload_1
        //   12: ldc_w '#'
        //   15: invokevirtual split : (Ljava/lang/String;)[Ljava/lang/String;
        //   18: astore_2
        //   19: aload_2
        //   20: iconst_0
        //   21: aaload
        //   22: astore_3
        //   23: aload_2
        //   24: arraylength
        //   25: iconst_1
        //   26: if_icmple -> 35
        //   29: aload_2
        //   30: iconst_1
        //   31: aaload
        //   32: goto -> 38
        //   35: ldc_w ''
        //   38: astore #4
        //   40: aload_3
        //   41: dup
        //   42: astore #5
        //   44: invokevirtual hashCode : ()I
        //   47: lookupswitch default -> 707, -1952183039 -> 104, -1881097187 -> 118, 68795 -> 132, 69820330 -> 146, 76397197 -> 160, 78984887 -> 174
        //   104: aload #5
        //   106: ldc_w 'OUTPUT'
        //   109: invokevirtual equals : (Ljava/lang/Object;)Z
        //   112: ifne -> 263
        //   115: goto -> 707
        //   118: aload #5
        //   120: ldc_w 'RESULT'
        //   123: invokevirtual equals : (Ljava/lang/Object;)Z
        //   126: ifne -> 263
        //   129: goto -> 707
        //   132: aload #5
        //   134: ldc_w 'END'
        //   137: invokevirtual equals : (Ljava/lang/Object;)Z
        //   140: ifne -> 685
        //   143: goto -> 707
        //   146: aload #5
        //   148: ldc_w 'INPUT'
        //   151: invokevirtual equals : (Ljava/lang/Object;)Z
        //   154: ifne -> 210
        //   157: goto -> 707
        //   160: aload #5
        //   162: ldc_w 'PRINT'
        //   165: invokevirtual equals : (Ljava/lang/Object;)Z
        //   168: ifne -> 188
        //   171: goto -> 707
        //   174: aload #5
        //   176: ldc_w 'SLEEP'
        //   179: invokevirtual equals : (Ljava/lang/Object;)Z
        //   182: ifne -> 199
        //   185: goto -> 707
        //   188: getstatic java/lang/System.out : Ljava/io/PrintStream;
        //   191: aload #4
        //   193: invokevirtual println : (Ljava/lang/String;)V
        //   196: goto -> 707
        //   199: aload #4
        //   201: invokestatic parseLong : (Ljava/lang/String;)J
        //   204: invokestatic sleep : (J)V
        //   207: goto -> 707
        //   210: aload #4
        //   212: ldc_w ' '
        //   215: invokevirtual split : (Ljava/lang/String;)[Ljava/lang/String;
        //   218: astore #6
        //   220: aload #6
        //   222: iconst_0
        //   223: aaload
        //   224: invokestatic valueOf : (Ljava/lang/String;)Ljava/lang/Long;
        //   227: invokevirtual longValue : ()J
        //   230: lstore #7
        //   232: aload #6
        //   234: iconst_1
        //   235: aaload
        //   236: invokestatic valueOf : (Ljava/lang/String;)Ljava/lang/Integer;
        //   239: invokevirtual intValue : ()I
        //   242: istore #9
        //   244: aload #6
        //   246: iconst_2
        //   247: aaload
        //   248: astore #10
        //   250: aload_0
        //   251: iload #9
        //   253: lload #7
        //   255: aload #10
        //   257: invokespecial sendInputQueueData : (IJLjava/lang/String;)V
        //   260: goto -> 707
        //   263: iconst_0
        //   264: istore #11
        //   266: goto -> 345
        //   269: aload_0
        //   270: getfield inputQueues : Ljava/util/List;
        //   273: iload #11
        //   275: invokeinterface get : (I)Ljava/lang/Object;
        //   280: checkcast java/util/concurrent/BlockingQueue
        //   283: invokeinterface size : ()I
        //   288: ifeq -> 342
        //   291: aload_0
        //   292: getfield inputQueues : Ljava/util/List;
        //   295: iload #11
        //   297: invokeinterface get : (I)Ljava/lang/Object;
        //   302: checkcast java/util/concurrent/BlockingQueue
        //   305: invokeinterface poll : ()Ljava/lang/Object;
        //   310: checkcast java/lang/String
        //   313: astore #12
        //   315: aload_0
        //   316: ldc_w '[오답]\\n> Input Queue(%d)에 데이터가 입력되었지만 구현프로그램(SP_TEST)이 요청하지 않음\\n>Input Queue 데이터: %s'
        //   319: iconst_2
        //   320: anewarray java/lang/Object
        //   323: dup
        //   324: iconst_0
        //   325: iload #11
        //   327: invokestatic valueOf : (I)Ljava/lang/Integer;
        //   330: aastore
        //   331: dup
        //   332: iconst_1
        //   333: aload #12
        //   335: aastore
        //   336: invokestatic format : (Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;
        //   339: invokespecial finalFail : (Ljava/lang/String;)V
        //   342: iinc #11, 1
        //   345: iload #11
        //   347: aload_0
        //   348: getfield inputQueues : Ljava/util/List;
        //   351: invokeinterface size : ()I
        //   356: if_icmplt -> 269
        //   359: ldc_w 'NO_OUTPUT'
        //   362: aload #4
        //   364: invokevirtual equals : (Ljava/lang/Object;)Z
        //   367: ifeq -> 418
        //   370: aload_0
        //   371: getfield outputQueue : Ljava/util/concurrent/BlockingQueue;
        //   374: invokeinterface size : ()I
        //   379: ifeq -> 668
        //   382: aload_0
        //   383: getfield outputQueue : Ljava/util/concurrent/BlockingQueue;
        //   386: invokeinterface poll : ()Ljava/lang/Object;
        //   391: checkcast java/lang/String
        //   394: astore #11
        //   396: aload_0
        //   397: ldc_w '[오답]\\n> Output Queue에 테스트 시나리오에 정의되지 않은 데이터가 요청됨\\n>정답: 데이터 없음\\n>실제요청: %s'
        //   400: iconst_1
        //   401: anewarray java/lang/Object
        //   404: dup
        //   405: iconst_0
        //   406: aload #11
        //   408: aastore
        //   409: invokestatic format : (Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;
        //   412: invokespecial finalFail : (Ljava/lang/String;)V
        //   415: goto -> 668
        //   418: aload_0
        //   419: getfield outputQueue : Ljava/util/concurrent/BlockingQueue;
        //   422: invokeinterface size : ()I
        //   427: tableswitch default -> 598, 0 -> 448, 1 -> 470
        //   448: aload_0
        //   449: ldc_w '[오답]\\n> Output Queue에 테스트 시나리오에 정의된 데이터가 요청되지 않음\\n>정답: %s\\n>실제요청: 요청 없음'
        //   452: iconst_1
        //   453: anewarray java/lang/Object
        //   456: dup
        //   457: iconst_0
        //   458: aload #4
        //   460: aastore
        //   461: invokestatic format : (Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;
        //   464: invokespecial finalFail : (Ljava/lang/String;)V
        //   467: goto -> 668
        //   470: aload_0
        //   471: getfield outputQueue : Ljava/util/concurrent/BlockingQueue;
        //   474: invokeinterface poll : ()Ljava/lang/Object;
        //   479: checkcast java/lang/String
        //   482: astore #11
        //   484: new com/google/gson/Gson
        //   487: dup
        //   488: invokespecial <init> : ()V
        //   491: aload #4
        //   493: ldc_w provide/gson/OutputQueueData
        //   496: invokevirtual fromJson : (Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/Object;
        //   499: checkcast provide/gson/OutputQueueData
        //   502: astore #12
        //   504: new com/google/gson/Gson
        //   507: dup
        //   508: invokespecial <init> : ()V
        //   511: aload #11
        //   513: ldc_w provide/gson/OutputQueueData
        //   516: invokevirtual fromJson : (Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/Object;
        //   519: checkcast provide/gson/OutputQueueData
        //   522: astore #13
        //   524: aload #12
        //   526: invokevirtual getResult : ()Ljava/util/List;
        //   529: aload #13
        //   531: invokevirtual getResult : ()Ljava/util/List;
        //   534: invokeinterface equals : (Ljava/lang/Object;)Z
        //   539: ifne -> 668
        //   542: aload_0
        //   543: ldc_w '[오답]\\n> Output Queue에 요청된 데이터가 테스트 시나리오에 정의된 데이터와 불일치\\n>정답: %s\\n>실제요청: %s'
        //   546: iconst_2
        //   547: anewarray java/lang/Object
        //   550: dup
        //   551: iconst_0
        //   552: aload #4
        //   554: aastore
        //   555: dup
        //   556: iconst_1
        //   557: aload #11
        //   559: aastore
        //   560: invokestatic format : (Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;
        //   563: invokespecial finalFail : (Ljava/lang/String;)V
        //   566: goto -> 668
        //   569: astore #12
        //   571: aload_0
        //   572: ldc_w '[오답]\\nOutput Queue에 요청된 데이터가 테스트 시나리오에 정의된 데이터와 불일치\\n>정답: %s\\n>실제요청: %s'
        //   575: iconst_2
        //   576: anewarray java/lang/Object
        //   579: dup
        //   580: iconst_0
        //   581: aload #4
        //   583: aastore
        //   584: dup
        //   585: iconst_1
        //   586: aload #11
        //   588: aastore
        //   589: invokestatic format : (Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;
        //   592: invokespecial finalFail : (Ljava/lang/String;)V
        //   595: goto -> 668
        //   598: new java/util/ArrayList
        //   601: dup
        //   602: invokespecial <init> : ()V
        //   605: astore #12
        //   607: goto -> 620
        //   610: aload #12
        //   612: aload #13
        //   614: invokeinterface add : (Ljava/lang/Object;)Z
        //   619: pop
        //   620: aload_0
        //   621: getfield outputQueue : Ljava/util/concurrent/BlockingQueue;
        //   624: invokeinterface poll : ()Ljava/lang/Object;
        //   629: checkcast java/lang/String
        //   632: dup
        //   633: astore #13
        //   635: ifnonnull -> 610
        //   638: aload_0
        //   639: ldc_w '[오답]\\n> Output Queue1에 1건을 초과하는 데이터가 요청됨\\n>정답: %s\\n>실제요청: %s'
        //   642: iconst_2
        //   643: anewarray java/lang/Object
        //   646: dup
        //   647: iconst_0
        //   648: aload #4
        //   650: aastore
        //   651: dup
        //   652: iconst_1
        //   653: ldc_w ','
        //   656: aload #12
        //   658: invokestatic join : (Ljava/lang/CharSequence;Ljava/lang/Iterable;)Ljava/lang/String;
        //   661: aastore
        //   662: invokestatic format : (Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;
        //   665: invokespecial finalFail : (Ljava/lang/String;)V
        //   668: aload_3
        //   669: ldc_w 'OUTPUT'
        //   672: invokevirtual equals : (Ljava/lang/Object;)Z
        //   675: ifeq -> 707
        //   678: aload_0
        //   679: invokespecial checkProcessThread : ()V
        //   682: goto -> 707
        //   685: aload_0
        //   686: new java/lang/StringBuilder
        //   689: dup
        //   690: ldc_w '[정답]\\n'
        //   693: invokespecial <init> : (Ljava/lang/String;)V
        //   696: aload #4
        //   698: invokevirtual append : (Ljava/lang/String;)Ljava/lang/StringBuilder;
        //   701: invokevirtual toString : ()Ljava/lang/String;
        //   704: invokespecial finalSuccess : (Ljava/lang/String;)V
        //   707: return
        // Line number table:
        //   Java source line number -> byte code offset
        //   #278	-> 0
        //   #279	-> 10
        //   #282	-> 11
        //   #283	-> 19
        //   #284	-> 23
        //   #286	-> 40
        //   #288	-> 188
        //   #289	-> 196
        //   #291	-> 199
        //   #292	-> 207
        //   #294	-> 210
        //   #295	-> 220
        //   #296	-> 232
        //   #297	-> 244
        //   #299	-> 250
        //   #301	-> 260
        //   #305	-> 263
        //   #306	-> 269
        //   #307	-> 291
        //   #308	-> 315
        //   #305	-> 342
        //   #312	-> 359
        //   #313	-> 370
        //   #314	-> 382
        //   #315	-> 396
        //   #317	-> 415
        //   #318	-> 418
        //   #320	-> 448
        //   #321	-> 467
        //   #323	-> 470
        //   #327	-> 484
        //   #328	-> 504
        //   #329	-> 524
        //   #332	-> 542
        //   #334	-> 566
        //   #336	-> 571
        //   #339	-> 595
        //   #341	-> 598
        //   #343	-> 607
        //   #344	-> 610
        //   #343	-> 620
        //   #346	-> 638
        //   #351	-> 668
        //   #352	-> 678
        //   #354	-> 682
        //   #356	-> 685
        //   #359	-> 707
        // Local variable table:
        //   start	length	slot	name	descriptor
        //   0	708	0	this	Lprovide/MockMain;
        //   0	708	1	line	Ljava/lang/String;
        //   19	689	2	lineSplit	[Ljava/lang/String;
        //   23	685	3	command	Ljava/lang/String;
        //   40	668	4	data	Ljava/lang/String;
        //   220	43	6	dataSplit	[Ljava/lang/String;
        //   232	31	7	timestamp	J
        //   244	19	9	queueNo	I
        //   250	13	10	value	Ljava/lang/String;
        //   266	93	11	i	I
        //   315	27	12	inputQueueString	Ljava/lang/String;
        //   396	19	11	outputQueueString	Ljava/lang/String;
        //   484	114	11	outputQueueString	Ljava/lang/String;
        //   504	62	12	scenarioData	Lprovide/gson/OutputQueueData;
        //   524	42	13	outputQueueData	Lprovide/gson/OutputQueueData;
        //   571	24	12	e	Ljava/lang/Exception;
        //   607	61	12	outputQueueList	Ljava/util/List;
        //   610	10	13	temp	Ljava/lang/String;
        //   635	33	13	temp	Ljava/lang/String;
        // Local variable type table:
        //   start	length	slot	name	signature
        //   607	61	12	outputQueueList	Ljava/util/List<Ljava/lang/String;>;
        // Exception table:
        //   from	to	target	type
        //   484	566	569	java/lang/Exception
    }

    private void sendInputQueueData(int queueNo, long timestamp, String value) {
        String json = (new Gson()).toJson(new InputQueueData(timestamp, value));
        System.out.println(String.format("\n[Input Queue(%d)] 입력 ('%s')", new Object[] { Integer.valueOf(queueNo), json }));
        ((BlockingQueue<String>)this.inputQueues.get(queueNo)).add(json);
    }

    private void checkProcessThread() throws Exception {
        int processCount, threadCount;
        WorkerCall workerCall;
        int processNo, threadNo;
        Map<Integer, Long> threadIds;
        switch (this.workerCallQueue.size()) {
            case 0:
                finalFail(String.format("[오답]\n> Input Queue 데이터가 입력되었는데 Worker가 실행되지 않음", new Object[0]));
                return;
            case 1:
                processCount = this.configuration.getProcessCount();
                threadCount = this.configuration.getThreadCount();
                workerCall = this.workerCallQueue.poll();
                processNo = workerCall.getQueueNo() / threadCount % processCount;
                threadNo = workerCall.getQueueNo() % threadCount;
                if (this.processIds.containsKey(Integer.valueOf(processNo))) {
                    for (Map.Entry<Integer, Long> entry : this.processIds.entrySet()) {
                        if (((Integer)entry.getKey()).intValue() == processNo && ((Long)entry.getValue()).longValue() != workerCall.getProcessId())
                            finalFail(String.format("[오답]\n> Process #%d에서 실행해야 하는 Worker가 이전에는 Process Id(%d)에서 실행되었는데, 이번에는 Process Id(%d)에서 실행됨(불일치)", new Object[] { Integer.valueOf(processNo), entry.getValue(), Long.valueOf(workerCall.getProcessId()) }));
                        if (((Integer)entry.getKey()).intValue() != processNo && ((Long)entry.getValue()).longValue() == workerCall.getProcessId())
                            finalFail(String.format("[오답]\n> Process #%d에서 실행해야 하는 Worker가 Process Id(%d)에서 실행되었는데, Process #%d에서 실행해야 하는 Worker도 Process Id(%d)에서 실행됨", new Object[] { entry.getKey(), entry.getValue(), Integer.valueOf(processNo), Long.valueOf(workerCall.getProcessId()) }));
                    }
                } else {
                    for (Map.Entry<Integer, Long> entry : this.processIds.entrySet()) {
                        if (((Long)entry.getValue()).longValue() == workerCall.getProcessId())
                            finalFail(String.format("[오답]\n> Process #%d에서 실행해야 하는 Worker가 Process Id(%d)에서 실행되었는데, Process #%d에서 실행해야 하는 Worker도 Process Id(%d)에서 실행됨", new Object[] { entry.getKey(), entry.getValue(), Integer.valueOf(processNo), Long.valueOf(workerCall.getProcessId()) }));
                    }
                    this.processIds.put(Integer.valueOf(processNo), Long.valueOf(workerCall.getProcessId()));
                }
                threadIds = this.processThreadIds.get(Integer.valueOf(processNo));
                if (threadIds == null) {
                    threadIds = new HashMap<>();
                    threadIds.put(Integer.valueOf(threadNo), Long.valueOf(workerCall.getThreadId()));
                    this.processThreadIds.put(Integer.valueOf(processNo), threadIds);
                } else if (threadIds.containsKey(Integer.valueOf(threadNo))) {
                    for (Map.Entry<Integer, Long> entry : threadIds.entrySet()) {
                        if (((Integer)entry.getKey()).intValue() == threadNo && ((Long)entry.getValue()).longValue() != workerCall.getThreadId())
                            finalFail(String.format("[오답]\n> Process #%d/Thread #%d에서 실행해야 하는 Worker가 이전에는 Thread Id(%d)에서 실행되었는데, 이번에는 Thread Id(%d)에서 실행됨 (불일치)", new Object[] { Integer.valueOf(processNo), Integer.valueOf(threadNo), entry.getValue(), Long.valueOf(workerCall.getThreadId()) }));
                        if (((Integer)entry.getKey()).intValue() != threadNo && ((Long)entry.getValue()).longValue() == workerCall.getThreadId())
                            finalFail(String.format("[오답]\n> Process #%d/Thread #%d에서 실행해야 하는 Worker가 Thread Id(%d)에서 실행되었는데, Process #%d/Thread #%d에서 실행해야 하는 Worker도 Thread Id(%d)에서 실행됨", new Object[] { Integer.valueOf(processNo), entry.getKey(), entry.getValue(), Integer.valueOf(processNo), Integer.valueOf(threadNo), Long.valueOf(workerCall.getThreadId()) }));
                    }
                } else {
                    for (Map.Entry<Integer, Long> entry : threadIds.entrySet()) {
                        if (((Long)entry.getValue()).longValue() == workerCall.getThreadId())
                            finalFail(String.format("[오답]\n> Process #%d/Thread #%d에서 실행해야 하는 Worker가 Thread Id(%d)에서 실행되었는데, Process #%d/Thread #%d에서 실행해야 하는 Worker도 Thread Id(%d)에서 실행됨", new Object[] { Integer.valueOf(processNo), entry.getKey(), entry.getValue(), Integer.valueOf(processNo), Integer.valueOf(threadNo), Long.valueOf(workerCall.getThreadId()) }));
                    }
                    threadIds.put(Integer.valueOf(threadNo), Long.valueOf(workerCall.getThreadId()));
                }
                return;
        }
        finalFail("[오답]\n>Input Queue 데이터 1건이 입력되었는데 Worker가 1회 이상 실행됨");
    }
}
