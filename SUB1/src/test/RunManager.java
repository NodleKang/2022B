package test;

import com.lgcns.test.Worker;

import java.util.HashMap;
import java.util.Scanner;

public class RunManager {

	public static void main(String[] args) {
		testOnConsole();
	}

	public static void testOnConsole() {
		HashMap<Integer, Worker> workerHashMap = new HashMap<>();

		Scanner sc = new Scanner(System.in);
		while (true) {
			String line = sc.nextLine();
			if (line.equals("exit")) {
				break;
			} else {
				String[] commands = MyString.splitToStringArray(line, " ", true);
				int queueNo = Integer.parseInt(commands[0]);
				String value = commands[1];
				if (!workerHashMap.containsKey(queueNo)) {
					workerHashMap.put(queueNo, new Worker(queueNo));
				}
				String result = workerHashMap.get(queueNo).run(value);
				if (result != null) {
					System.out.println(result);
				}
			}
		}
	}
}
