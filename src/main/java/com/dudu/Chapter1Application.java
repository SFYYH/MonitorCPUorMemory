package com.dudu;

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
		while (!shouldStop.get()) {
			if (startCreating.get()) {
				String newString = new String("开始测试");
				synchronized (stringList) {
					stringList.add(newString);
				}
			} else {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void monitorSystem(String type, int du, int threshold, int startThreshold) {
		OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

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

			System.out.println("当前 CPU 使用率: " + String.format("%.2f", cpuLoad) + "%");

			if (cpuLoad < startThreshold) {
				if (!startCreating.get()) {
					System.out.println("CPU 使用率低于 " + startThreshold + "%，开始创建字符串");
					startCreating.set(true);
				}
			} else if (cpuLoad > threshold) {
				if (startCreating.get()) {
					System.out.println("CPU 使用率超过阈值 " + threshold + "%，停止创建字符串");
					clearStringList();
					startCreating.set(false);
				}
			}

			try {
				Thread.sleep(1000);
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