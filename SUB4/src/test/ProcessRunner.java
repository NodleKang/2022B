package test;

import java.io.*;

public class ProcessRunner {

    public static void main(String[] args) {
        int processCount = 2;
        int threadCount = 2;
        ProcessBuilder[] processBuilders = new ProcessBuilder[processCount];
        for (int i = 0; i < processCount; i++) {
            String path = MyFile.getCurrentDirectoryFullPath();
            path = path + "\\SUB4\\src";
            System.out.println("Current Path: " + path);
            processBuilders[i] = new ProcessBuilder();
            processBuilders[i].directory(new File(path));
            processBuilders[i].redirectErrorStream(true);
            processBuilders[i].command("java", "-cp", ".", "test/MyProcess.java", Integer.toString(i), Integer.toString(threadCount));
            try {
                Process process = processBuilders[i].start();

                InputStream inputStream = process.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
