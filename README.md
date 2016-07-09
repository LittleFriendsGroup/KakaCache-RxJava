## 咔咔缓存（KakaCache）
> 咔咔一声，缓存搞定。这是一个专用于解决Android中网络请求及图片加载的缓存处理框架

## 如何使用

### 准备Retrofit
```java
retrofit = new Retrofit.Builder()
    .baseUrl("https://api.github.com/")
    .addConverterFactory(KakaCache.gsonConverter())
    .addCallAdapterFactory(KakaCache.rxCallAdapter())
    .build();
```

### 定义接口
```java
@GET("users/{user}/repos")
@CACHE(value = "custom_key_listRepos", strategy = CacheAndRemoteStrategy.class)
rx.Observable<ResultData<List<GithubRepoEntity>>> listReposForKaka(@Path("user") String user);
```

### 调用接口
```java
service.listReposForKaka("alafighting")
    .subscribeOn(Schedulers.io())
    .observeOn(AndroidSchedulers.mainThread())
    .subscribe(data -> {
        LogUtils.log("listReposForKaka => "+data);
    }, error -> {
        LogUtils.log(error);
    });
```

### or 太麻烦？给你`一步到位`！！

在原有代码的基础上，仅需一行代码搞定
```java
.compose(KakaCache.transformer(KEY_CACHE, new FirstCacheStrategy()))
```

在这里声明缓存策略即可，不影响原有代码结构


## 支持特性

#### 缓存层级 - 更优良可靠的缓存
- Internet临时缓存
- 磁盘缓存
- 内存缓存

#### 缓存策略 - 尽可能适应多种使用场景
- 仅缓存
- 仅网络
- 优先缓存
- 优先网络
- 先缓存后网络

#### 缓存置换算法 - 多种实现，按需选择
- 先进先出算法（FIFO）：最先进入的内容作为替换对象
- 最近最少使用算法（LFU）：最近最少使用的内容作为替换对象
- 最久未使用算法（LRU）：最久没有访问的内容作为替换对象
- 非最近使用算法（NMRU）：在最近没有使用的内容中随机选择一个作为替换对象
- 其他算法，包括变种算法和组合算法

#### 存储策略 - 支持不同数据的缓存需求
- 不存储
- 仅内存
- 仅磁盘
- 内存+磁盘

#### 线程管理 - 异步执行
- 支持多线程操作
- 支持异步执行，UI线程回调

#### 自动清理 - 自动检查
- 缓存过期后，自动清理
- 存储空间不足时，清理超出数据
- 存储个数超量时，清理超出数据

#### 配置项 - 约定大于配置
- 策略
- 存储空间大小
- 存储个数
- 有效期
- 是否启用缓存
- 置换算法
- 线程池大小
- 缓存实现
- 任务优先级

## **项目分层结构**
```
common >> core >> manager >> netcache\imagecache
公用类 >> 存储核心 >> 缓存管理 >> 应用缓存
```

- **common**        通用代码，一般为通用工具类或通用基类，也包含丰富语言特性的基础代码等
- **core**          数据存储，负责数据的读取和写入，不关心线程等
- **manager**       缓存管理，包括但不限于线程等的管理
- **netcache**      网络缓存，针对网络请求的特点，优化缓存功能，重点在于数据同步问题
- **imagecache**    图片缓存，因图片的同步要求不那么苛刻，可以适当的放宽缓存条件

## 关于

- 这是一个正在成长中的开源项目…
- 参与项目开发，欢迎入群：574171290