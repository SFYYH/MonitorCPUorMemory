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
	// 存储生成的字符串列表
	private List<String> stringList = new ArrayList<>();
	// 控制监控是否应该停止
	private AtomicBoolean shouldStop = new AtomicBoolean(false);
	// 控制是否开始创建字符串
	private AtomicBoolean startCreating = new AtomicBoolean(false);
	// 初始延迟时间，单位为秒
	private static final int INITIAL_DELAY_SECONDS = 5;
	// 默认的监控阈值
	private static final int DEFAULT_THRESHOLD = 70;
	// 默认的启动阈值
	private static final int DEFAULT_START_THRESHOLD = 50;

	/**
	 * 监控CPU使用率
	 * @param du 监控持续时间，单位为分钟
	 * @param threshold 监控阈值，当CPU使用率超过该值时采取行动
	 * @param startThreshold 启动阈值，当CPU使用率低于该值时开始创建字符串
	 * @return 监控的详细信息
	 */
	@GetMapping("/loadCPU")
	public String monitorCPU(
			@RequestParam(defaultValue = "5") int du,
			@RequestParam(defaultValue = "70") int threshold,
			@RequestParam(defaultValue = "50") int startThreshold) {
		return startMonitoring("CPU", du, threshold, startThreshold);
	}

	/**
	 * 监控内存使用率
	 * @param du 监控持续时间，单位为分钟
	 * @param threshold 监控阈值，当内存使用率超过该值时采取行动
	 * @param startThreshold 启动阈值，当内存使用率低于该值时开始创建字符串
	 * @return 监控的详细信息
	 */
	@GetMapping("/loadMemory")
	public String monitorMemory(
			@RequestParam(defaultValue = "5") int du,
			@RequestParam(defaultValue = "70") int threshold,
			@RequestParam(defaultValue = "50") int startThreshold) {
		return startMonitoring("Memory", du, threshold, startThreshold);
	}

	/**
	 * 同时监控CPU和内存使用率
	 * @param du 监控持续时间，单位为分钟
	 * @param threshold 监控阈值，当CPU或内存使用率超过该值时采取行动
	 * @param startThreshold 启动阈值，当CPU或内存使用率低于该值时开始创建字符串
	 * @return 监控的详细信息
	 */
	@GetMapping("/loadAll")
	public String monitorAll(
			@RequestParam(defaultValue = "5") int du,
			@RequestParam(defaultValue = "70") int threshold,
			@RequestParam(defaultValue = "50") int startThreshold) {
		return startMonitoring("All", du, threshold, startThreshold);
	}

	/**
	 * 开始监控系统资源
	 * @param type 监控的系统资源类型（CPU、Memory或All）
	 * @param du 监控持续时间，单位为分钟
	 * @param threshold 监控阈值，当对应资源使用率超过该值时采取行动
	 * @param startThreshold 启动阈值，当对应资源使用率低于该值时开始创建字符串
	 * @return 监控操作的概述信息
	 */
	private String startMonitoring(String type, int du, int threshold, int startThreshold) {
		resetMonitoringState();
		Thread creatorThread = new Thread(this::createStrings);
		Thread monitorThread = new Thread(() -> monitorSystem(type, du, threshold, startThreshold));

		creatorThread.start();
		monitorThread.start();

		return "开始监控" + type + "，持续时间: " + du + " 分钟，阈值: " + threshold + "%，启动阈值: " + startThreshold + "%";
	}

	/**
	 * 重置监控状态
	 */
	private void resetMonitoringState() {
		shouldStop.set(false);
		startCreating.set(false);
		stringList.clear();
	}

	/**
	 * 创建字符串，模拟系统资源消耗
	 */
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

	/**
	 * 监控系统资源（CPU使用率），根据使用率决定是否创建字符串
	 * @param type 监控的资源类型
	 * @param du 监控持续时间，单位为分钟
	 * @param threshold 监控阈值，当CPU使用率超过该值时采取行动
	 * @param startThreshold 启动阈值，当CPU使用率低于该值时开始创建字符串
	 */
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

	/**
	 * 停止监控，清理资源
	 * @param startTime 监控开始时间，用于计算监控持续时间
	 */
	private void stopMonitoring(long startTime) {
		shouldStop.set(true);
		startCreating.set(false);
		clearStringList();
		long endTime = System.currentTimeMillis();
		long duration = endTime - startTime;
		System.out.println("监控结束");
		System.out.println("程序运行时间: " + formatDuration(duration));
	}

	/**
	 * 清空字符串列表，尝试释放内存
	 */
	private void clearStringList() {
		System.out.println("清空字符串列表，释放内存");
		System.out.println("清空前的列表大小: " + stringList.size());
		stringList.clear();
		System.gc();
		System.out.println("清空后的列表大小: " + stringList.size());
	}

	/**
	 * 格式化持续时间
	 * @param durationMillis 持续时间，单位为毫秒
	 * @return 格式化的持续时间字符串
	 */
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
