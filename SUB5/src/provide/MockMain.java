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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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

    private String configurationString;

    private Configuration configuration;

    private boolean scenarioStarted;

    private List<BlockingQueue<String>> inputQueues = new ArrayList<>();

    private BlockingQueue<String> outputQueue = new ArrayBlockingQueue<>(100);

    private BlockingQueue<WorkerCall> workerCallQueue = new ArrayBlockingQueue<>(100);

    private Map<Integer, Long> processIds = new HashMap<>();

    private Map<Integer, Map<Integer, Long>> processThreadIds = new HashMap<>();

    public static void main(String[] args) throws Exception {
        (new MockMain()).start();
    }

    public void start() throws Exception {
        loadConfiguration();
        startServer("http://127.0.0.1:9999/workerCall", (Servlet)new WorkerCallServlet());
        createInputQueue();
        startInputQueueServer();
        startServer(this.configuration.getOutputQueueURI(), (Servlet)new OutputQueueServlet());
        startServer("http://127.0.0.1:8080/queueInfo", (Servlet)new ControllerServlet());
        System.out.println();
        System.out.println("[Controller] 요청 대기중 (Controller 요청/응답이 수행되면 채점 시나리오에 따른 Input Queue 입력이 시작됨");
        (new Thread(() -> {
            try {
                Thread.sleep(10000L);
            } catch (InterruptedException interruptedException) {}
            if (!this.scenarioStarted)
                finalFail("[오답]\n>Controller에 대한 구현프로그램(SP_TEST)의 요청 없음(최대 10초)");
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
        this.configurationString = new String(Files.readAllBytes(Paths.get("../mock/sub5/CONFIGURATION.JSON", new String[0])));
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
            String value = "";
            try {
                value = ((BlockingQueue<String>)MockMain.this.inputQueues.get(this.queueNo)).take();
                res.flushBuffer();
                System.out.println(String.format("[Input Queue(%d)] HTTP 응답 ('%s')", new Object[] { Integer.valueOf(this.queueNo), value }));
                res.setStatus(200);
                res.getWriter().write(value);
            } catch (Exception e) {
                ((BlockingQueue<String>)MockMain.this.inputQueues.get(this.queueNo)).add(value);
            }
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
        //   47: lookupswitch default -> 843, -1952183039 -> 112, -1881097187 -> 126, -448182674 -> 140, 68795 -> 154, 69820330 -> 168, 76397197 -> 182, 78984887 -> 196
        //   112: aload #5
        //   114: ldc_w 'OUTPUT'
        //   117: invokevirtual equals : (Ljava/lang/Object;)Z
        //   120: ifne -> 285
        //   123: goto -> 843
        //   126: aload #5
        //   128: ldc_w 'RESULT'
        //   131: invokevirtual equals : (Ljava/lang/Object;)Z
        //   134: ifne -> 285
        //   137: goto -> 843
        //   140: aload #5
        //   142: ldc_w 'KILL_PROCESS'
        //   145: invokevirtual equals : (Ljava/lang/Object;)Z
        //   148: ifne -> 709
        //   151: goto -> 843
        //   154: aload #5
        //   156: ldc_w 'END'
        //   159: invokevirtual equals : (Ljava/lang/Object;)Z
        //   162: ifne -> 821
        //   165: goto -> 843
        //   168: aload #5
        //   170: ldc_w 'INPUT'
        //   173: invokevirtual equals : (Ljava/lang/Object;)Z
        //   176: ifne -> 232
        //   179: goto -> 843
        //   182: aload #5
        //   184: ldc_w 'PRINT'
        //   187: invokevirtual equals : (Ljava/lang/Object;)Z
        //   190: ifne -> 210
        //   193: goto -> 843
        //   196: aload #5
        //   198: ldc_w 'SLEEP'
        //   201: invokevirtual equals : (Ljava/lang/Object;)Z
        //   204: ifne -> 221
        //   207: goto -> 843
        //   210: getstatic java/lang/System.out : Ljava/io/PrintStream;
        //   213: aload #4
        //   215: invokevirtual println : (Ljava/lang/String;)V
        //   218: goto -> 843
        //   221: aload #4
        //   223: invokestatic parseLong : (Ljava/lang/String;)J
        //   226: invokestatic sleep : (J)V
        //   229: goto -> 843
        //   232: aload #4
        //   234: ldc_w ' '
        //   237: invokevirtual split : (Ljava/lang/String;)[Ljava/lang/String;
        //   240: astore #6
        //   242: aload #6
        //   244: iconst_0
        //   245: aaload
        //   246: invokestatic valueOf : (Ljava/lang/String;)Ljava/lang/Long;
        //   249: invokevirtual longValue : ()J
        //   252: lstore #7
        //   254: aload #6
        //   256: iconst_1
        //   257: aaload
        //   258: invokestatic valueOf : (Ljava/lang/String;)Ljava/lang/Integer;
        //   261: invokevirtual intValue : ()I
        //   264: istore #9
        //   266: aload #6
        //   268: iconst_2
        //   269: aaload
        //   270: astore #10
        //   272: aload_0
        //   273: iload #9
        //   275: lload #7
        //   277: aload #10
        //   279: invokespecial sendInputQueueData : (IJLjava/lang/String;)V
        //   282: goto -> 843
        //   285: iconst_0
        //   286: istore #11
        //   288: goto -> 367
        //   291: aload_0
        //   292: getfield inputQueues : Ljava/util/List;
        //   295: iload #11
        //   297: invokeinterface get : (I)Ljava/lang/Object;
        //   302: checkcast java/util/concurrent/BlockingQueue
        //   305: invokeinterface size : ()I
        //   310: ifeq -> 364
        //   313: aload_0
        //   314: getfield inputQueues : Ljava/util/List;
        //   317: iload #11
        //   319: invokeinterface get : (I)Ljava/lang/Object;
        //   324: checkcast java/util/concurrent/BlockingQueue
        //   327: invokeinterface poll : ()Ljava/lang/Object;
        //   332: checkcast java/lang/String
        //   335: astore #12
        //   337: aload_0
        //   338: ldc_w '[오답]\\n> Input Queue(%d)에 데이터가 입력되었지만 구현프로그램(SP_TEST)이 요청하지 않음\\n>Input Queue 데이터: %s'
        //   341: iconst_2
        //   342: anewarray java/lang/Object
        //   345: dup
        //   346: iconst_0
        //   347: iload #11
        //   349: invokestatic valueOf : (I)Ljava/lang/Integer;
        //   352: aastore
        //   353: dup
        //   354: iconst_1
        //   355: aload #12
        //   357: aastore
        //   358: invokestatic format : (Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;
        //   361: invokespecial finalFail : (Ljava/lang/String;)V
        //   364: iinc #11, 1
        //   367: iload #11
        //   369: aload_0
        //   370: getfield inputQueues : Ljava/util/List;
        //   373: invokeinterface size : ()I
        //   378: if_icmplt -> 291
        //   381: ldc_w 'NO_OUTPUT'
        //   384: aload #4
        //   386: invokevirtual equals : (Ljava/lang/Object;)Z
        //   389: ifeq -> 440
        //   392: aload_0
        //   393: getfield outputQueue : Ljava/util/concurrent/BlockingQueue;
        //   396: invokeinterface size : ()I
        //   401: ifeq -> 692
        //   404: aload_0
        //   405: getfield outputQueue : Ljava/util/concurrent/BlockingQueue;
        //   408: invokeinterface poll : ()Ljava/lang/Object;
        //   413: checkcast java/lang/String
        //   416: astore #11
        //   418: aload_0
        //   419: ldc_w '[오답]\\n> Output Queue에 테스트 시나리오에 정의되지 않은 데이터가 요청됨\\n>정답: 데이터 없음\\n>실제요청: %s'
        //   422: iconst_1
        //   423: anewarray java/lang/Object
        //   426: dup
        //   427: iconst_0
        //   428: aload #11
        //   430: aastore
        //   431: invokestatic format : (Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;
        //   434: invokespecial finalFail : (Ljava/lang/String;)V
        //   437: goto -> 692
        //   440: aload_0
        //   441: getfield outputQueue : Ljava/util/concurrent/BlockingQueue;
        //   444: invokeinterface size : ()I
        //   449: tableswitch default -> 622, 0 -> 472, 1 -> 494
        //   472: aload_0
        //   473: ldc_w '[오답]\\n> Output Queue에 테스트 시나리오에 정의된 데이터가 요청되지 않음\\n>정답: %s\\n> 실제요청: 요청 없음'
        //   476: iconst_1
        //   477: anewarray java/lang/Object
        //   480: dup
        //   481: iconst_0
        //   482: aload #4
        //   484: aastore
        //   485: invokestatic format : (Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;
        //   488: invokespecial finalFail : (Ljava/lang/String;)V
        //   491: goto -> 692
        //   494: aload_0
        //   495: getfield outputQueue : Ljava/util/concurrent/BlockingQueue;
        //   498: invokeinterface poll : ()Ljava/lang/Object;
        //   503: checkcast java/lang/String
        //   506: astore #11
        //   508: new com/google/gson/Gson
        //   511: dup
        //   512: invokespecial <init> : ()V
        //   515: aload #4
        //   517: ldc_w provide/gson/OutputQueueData
        //   520: invokevirtual fromJson : (Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/Object;
        //   523: checkcast provide/gson/OutputQueueData
        //   526: astore #12
        //   528: new com/google/gson/Gson
        //   531: dup
        //   532: invokespecial <init> : ()V
        //   535: aload #11
        //   537: ldc_w provide/gson/OutputQueueData
        //   540: invokevirtual fromJson : (Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/Object;
        //   543: checkcast provide/gson/OutputQueueData
        //   546: astore #13
        //   548: aload #12
        //   550: invokevirtual getResult : ()Ljava/util/List;
        //   553: aload #13
        //   555: invokevirtual getResult : ()Ljava/util/List;
        //   558: invokeinterface equals : (Ljava/lang/Object;)Z
        //   563: ifne -> 692
        //   566: aload_0
        //   567: ldc_w '[오답]\\n> Output Queue에 요청된 데이터가 테스트 시나리오에 정의된 데이터와 불일치\\n>정답: %s\\n>설제요청: %s'
        //   570: iconst_2
        //   571: anewarray java/lang/Object
        //   574: dup
        //   575: iconst_0
        //   576: aload #4
        //   578: aastore
        //   579: dup
        //   580: iconst_1
        //   581: aload #11
        //   583: aastore
        //   584: invokestatic format : (Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;
        //   587: invokespecial finalFail : (Ljava/lang/String;)V
        //   590: goto -> 692
        //   593: astore #12
        //   595: aload_0
        //   596: ldc_w '[오답]\\n> Output Queue에 요청된 데이터가 테스트 시나리오에 정의된 데이터와 불일치\\n>정답: %s\\n>설제요청: %s'
        //   599: iconst_2
        //   600: anewarray java/lang/Object
        //   603: dup
        //   604: iconst_0
        //   605: aload #4
        //   607: aastore
        //   608: dup
        //   609: iconst_1
        //   610: aload #11
        //   612: aastore
        //   613: invokestatic format : (Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;
        //   616: invokespecial finalFail : (Ljava/lang/String;)V
        //   619: goto -> 692
        //   622: new java/util/ArrayList
        //   625: dup
        //   626: invokespecial <init> : ()V
        //   629: astore #12
        //   631: goto -> 644
        //   634: aload #12
        //   636: aload #13
        //   638: invokeinterface add : (Ljava/lang/Object;)Z
        //   643: pop
        //   644: aload_0
        //   645: getfield outputQueue : Ljava/util/concurrent/BlockingQueue;
        //   648: invokeinterface poll : ()Ljava/lang/Object;
        //   653: checkcast java/lang/String
        //   656: dup
        //   657: astore #13
        //   659: ifnonnull -> 634
        //   662: aload_0
        //   663: ldc_w '[오답]\\n> Output Queue에 1을 초과하는 데이터가 요청됨\\n>정답: %s\\n>실제요청: %s'
        //   666: iconst_2
        //   667: anewarray java/lang/Object
        //   670: dup
        //   671: iconst_0
        //   672: aload #4
        //   674: aastore
        //   675: dup
        //   676: iconst_1
        //   677: ldc_w ','
        //   680: aload #12
        //   682: invokestatic join : (Ljava/lang/CharSequence;Ljava/lang/Iterable;)Ljava/lang/String;
        //   685: aastore
        //   686: invokestatic format : (Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;
        //   689: invokespecial finalFail : (Ljava/lang/String;)V
        //   692: aload_3
        //   693: ldc_w 'OUTPUT'
        //   696: invokevirtual equals : (Ljava/lang/Object;)Z
        //   699: ifeq -> 843
        //   702: aload_0
        //   703: invokespecial checkProcessThread : ()V
        //   706: goto -> 843
        //   709: aload #4
        //   711: invokestatic parseInt : (Ljava/lang/String;)I
        //   714: istore #11
        //   716: aload_0
        //   717: getfield processIds : Ljava/util/Map;
        //   720: iload #11
        //   722: invokestatic valueOf : (I)Ljava/lang/Integer;
        //   725: invokeinterface get : (Ljava/lang/Object;)Ljava/lang/Object;
        //   730: checkcast java/lang/Long
        //   733: invokevirtual longValue : ()J
        //   736: lstore #12
        //   738: aload_0
        //   739: getfield processIds : Ljava/util/Map;
        //   742: iload #11
        //   744: invokestatic valueOf : (I)Ljava/lang/Integer;
        //   747: invokeinterface remove : (Ljava/lang/Object;)Ljava/lang/Object;
        //   752: pop
        //   753: aload_0
        //   754: getfield processThreadIds : Ljava/util/Map;
        //   757: iload #11
        //   759: invokestatic valueOf : (I)Ljava/lang/Integer;
        //   762: invokeinterface remove : (Ljava/lang/Object;)Ljava/lang/Object;
        //   767: pop
        //   768: aload_0
        //   769: getfield processIds : Ljava/util/Map;
        //   772: iload #11
        //   774: invokestatic valueOf : (I)Ljava/lang/Integer;
        //   777: invokeinterface remove : (Ljava/lang/Object;)Ljava/lang/Object;
        //   782: pop
        //   783: ldc_w 'taskkill /F /pid %d'
        //   786: iconst_1
        //   787: anewarray java/lang/Object
        //   790: dup
        //   791: iconst_0
        //   792: lload #12
        //   794: invokestatic valueOf : (J)Ljava/lang/Long;
        //   797: aastore
        //   798: invokestatic format : (Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;
        //   801: astore #14
        //   803: getstatic java/lang/System.out : Ljava/io/PrintStream;
        //   806: aload #14
        //   808: invokevirtual println : (Ljava/lang/String;)V
        //   811: aload_0
        //   812: aload #14
        //   814: invokevirtual execCmd : (Ljava/lang/String;)Ljava/lang/String;
        //   817: pop
        //   818: goto -> 843
        //   821: aload_0
        //   822: new java/lang/StringBuilder
        //   825: dup
        //   826: ldc_w '[오답]\\n> '
        //   829: invokespecial <init> : (Ljava/lang/String;)V
        //   832: aload #4
        //   834: invokevirtual append : (Ljava/lang/String;)Ljava/lang/StringBuilder;
        //   837: invokevirtual toString : ()Ljava/lang/String;
        //   840: invokespecial finalSuccess : (Ljava/lang/String;)V
        //   843: return
        // Line number table:
        //   Java source line number -> byte code offset
        //   #281	-> 0
        //   #282	-> 10
        //   #285	-> 11
        //   #286	-> 19
        //   #287	-> 23
        //   #289	-> 40
        //   #291	-> 210
        //   #292	-> 218
        //   #294	-> 221
        //   #295	-> 229
        //   #297	-> 232
        //   #298	-> 242
        //   #299	-> 254
        //   #300	-> 266
        //   #302	-> 272
        //   #304	-> 282
        //   #308	-> 285
        //   #309	-> 291
        //   #310	-> 313
        //   #311	-> 337
        //   #308	-> 364
        //   #315	-> 381
        //   #316	-> 392
        //   #317	-> 404
        //   #318	-> 418
        //   #320	-> 437
        //   #321	-> 440
        //   #323	-> 472
        //   #324	-> 491
        //   #326	-> 494
        //   #330	-> 508
        //   #331	-> 528
        //   #332	-> 548
        //   #335	-> 566
        //   #337	-> 590
        //   #339	-> 595
        //   #342	-> 619
        //   #344	-> 622
        //   #346	-> 631
        //   #347	-> 634
        //   #346	-> 644
        //   #349	-> 662
        //   #354	-> 692
        //   #355	-> 702
        //   #357	-> 706
        //   #359	-> 709
        //   #360	-> 716
        //   #362	-> 738
        //   #363	-> 753
        //   #365	-> 768
        //   #366	-> 783
        //   #367	-> 803
        //   #369	-> 811
        //   #370	-> 818
        //   #372	-> 821
        //   #375	-> 843
        // Local variable table:
        //   start	length	slot	name	descriptor
        //   0	844	0	this	Lprovide/MockMain;
        //   0	844	1	line	Ljava/lang/String;
        //   19	825	2	lineSplit	[Ljava/lang/String;
        //   23	821	3	command	Ljava/lang/String;
        //   40	804	4	data	Ljava/lang/String;
        //   242	43	6	dataSplit	[Ljava/lang/String;
        //   254	31	7	timestamp	J
        //   266	19	9	queueNo	I
        //   272	13	10	value	Ljava/lang/String;
        //   288	93	11	i	I
        //   337	27	12	inputQueueString	Ljava/lang/String;
        //   418	19	11	outputQueueString	Ljava/lang/String;
        //   508	114	11	outputQueueString	Ljava/lang/String;
        //   528	62	12	scenarioData	Lprovide/gson/OutputQueueData;
        //   548	42	13	outputQueueData	Lprovide/gson/OutputQueueData;
        //   595	24	12	e	Ljava/lang/Exception;
        //   631	61	12	outputQueueList	Ljava/util/List;
        //   634	10	13	temp	Ljava/lang/String;
        //   659	33	13	temp	Ljava/lang/String;
        //   716	105	11	processNo	I
        //   738	83	12	processId	J
        //   803	18	14	cmd	Ljava/lang/String;
        // Local variable type table:
        //   start	length	slot	name	signature
        //   631	61	12	outputQueueList	Ljava/util/List<Ljava/lang/String;>;
        // Exception table:
        //   from	to	target	type
        //   508	590	593	java/lang/Exception
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
                finalFail(String.format("[오답]\n>Input Queue Worker", new Object[0]));
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
                            finalFail(String.format("[오답]\n>Process #%d에서 실행해야 하는 Worker가 이전에는 Process Id(%d)에서 실행되었는데, 이번에는 Process Id(%d)에서 실행됨(불일치)", new Object[] { Integer.valueOf(processNo), entry.getValue(), Long.valueOf(workerCall.getProcessId()) }));
                        if (((Integer)entry.getKey()).intValue() != processNo && ((Long)entry.getValue()).longValue() == workerCall.getProcessId())
                            finalFail(String.format("[오답]\n>Process #%d에서 실행해야 하는 Worker가 Process Id(%d)에서 실행되었는데, Process #%d에서 실행해야 하는 Worker도 Process Id(%d)에서 실행됨", new Object[] { entry.getKey(), entry.getValue(), Integer.valueOf(processNo), Long.valueOf(workerCall.getProcessId()) }));
                    }
                } else {
                    for (Map.Entry<Integer, Long> entry : this.processIds.entrySet()) {
                        if (((Long)entry.getValue()).longValue() == workerCall.getProcessId())
                            finalFail(String.format("[오답]\n>Process #%d에서 실행해야 하는 Worker가 Process Id(%d)에서 실행되었는데, Process #%d에서 실행해야 하는 Worker도 Process Id(%d)에서 실행됨", new Object[] { entry.getKey(), entry.getValue(), Integer.valueOf(processNo), Long.valueOf(workerCall.getProcessId()) }));
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
                            finalFail(String.format("[오답]\n>Process #%d/Thread #%d에서 실행해야 하는 Worker가 이전에는 Thread Id(%d)에서 실행되었는데, 이번에는 Thread Id(%d)에서 실행됨(불일치)", new Object[] { Integer.valueOf(processNo), Integer.valueOf(threadNo), entry.getValue(), Long.valueOf(workerCall.getThreadId()) }));
                        if (((Integer)entry.getKey()).intValue() != threadNo && ((Long)entry.getValue()).longValue() == workerCall.getThreadId())
                            finalFail(String.format("[오답]\n>Process #%d/Thread #%d에서 실행해야 하는 Worker가 Thread Id(%d)에서 실행되었는데, Process #%d/Thread #%d에서 실행해야 하는 Worker도 Thread Id(%d)에서 실행됨", new Object[] { Integer.valueOf(processNo), entry.getKey(), entry.getValue(), Integer.valueOf(processNo), Integer.valueOf(threadNo), Long.valueOf(workerCall.getThreadId()) }));
                    }
                } else {
                    for (Map.Entry<Integer, Long> entry : threadIds.entrySet()) {
                        if (((Long)entry.getValue()).longValue() == workerCall.getThreadId())
                            finalFail(String.format("[오답]\n>Process #%d/Thread #%d에서 실행해야 하는 Worker가 Thread Id(%d)에서 실행되었는데, Process #%d/Thread #%d에서 실행해야 하는 Worker도 Thread Id(%d)에서 실행됨", new Object[] { Integer.valueOf(processNo), entry.getKey(), entry.getValue(), Integer.valueOf(processNo), Integer.valueOf(threadNo), Long.valueOf(workerCall.getThreadId()) }));
                    }
                    threadIds.put(Integer.valueOf(threadNo), Long.valueOf(workerCall.getThreadId()));
                }
                return;
        }
        finalFail("[오답]\n>Input Queue 데이터 1건이 입력되었는데 Worker가 1회 이상 실행됨");
    }

    public String execCmd(String cmd) {
        try {
            Process process = Runtime.getRuntime().exec("cmd /c " + cmd);
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getErrorStream()));
            String line = null;
            StringBuffer sb = new StringBuffer();
            sb.append(cmd);
            while ((line = reader.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
