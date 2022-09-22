# apijson-framework  [![](https://jitpack.io/v/APIJSON/apijson-framework.svg)](https://jitpack.io/#APIJSON/apijson-framework)
腾讯 [APIJSON](https://github.com/Tencent/APIJSON) 服务端框架，通过数据库表配置角色权限、参数校验等，简化使用。<br />
Tencent [APIJSON](https://github.com/Tencent/APIJSON) Server Framework for configuring access of roles and validation of arguments in database tables,  then using APIJSON easier.

#### Access: https://github.com/Tencent/APIJSON/blob/master/APIJSONORM/src/main/java/apijson/MethodAccess.java
![image](https://user-images.githubusercontent.com/5738175/167259883-e5fff2f4-b3e8-4b2f-a597-d851004c3393.png)

#### Request: https://github.com/Tencent/APIJSON/blob/master/APIJSONORM/src/main/java/apijson/orm/Operation.java
![image](https://user-images.githubusercontent.com/5738175/167259922-f343683f-6335-4778-aaeb-d1b9aed999dc.png)

<br />

## 添加依赖
## Add Dependency

### Maven
#### 1. 在 pom.xml 中添加 JitPack 仓库
#### 1. Add the JitPack repository to pom.xml
```xml
	<repositories>
		<repository>
		    <id>jitpack.io</id>
		    <url>https://jitpack.io</url>
		</repository>
	</repositories>
```

![image](https://user-images.githubusercontent.com/5738175/167261102-12f7f4d6-7895-4d79-a50e-076f93fca6d7.png)

<br />

#### 2. 在 pom.xml 中添加 apijson-framework 依赖
#### 2. Add the apijson-framework dependency to pom.xml
```xml
	<dependency>
	    <groupId>com.github.APIJSON</groupId>
	    <artifactId>apijson-framework</artifactId>
	    <version>LATEST</version>
	</dependency>
```

![image](https://user-images.githubusercontent.com/5738175/167261052-263ee9b4-aae5-4c51-b4d2-6a0446fc4152.png)

<br />

https://github.com/APIJSON/APIJSON-Demo/blob/master/APIJSON-Java-Server/APIJSONDemo/pom.xml

<br />
<br />

### Gradle
#### 1. 在项目根目录 build.gradle 中最后添加 JitPack 仓库
#### 1. Add the JitPack repository in your root build.gradle at the end of repositories
```gradle
	allprojects {
		repositories {
			maven { url 'https://jitpack.io' }
		}
	}
```
<br />

#### 2. 在项目某个 module 目录(例如 `app`) build.gradle 中添加 apijson-orm 依赖
#### 2. Add the apijson-orm dependency in one of your modules(such as `app`)
```gradle
	dependencies {
	        implementation 'com.github.APIJSON:apijson-framework:latest'
	}
```

<br />
<br />
<br />

## 初始化
## Initialization
#### 1.在你项目的主程序启动类 Application 的 static {} 代码块配置 APIJSONApplication.DEFAULT_APIJSON_CREATOR，至少重写 createSQLConfig 方法返回你自己继承 APIJSONSQLConfig 的子类
#### 1.Configure APIJSONApplication.DEFAULT_APIJSON_CREATOR in static {} of your Application, at least override createSQLConfig method and return your SQLConfig extends APIJSONSQLConfig.

```java
	static {
		APIJSONApplication.DEFAULT_APIJSON_CREATOR = new APIJSONCreator<Long>() {
			@Override
			public SQLConfig createSQLConfig() {
				return new DemoSQLConfig();
			}
		};
	}
```

<br />

#### 2.在你项目的主程序启动类 Application 的 main 方法里 SpringApplication.run 后调用 APIJSONApplication.init
#### 2.Call APIJSONApplication.init after SpringApplication.run in main method of your Application

```java
	public static void main(String[] args) throws Exception {
		SpringApplication.run(DemoApplication.class, args);
		APIJSONApplication.init();
	}
```

<br />

#### 见 [apijson.framework](/src/main/java/apijson/framework) 里各个类的注释及 [APIJSONDemo](https://github.com/APIJSON/APIJSON-Demo/blob/master/APIJSON-Java-Server/APIJSONDemo) 里的 [DemoApplication](https://github.com/APIJSON/APIJSON-Demo/blob/master/APIJSON-Java-Server/APIJSONDemo/src/main/java/apijson/demo/DemoApplication.java) <br />

#### See document in [apijson.framework](/src/main/java/apijson/framework) classes and [DemoApplication](https://github.com/APIJSON/APIJSON-Demo/blob/master/APIJSON-Java-Server/APIJSONDemo/src/main/java/apijson/demo/DemoApplication.java) in [APIJSONDemo](https://github.com/APIJSON/APIJSON-Demo/blob/master/APIJSON-Java-Server/APIJSONDemo)

![image](https://user-images.githubusercontent.com/5738175/167260539-27d7e13b-27b9-43ad-925e-3f79c99e8ac9.png)

<br />
<br />
<br />

## 使用
## Usage

#### Access: https://github.com/Tencent/APIJSON/blob/master/APIJSONORM/src/main/java/apijson/MethodAccess.java
![image](https://user-images.githubusercontent.com/5738175/167259883-e5fff2f4-b3e8-4b2f-a597-d851004c3393.png)

![image](https://user-images.githubusercontent.com/5738175/167261523-59abf4ba-e211-49f9-92bd-a79384bb757f.png)

#### Request: https://github.com/Tencent/APIJSON/blob/master/APIJSONORM/src/main/java/apijson/orm/Operation.java
![image](https://user-images.githubusercontent.com/5738175/167259922-f343683f-6335-4778-aaeb-d1b9aed999dc.png)

![image](https://user-images.githubusercontent.com/5738175/167262762-2c2a1c58-e7bf-4352-a7b9-fcbb0fa67f7f.png)

<br />
有问题可以去 Tencent/APIJSON 提 issue <br />
https://github.com/Tencent/APIJSON/issues/36

<br />
<br />

#### 点右上角 ⭐Star 支持一下，谢谢 ^_^
#### Please ⭐Star this project ^_^
https://github.com/APIJSON/apijson-framework
