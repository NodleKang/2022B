package test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import java.lang.ProcessBuilder.Redirect;

public class MyProcessRunner {

    public static void main(String[] args) throws IOException, InterruptedException{
        String currentPath = MyFile.getCurrentDirectoryFullPath();

    	String libPath = currentPath + "/lib/*;.";
    	System.out.println("libPath: "+libPath);
        String[] command = new String[] {"java", "-classpath", libPath, "test/TestProcess"};
        MyProcessRunner processRunner = new MyProcessRunner();
        processRunner.byProcessBuilderRedirect(command); // 권장하는 방식
    }

    public void byProcessBuilderRedirect(String[] command) throws IOException, InterruptedException{
        ProcessBuilder builder = new ProcessBuilder(command);
        
        String currentPath = MyFile.getCurrentDirectoryFullPath();
        String path = currentPath+"/bin";
        System.out.println("currentPath: " + currentPath);
        System.out.println("path: " + path);
        builder.directory(new File(path));
        builder.command(command);

        /*
         * ProcessBuilder.Redirect.INHERIT
         * 부모 프로세스의 입출력 스트림을 자식 프로세스에게 상속하도록 지정합니다.
         * 즉, 자식 프로세스가 부모 프로세스와 같은 표준 입력, 표준 출력 및 표준 오류 스트림을 사용할 수 있게 됩니다.
         *
         * redirectOutput() 메소드에 값을 지정하지 않으면 기본값은 Redirect.PIPE 이며,
         * 이 때는 Process.getInputStream()으로 얻어온 스트림을 다뤄야 합니다.
         *
         * redirectOutput(File) 메소드로 직접 스트림을 출력할 파일로 지정할 수도 있습니다.
         */
        /*
         * ProcessBuilder.Redirect.PIPE
         * 자식 프로세스의 입출력 스트림을 부모 프로세스와 연결하지 않고, 자식 프로세스 내부에서만 사용하도록 지정합니다.
         */
        /*
         * ProcessBuilder.Redirect.DISCARD
         * 자식 프로세스의 출력을 무시하고 버리도록 지정합니다.
         */
        builder.redirectOutput(Redirect.INHERIT);
        builder.redirectError(Redirect.INHERIT);
        builder.redirectInput(Redirect.INHERIT);
        Process p = builder.start();
    }

    public void byRuntime(String[] command) throws IOException, InterruptedException {
        Runtime runtime = Runtime.getRuntime();
        Process process = runtime.exec(command);
        printStream(process);
    }

    public void byProcessBuilder(String[] command) throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder(command);
        Process process = builder.start();
        printStream(process);
    }

    // 프로세스에서 얻은 스트림을 System.out으로 복사
    private void printStream(Process process) throws IOException, InterruptedException {
        process.waitFor();
        try (InputStream processOut = process.getInputStream()) {

        }
    }

    public void copy(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[1024];
        int n = 0;
        while ( (n = input.read(buffer)) != -1) {
            output.write(buffer, 0, n);
        }
    }

    public void process(String jarFileName, String imageDir) throws IOException {

        ProcessBuilder builderNoArgs = new ProcessBuilder("java", "-jar", jarFileName);
        ProcessBuilder builderWithArgs = new ProcessBuilder("java", "-jar", jarFileName, imageDir);

        builderNoArgs.directory(new File(Paths.get("").toAbsolutePath().toString()));

        /*
         * ProcessBuilder.Redirect.INHERIT
         * 프로세스 스트림 버퍼 문제를 방지하기 위해 수행결과를 직접 출력하지 않고 ProcessBuilder애 위임함
         * 부모 프로세스의 입출력 스트림을 자식 프로세스에게 상속하도록 지정
         * 즉, 자식 프로세스가 부모 프로세스의 표준 입력, 표준 출력 및 표준 오류 스트림을 사용할 수 있게 함
         * 자식 프로세스는 부모 프로세스와 같은 입출력 스트림을 사용할 수 있게 함
         */
        /*
         * ProcessBuilder.Redirect.PIPE
         * 자식 프로세스의 입출력 스트림을 부모 프로세스와 연결하지 않고, 자식 프로세스 내부에서만 사용하도록 지정
         */
        /*
         * ProcessBuilder.Redirect.DISCARD
         * 자식 프로세스의 출력을 무시하고 버리도록 지정
         */
        builderNoArgs.redirectOutput(Redirect.INHERIT);
        builderNoArgs.redirectError(Redirect.INHERIT);
        builderNoArgs.redirectInput(Redirect.INHERIT);
        Process p = builderNoArgs.start();
        try {
            // 프로세스가 종료될 때까지 블록킹
            // 무한정 대기를 방지하기 위해 timeout 설정
            // 데몬이나 서비스 형태로 사용되는 프로세스에서는 사용하지 않는다 (종료되기 않기 때문)
            p.waitFor(60, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 프로세스 종료
            // 데몬이나 서비스 형태로 사용되는 프로세스에서는 사용하지 않는다 (종료되기 때문)
            p.destroy();
        }
    }
}

