# apijson-framework  [![](https://jitpack.io/v/APIJSON/apijson-framework.svg)](https://jitpack.io/#APIJSON/apijson-framework)
腾讯 [APIJSON](https://github.com/APIJSON/APIJSON) 服务端框架，可通过 Maven, Gradle 等远程依赖。<br />
Tencent [APIJSON](https://github.com/APIJSON/APIJSON) Server Framework for remote dependencies with Maven, Gradle, etc.

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
<br />

#### 2. 在 pom.xml 中添加 apijson-orm 依赖
#### 2. Add the apijson-orm dependency to pom.xml
```xml
	<dependency>
	    <groupId>com.github.APIJSON</groupId>
	    <artifactId>apijson-framework</artifactId>
	    <version>LATEST</version>
	</dependency>
```

<br />
<br />
<br />

### Gradle
#### 1. 在项目根目录 build.gradle 中最后添加 JitPack 仓库
#### 1. Add the JitPack repository in your root build.gradle at the end of repositories
```gradle
	allprojects {
		repositories {
			...
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
