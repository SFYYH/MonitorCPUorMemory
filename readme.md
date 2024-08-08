### 1. `/loadCPU`

该接口用于监控CPU的使用率，并在CPU使用率低于`START_THRESHOLD`时开始创建字符串，在使用率超过`threshold`时停止创建。

#### 请求参数：

- `du` (可选): 监控持续时间，单位为分钟。默认为 `5` 分钟。
- `threshold` (可选): 停止创建字符串的CPU使用率阈值，默认为 `70%`。
- `startThreshold` (可选): 开始创建字符串的CPU使用率阈值，默认为 `50%`。

#### 示例：

```bash
curl "http://localhost:8080/loadCPU?du=10&threshold=75&startThreshold=40"
```

此请求将启动对CPU使用率的监控，持续时间为 `10` 分钟。当CPU使用率低于 `40%` 时开始创建字符串，当使用率超过 `75%` 时停止创建。

---

### 2. `/loadMemory`

该接口用于监控内存的使用情况，并在CPU使用率低于`START_THRESHOLD`时开始创建字符串，在使用率超过`threshold`时停止创建。

#### 请求参数：

- `du` (可选): 监控持续时间，单位为分钟。默认为 `5` 分钟。
- `threshold` (可选): 停止创建字符串的内存使用率阈值，默认为 `70%`。
- `startThreshold` (可选): 开始创建字符串的内存使用率阈值，默认为 `50%`。

#### 示例：

```bash
curl "http://localhost:8080/loadMemory?du=10&threshold=75&startThreshold=40"
```

此请求将启动对内存使用情况的监控，持续时间为 `10` 分钟。当内存使用率低于 `40%` 时开始创建字符串，当使用率超过 `75%` 时停止创建。

---

### 3. `/loadAll`

该接口用于同时监控CPU和内存的使用情况，并在CPU使用率低于`START_THRESHOLD`时开始创建字符串，在使用率超过`threshold`时停止创建。

#### 请求参数：

- `du` (可选): 监控持续时间，单位为分钟。默认为 `5` 分钟。
- `threshold` (可选): 停止创建字符串的CPU或内存使用率阈值，默认为 `70%`。
- `startThreshold` (可选): 开始创建字符串的CPU或内存使用率阈值，默认为 `50%`。

#### 示例：

```bash
curl "http://localhost:8080/loadAll?du=15&threshold=80&startThreshold=45"
```

此请求将启动对CPU和内存使用情况的监控，持续时间为 `15` 分钟。当CPU或内存使用率低于 `45%` 时开始创建字符串，当使用率超过 `80%` 时停止创建。

---

以上是如何使用这些接口的示例和说明。你可以根据实际情况调整参数，来适应不同的监控需求。