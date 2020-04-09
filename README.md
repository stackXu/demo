


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
#### initUpload分片上传

> 作用: 初始化一个Upload之后，可以根据指定的Object名和Upload ID来分块（Part）上传数据 :

-  调用该接口上传Part数据前，必须先调用InitUpload接口来获取一个OSS服务器颁发的Upload ID。Upload ID用于唯一标识上传的part属于哪个Object。 
-  每一个上传的Part都有一个标识它的号码（part number，范围是1-10000），单个Part大小范围100KB-5GB。MultipartUpload要求除最后一个Part以外，其他的Part大小都要大于100KB。因不确定是否为最后一个Part，UploadPart接口并不会立即校验上传Part的大小，只有当CompleteMultipartUpload的时候才会校验。 
-  如果你用同一个part number上传了新的数据，那么OSS上已有的这个号码的Part数据将被覆盖。 
-  OSS会将服务器端收到Part数据的MD5值放在ETag头内返回给用户。 
-  若调用InitiateMultipartUpload接口时，指定了x-oss-server-side-encryption请求头，则会对上传的Part进行加密编码，并在UploadPart响应头中返回x-oss-server-side-encryption头，其值表明该Part的服务器端加密算法 



代码步骤:

1. 初始化OSSApiClient
2. 初始化Upload获取uploadId

```java
InitiateUploadRequest initiateUploadRequest = new InitiateUploadRequest(config.getBucket(), "osstestinitUploadApload.txt");
client.initUpload(initiateUploadRequest, resp -> {
            System.out.println("code=" + resp.getCode());
            System.out.println("message=" + resp.getMessage());
            InitiateUploadResult data = resp.getData().getData();
            String uploadId = data.getUploadId();
            System.out.println("uploadId:   " + uploadId);
            abortUpload(context, uploadId);
});
```

3. 将大文件进行分片,看情况使用遍历上传或者循环上传,     每次上传分片之后，OSS的返回结果会包含一个PartETag。PartETag将被保存到partETags中。


```java
	//完成上传需要记录所有Part信息
   ArrayList<Part> lists = new ArrayList<>();
        Buffer buffer = Buffer.buffer();
        buffer.appendString("这是上传的内容");
        buffer.appendBytes("aaa".getBytes());
        UploadPartRequest uploadPartRequest = new UploadPartRequest(config.getBucket(), "osstestinitUpload.txt", buffer, 1, uploadId);
client.continueUpload(uploadPartRequest, resp -> {
            System.out.println("code=" + resp.getCode());
            System.out.println("message=" + resp.getMessage());
            System.out.println("1etag:" + resp.getHeaders().getETag());
            Assert.assertTrue(resp.isSuccess());
            lists.add(new Part(1, resp.getHeaders().getETag()));
            //完成上传
            //completeUpload(context, lists, uploadId);
});

```


4. 在执行完成分片上传操作时，需要提供所有有效的partETags。OSS收到提交的partETags后，会逐一验证每个分片的有效性。当所有的数据分片验证通过后，OSS将把这些分片组合成一个完整的文件。


```java
 public void completeUpload(TestContext context, ArrayList<Part> lists, String uploadId) {
        Async async = context.async();
        OSSConfig config = getConfig();
        OSSApiClient client = new OSSApiClient(vertx, config);
        CompleteUploadRequest completeUploadRequest = new CompleteUploadRequest(config.getBucket(), "osstestinitUpload.txt", uploadId, lists);
        client.completeUpload(completeUploadRequest, resp -> {
            System.out.println("code=" + resp.getCode());
            System.out.println("message=" + resp.getMessage());
            System.out.println("访问地址:  " + resp.getData().getLocation());
            Assert.assertTrue(resp.isSuccess());
            async.complete();
	});
}
```
5. 也可以进行终止上传,需要记录初始化Upload的uploadId进行终止
>说明: 测试用例终止上传请求返回403没有权限,猜测是oss权限问题
```java
@Test
public void abortUploadId(TestContext context) {
        Async async = context.async();
        OSSConfig config = getConfig();
        OSSApiClient client = new OSSApiClient(vertx, config);
        AbortUploadRequest abortUploadRequest = new AbortUploadRequest(config.getBucket(), "osstestinitUploadApload.txt", "BFC7828772074660BB163751F7F71857");
        client.abortUpload(abortUploadRequest, resp -> {
            System.out.println("code=" + resp.getCode());
            System.out.println("message=" + resp.getMessage());
            Assert.assertTrue(resp.isSuccess());
            async.complete();
        });
}
```

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
#### copyObject复制Object对象

>  作用: CopyObject接口用于在存储空间（Bucket ）内或同地域的Bucket之间拷贝文件（Object）。使用CopyObject接口发送一个Put请求给OSS，OSS会自动判断为拷贝操作，并直接在服务器端执行该操作。 
>说明: 测试用例请求返回加密失败,猜测是oss权限问题

```java
  @Test
    public void copyObject(TestContext context) {
        Async async = context.async();
        OSSConfig config = getConfig();
        OSSApiClient client = new OSSApiClient(vertx, config);
//        CopyObjectRequest copyObjectRequest = new CopyObjectRequest(config.getBucket(), "osstest/1.txt");
        CopyObjectRequest copyObjectRequest = new CopyObjectRequest(config.getBucket(), "osstestinitUpload.txt");

        //指定CopyObject操作时是否覆盖同名目标Object。不指定x-oss-forbid-overwrite时，默认覆盖同名目标Object。
        //指定x-oss-forbid-overwrite为true时，表示禁止覆盖同名Object；指定x-oss-forbid-overwrite为false时，表示允许覆盖同名目标Object。
        copyObjectRequest.withForbidOverwrite(false);

        //指定拷贝的源地址。默认值：无
        copyObjectRequest.withSource("/whxjg01/osstest/1.txt");

        //如果源Object的ETag值和您提供的ETag相等，则执行拷贝操作，并返回200 OK；否则返回412 Precondition Failed错误码（预处理失败）。
        //默认值：无
//        copyObjectRequest.withIfMatch("");
//
//        //如果源Object的ETag值和您提供的ETag不相等，则执行拷贝操作，并返回200 OK；否则返回304 Not Modified错误码（预处理失败）。
//        //默认值：无
//        copyObjectRequest.withIfNoneMatch("");

        //COPY （默认值）
        //复制源Object的元数据到目标Object。
        //说明 不会复制源Object的x-oss-server-side-encryption。目标Object是否进行服务器端加密编码只根据当前拷贝操作是否指定了x-oss-server-side-encryption来决定。
        //REPLACE
        //忽略源Object的元数据，直接采用请求中指定的元数据。
//        copyObjectRequest.withMetadataDirective(Directive.REPLACE);
//        copyObjectRequest.withStorageClass(StorageClass.ARCHIVE);
//        copyObjectRequest.withTaggingDirective(Directive.REPLACE);

//        copyObjectRequest.withContentLength(3);
        client.copyObject(copyObjectRequest, resp -> {
            System.out.println(resp.getCode());
            System.out.println(resp.getMessage());
            System.out.println("请求:" + resp.isSuccess());
            async.complete();
        });

    }

```




### 查看修改文件


#### getBucket查看Bucket文件信息
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

#### headObject查看Object元信息
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

#### putObjectAcl修改Object的权限
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

