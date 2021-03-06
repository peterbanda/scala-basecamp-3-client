# Scala Basecamp 3 Client

The service currently supports 6 calls
- *projects* (accountId: Int) - with pagination
- *people* (accountId: Int) - with pagination
- *uploads* (accountId: Int, bucket: Long, vault: Long) - with pagination
- *vaults* (accountId: Int, bucket: Long, vault: Long) - with pagination
- *downloadFile* (accountId: Int, bucket: Long, upload: Long, fileName: String)
- *downloadFileStreamed* (accountId: Int, bucket: Long, upload: Long, fileName: String)

## Installation

All you need is **Scala 2.11**. To pull the library you have to add the following dependency to *build.sbt*

```
"com.bnd-lib" %% "scala-basecamp-3-client" % "0.0.3"
```

or to *pom.xml* (if you use maven)

```
<dependency>
    <groupId>com.bnd-lib</groupId>
    <artifactId>scala-basecamp-3-client_2.11</artifactId>
    <version>0.0.3</version>
</dependency>
```