在ES6和7以前，可以将index和type类比为关系数据库
的db和table，可以在一个index创建多个type，但是在6以后
不建议这样操作，默认type是_doc，只允许创建一个type
也就是这里的index和 _doc相当于关系数据库的table

Spring boot 2.2.X整合Elasticsearch最新版的一处问题

新版本的Spring boot 2的
spring-boot-starter-data-elasticsearch中支持的
Elasticsearch版本是6.X，
但Elasticsearch实际上已经发展到7.2.X版本了，
为了更好的使用Elasticsearch的新特性，
所以弃用了spring-boot-starter-data-elasticsearch依赖，
而改为直接使用Spring-data-elasticsearch，也就是下面的依赖
<dependency>
    <groupId>org.elasticsearch.client</groupId>
    <artifactId>elasticsearch-rest-high-level-client</artifactId>
    <version>${elasticsearch.version}</version>
</dependency>
<dependency>
    <groupId>org.elasticsearch</groupId>
    <artifactId>elasticsearch</artifactId>
    <version>${elasticsearch.version}</version>
</dependency>
<dependency>
    <groupId>org.elasticsearch.client</groupId>
    <artifactId>elasticsearch-rest-client</artifactId>
    <version>${elasticsearch.version}</version>
</dependency>