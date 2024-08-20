package com.dudu;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import com.sun.management.OperatingSystemMXBean;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SpringBootApplication
public class Chapter1Application {
	private List<Double> cpuUsageList = new ArrayList<>();
	private List<String> stringList = new ArrayList<>();
	private AtomicBoolean shouldStop = new AtomicBoolean(false);
	private AtomicBoolean startCreating = new AtomicBoolean(false);
	private static final int INITIAL_DELAY_SECONDS = 5;
	private static final int DEFAULT_THRESHOLD = 70;
	private static final int DEFAULT_START_THRESHOLD = 50;

	@GetMapping("/loadCPU")
	public String monitorCPU(
			@RequestParam(defaultValue = "5") int du,
			@RequestParam(defaultValue = "70") int threshold,
			@RequestParam(defaultValue = "50") int startThreshold) {
		return startMonitoring("CPU", du, threshold, startThreshold);
	}

	@GetMapping("/loadMemory")
	public String monitorMemory(
			@RequestParam(defaultValue = "5") int du,
			@RequestParam(defaultValue = "70") int threshold,
			@RequestParam(defaultValue = "50") int startThreshold) {
		return startMonitoring("Memory", du, threshold, startThreshold);
	}

	@GetMapping("/loadAll")
	public String monitorAll(
			@RequestParam(defaultValue = "5") int du,
			@RequestParam(defaultValue = "70") int threshold,
			@RequestParam(defaultValue = "50") int startThreshold) {
		return startMonitoring("All", du, threshold, startThreshold);
	}

	private String startMonitoring(String type, int du, int threshold, int startThreshold) {
		resetMonitoringState();
		Thread creatorThread = new Thread(this::createStrings);
		Thread monitorThread = new Thread(() -> monitorSystem(type, du, threshold, startThreshold));

		creatorThread.start();
		monitorThread.start();

		return "开始监控" + type + "，持续时间: " + du + " 分钟，阈值: " + threshold + "%，启动阈值: " + startThreshold + "%";
	}

	private void resetMonitoringState() {
		shouldStop.set(false);
		startCreating.set(false);
		stringList.clear();
	}

	private void createStrings() {
		int workload = 1000; // 提高初始工作量
		int maxWorkload = 1000000; // 大幅提高最大工作量上限
		while (!shouldStop.get()) {
			if (startCreating.get()) {
				for (int i = 0; i < workload; i++) {
					String newString = new String("测试数据 " + i);
					synchronized (stringList) {
						stringList.add(newString);
					}
				}
				// 更快地增加工作量
				workload = (int) Math.min(workload * 1.2, maxWorkload);
				try {
					// 减少休眠时间，但保持最小值
					long sleepTime = Math.max(1, 100 - (workload / 10000));
					Thread.sleep(sleepTime);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			} else {
				workload = 1000; // 重置工作量，但保持较高的起始点
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		}
	}
	private void monitorSystem(String type, int du, int threshold, int startThreshold) {
		OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		final String ANSI_RESET = "\u001B[0m";
		final String ANSI_GREEN = "\u001B[32m";
		final String ANSI_RED = "\u001B[31m";

		try {
			System.out.println("等待 " + INITIAL_DELAY_SECONDS + " 秒后开始创建字符串并监控系统资源...");
			Thread.sleep(INITIAL_DELAY_SECONDS * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		long startTime = System.currentTimeMillis();
		long endTime = startTime + (du * 60 * 1000);

		while (System.currentTimeMillis() < endTime && !shouldStop.get()) {
			double cpuLoad = osBean.getSystemCpuLoad() * 100;
			cpuUsageList.add(cpuLoad);  // 用来计算平均占用率
			String currentTime = LocalDateTime.now().format(dtf);

			System.out.println("[" + currentTime + "] 当前 CPU 使用率: " + String.format("%.2f", cpuLoad) + "%");

			if (cpuLoad < startThreshold) {
				if (!startCreating.get()) {
					System.out.println(ANSI_GREEN + "CPU 使用率低于 " + startThreshold + "%，开始创建字符串" + ANSI_RESET);
					startCreating.set(true);
				}
			} else if (cpuLoad > threshold) {
				if (startCreating.get()) {
					System.out.println(ANSI_RED + "CPU 使用率超过阈值 " + threshold + "%，停止创建字符串" + ANSI_RESET);
					clearStringList();
					startCreating.set(false);
				}
			}

			try {
				Thread.sleep(300); // 更频繁地检查CPU使用率
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		stopMonitoring(startTime);
	}
	private void stopMonitoring(long startTime) {
		shouldStop.set(true);
		startCreating.set(false);
		clearStringList();
		long endTime = System.currentTimeMillis();
		long duration = endTime - startTime;
		System.out.println("监控结束");
		System.out.println("程序运行时间: " + formatDuration(duration));
		// 计算并打印平均 CPU 使用率
		double averageCpuUsage = cpuUsageList.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
		System.out.printf("平均 CPU 使用率: %.2f%%\n", averageCpuUsage);
		cpuUsageList.clear();
	}

	private void clearStringList() {
		System.out.println("清空字符串列表，释放内存");
		System.out.println("清空前的列表大小: " + stringList.size());
		stringList.clear();
		System.gc();
		System.out.println("清空后的列表大小: " + stringList.size());
	}

	private String formatDuration(long durationMillis) {
		long seconds = durationMillis / 1000;
		long minutes = seconds / 60;

		seconds %= 60;

		return minutes + " 分钟 " + seconds + " 秒";
	}

	public static void main(String[] args) {
		SpringApplication.run(Chapter1Application.class, args);
	}
}
