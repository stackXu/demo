# OSS 异步客户端库

[TOC]



## 简介

本文介绍基于 vert.x 的异步对象存储OSS提供的相关API接口。

## 要求

1. 使用Java 1.8及以上版本。


## 初始化OSSApiClient

```java
// 初始化OSS配置,需要知道服务器的ip,AccessKey,AccessSecret,bucket
OSSConfig config = new OSSConfig();
config.setEndpoint("10.129.40.2:7080");
config.setBucket("whxjg01");
config.setAccessKeyId("9z3e2PjovUT2ts54");
config.setAccessSecret("g8knLrURFttmbuI1VsJG43x3yCKveW");

// 初始化Vertx
Vertx vertx = Vertx.vertx();

// 初始化client
OSSApiClient client = new OSSApiClient(vertx, config);

// 关闭client
client.close();

```


## 功能

### 上传文件

#### adaptiveUpload自适应上传文件



> 作用:自适应上传文件



```java
OpenOptions options = new OpenOptions().setRead(true).setWrite(false);
String tFile = new File(this.getClass().getResource("/").getPath() + "t.py").getCanonicalPath();
AsyncFile file = fs.openBlocking(tFile, options);
AdaptiveUploadRequest request = new AdaptiveUploadRequest(config.getBucket(), "osstest/t.py", file);
        client.adaptiveUpload(request, resp -> {
            System.out.println("code=" + resp.getCode());
            System.out.println("message=" + resp.getMessage());
            Assert.assertTrue(resp.isSuccess());
            async.complete();
});

```

#### putObject流式上传



> 作用:上传字符串, Byte数组 等:



```java
Buffer buffer = Buffer.buffer();
buffer.appendString("这是上传的内容");
buffer.appendBytes("aaa".getBytes());
PutObjectRequest putObjectRequest = new PutObjectRequest(config.getBucket(), "osstest/1.txt", buffer);
client.putObject(putObjectRequest, resp -> {
    System.out.println("code=" + resp.getCode());
    System.out.println("message=" + resp.getMessage());
    Assert.assertTrue(resp.isSuccess());
    async.complete();
});
```
#### 分片上传



### 下载文件

#### getObject 获取对象
> 作用:GetObject接口用于获取某个文件（Object） 。此操作需要对该Object有读权限。

```java
GetObjectRequest get = new GetObjectRequest(config.getBucket(), "osstest/t.py");
client.getObject(get, resp -> {
    System.out.println("code=" + resp.toString());
    System.out.println("message=" + resp.getMessage());
    Assert.assertTrue(resp.isSuccess());
    async.complete();
});
```


### 删除文件

#### deleteObject删除文件
> 作用:DeleteObject用于删除某个文件（Object）。使用DeleteObject需要对该Object有写权限。

```java
DeleteObjectRequest request = new DeleteObjectRequest(config.getBucket(), "osstest/t.py");
client.deleteObject(request, resp -> {
        System.out.println(resp.getCode());
        System.out.println(resp.getMessage());
        Assert.assertTrue(resp.isSuccess());
        async.complete();
});
        
```

###  复制文件
#### copyObject



>  作用: CopyObject接口用于在存储空间（Bucket ）内或同地域的Bucket之间拷贝文件（Object）。使用CopyObject接口发送一个Put请求给OSS，OSS会自动判断为拷贝操作，并直接在服务器端执行该操作。 
>



### 请求返回加密失败



### 查看修改文件


#### getBucket
> 作用:GetBucket接口用于列举存储空间（Bucket）中所有文件（Object）的信息。


```java
// Bucket , 以匹配的前缀 , 是否通过/进行分组 ,指定返回Object的最大数。默认100 , 以某个字母排序开始返回
GetBucketRequest getBucketRequest = new GetBucketRequest(config.getBucket(), "osstest" ,true ,100,"5");
        client.getBucket(getBucketRequest, resp -> {
            System.out.println(resp.getCode());
            System.out.println(resp.getData());
            Assert.assertTrue(resp.isSuccess());
            async.complete();
});
```

#### headObject
> 作用:HeadObject接口用于获取某个文件（Object）的元信息。使用此接口不会返回文件内容。

请求头:

| 名称                    | 类型   | 是否必选 | 描述                                                         |
| :---------------------- | :----- | :------- | :----------------------------------------------------------- |
| **If-Modified-Since**   | 字符串 | 否       | 如果传入参数中的时间早于实际修改时间，则返回200 OK和Object Meta；否则返回304 not modified。默认值：无 |
| **If-Unmodified-Since** | 字符串 | 否       | 如果传入参数中的时间等于或者晚于文件实际修改时间，则返回200 OK和Object Meta；否则返回412 precondition failed。默认值：无 |
| **If-Match**            | 字符串 | 否       | 如果传入期望的ETag和Object的 ETag匹配，则返回200 OK和Object Meta；否则返回412 precondition failed。默认值：无 |
| **If-None-Match**       | 字符串 | 否       | 如果传入期望的ETag值和Object的ETag不匹配，则返回200 OK和Object Meta；否则返回304 Not Modified。默认值：无 |

响应头:

| 名称   | 类型   | 描述                                                         |
| :----- | :----- | :----------------------------------------------------------- |
| eTag   | 字符串 | 对于PutObject请求创建的Object，ETag值是其内容的MD5值；对于其他方式创建的Object，ETag值是其内容的UUID。ETag值可以用于检查Object内容是否发生变化。不建议您使用ETag来作为Object内容的MD5校验数据完整性。 |
| date   | 字符串 | 上传时间                                                     |
| server | 字符串 | 服务                                                         |




```java
HeadObjectRequest headObjectRequest = new HeadObjectRequest(config.getBucket(), "osstest/t.py");
client.headObject(headObjectRequest, resp -> {
    System.out.println("code=" + resp.toString());
    System.out.println("head=" + resp.getHeaders().toString());
    Assert.assertTrue(resp.isSuccess());
    async.complete();
});

```

#### putObjectAcl
>  作用: PutObjectACL接口用于修改文件（Object）的访问权限（ACL）。此操作只有Bucket Owner有权限执行，且需对Object有读写权限。 
> 
```java
PutObjectAclRequest putObjectAclRequest = new PutObjectAclRequest(config.getBucket(), "osstest/1.txt", ObjectAcl.PRIVATE);
client.putObjectAcl(putObjectAclRequest, resp -> {
    System.out.println("code=" + resp.getCode());
    System.out.println("message=" + resp.getMessage());
    Assert.assertTrue(resp.isSuccess());
    async.complete();
});
```


## createChunkedStream









