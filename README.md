# MyShortlink

MyShortlink 是一个基于 Spring Boot、Spring Cloud 和 Vue 3 的 SaaS 短链接系统，包含短链接创建、跳转、分组管理、访问统计、回收站、用户登录和网关鉴权等核心能力。项目采用前后端分离和微服务拆分，适合作为 Java 后端方向的实战项目展示。

## 项目亮点

- 短链接生成与跳转：支持长链接转短链接，并通过短码完成重定向访问。
- 分组与回收站：支持短链接按分组管理、删除、恢复和彻底移除。
- 访问统计：统计 PV、UV、UIP、浏览器、操作系统、设备、网络类型和地区等访问数据。
- 高并发设计：结合 Redis、布隆过滤器、分库分表和消息队列思路提升查询与写入能力。
- 网关鉴权：通过 Spring Cloud Gateway 统一路由请求并校验登录令牌。
- 风控限流：支持用户维度的访问频控，降低恶意请求对系统的影响。
- 前端控制台：基于 Vue 3、Element Plus 和 ECharts 实现短链接管理与数据可视化。

## 技术栈

| 模块 | 技术 |
| --- | --- |
| 后端 | Java 17, Spring Boot 3, Spring Cloud, MyBatis-Plus |
| 网关 | Spring Cloud Gateway |
| 数据库 | MySQL, Apache ShardingSphere |
| 缓存 | Redis, Redisson |
| 前端 | Vue 3, Vite, Element Plus, ECharts, Axios |
| 工程 | Maven, Nacos |

## 模块说明

```text
MyShortlink
├── admin        # 用户、分组、后台管理接口
├── Project      # 短链接核心业务、跳转与访问统计
├── gateway      # API 网关与 Token 校验
└── console-vue  # 前端控制台
```

## 本地运行

### 环境要求

- JDK 17+
- Maven 3.8+
- Node.js 16+
- MySQL 8+
- Redis
- Nacos

### 后端配置

默认配置已经移除个人本地地址和密码。运行前请根据自己的环境调整以下配置文件：

- `admin/src/main/resources/application.yaml`
- `Project/src/main/resources/application.yaml`
- `gateway/src/main/resources/application.yaml`
- `admin/src/main/resources/shardingsphere-config-dev.yaml`
- `Project/src/main/resources/shardingsphere-config-dev.yaml`

常用环境变量：

```bash
REDIS_HOST=127.0.0.1
REDIS_PORT=6379
REDIS_PASSWORD=
NACOS_SERVER_ADDR=127.0.0.1:8848
AMAP_KEY=
```

ShardingSphere 配置文件中的 MySQL 地址、用户名和密码为示例值，实际运行时请改成自己的数据库配置。

### 启动后端

```bash
mvn clean package -DskipTests
```

分别启动以下 Spring Boot 应用：

- `com.nageoffer.shortlink.admin.ShortLinkAdminApplication`
- `com.nageoffer.shortlink.project.ShortLinkApplication`
- `com.nageoffer.shortlink.gateway.GatewayServiceApplication`

### 启动前端

```bash
cd console-vue
npm install
npm run dev
```

## 接口端口

| 服务 | 默认端口 |
| --- | --- |
| gateway | 8000 |
| project | 8001 |
| admin | 8002 |

## 简历描述参考

SaaS 短链接系统：基于 Spring Boot 3、Spring Cloud Gateway、Redis、MySQL、ShardingSphere 和 Vue 3 实现短链接创建、跳转、分组管理、回收站、访问统计和网关鉴权。通过 Redis 缓存、布隆过滤器、分库分表和异步统计等设计提升系统在高并发访问场景下的性能与可扩展性。

## License

本项目仅用于学习和个人项目展示。
