原文网址
https://blog.csdn.net/ZYC88888/article/details/83059040

字段类型概述一级分类	二级分类	具体类型核心类型	字符串类型	string,text,keyword整数类型	integer,long,short,byte浮点类型	double,float,half_float,scaled_float逻辑类型	boolean日期类型	date范围类型	range二进制类型	binary复合类型	数组类型	array对象类型	object嵌套类型	nested地理类型	地理坐标类型	geo_point地理地图	geo_shape特殊类型	IP类型	ip范围类型	completion令牌计数类型	token_count附件类型	attachment抽取类型	percolator

一、Field datatype(字段数据类型)

1.1string类型

ELasticsearch 5.X之后的字段类型不再支持string，由text或keyword取代。 如果仍使用string，会给出警告。

测试：

PUT my_index{  "mappings": {    "my_type": {      "properties": {        "title": {          "type":  "string"        }      }    }  }}

结果：

#! Deprecation: The [string] field is deprecated, please use [text] or [keyword] instead on [title]{  "acknowledged": true,  "shards_acknowledged": true}

1.2 text类型

text取代了string，当一个字段是要被全文搜索的，比如Email内容、产品描述，应该使用text类型。设置text类型以后，字段内容会被分析，在生成倒排索引以前，字符串会被分析器分成一个一个词项。text类型的字段不用于排序，很少用于聚合（termsAggregation除外）。

把full_name字段设为text类型的Mapping如下：

PUT my_index{  "mappings": {    "my_type": {      "properties": {        "full_name": {          "type":  "text"        }      }    }  }}

1.3 keyword类型

keyword类型适用于索引结构化的字段，比如email地址、主机名、状态码和标签。如果字段需要进行过滤(比如查找已发布博客中status属性为published的文章)、排序、聚合。keyword类型的字段只能通过精确值搜索到。

1.4 数字类型

对于数字类型，ELasticsearch支持以下几种：

类型
			取值范围
		long
			-2^63至2^63-1
		integer
			-2^31至2^31-1
		short
			-32,768至32768
		byte
			-128至127
		double
			64位双精度IEEE 754浮点类型
		float
			32位单精度IEEE 754浮点类型
		half_float
			16位半精度IEEE 754浮点类型
		scaled_float
			缩放类型的的浮点数（比如价格只需要精确到分，price为57.34的字段缩放因子为100，存起来就是5734）
		对于float、half_float和scaled_float,-0.0和+0.0是不同的值，使用term查询查找-0.0不会匹配+0.0，同样range查询中上边界是-0.0不会匹配+0.0，下边界是+0.0不会匹配-0.0。

对于数字类型的数据，选择以上数据类型的注意事项：

在满足需求的情况下，尽可能选择范围小的数据类型。比如，某个字段的取值最大值不会超过100，那么选择byte类型即可。迄今为止吉尼斯记录的人类的年龄的最大值为134岁，对于年龄字段，short足矣。字段的长度越短，索引和搜索的效率越高。
	优先考虑使用带缩放因子的浮点类型。
例子：

PUT my_index{  "mappings": {    "my_type": {      "properties": {        "number_of_bytes": {          "type": "integer"        },        "time_in_seconds": {          "type": "float"        },        "price": {          "type": "scaled_float",          "scaling_factor": 100        }      }    }  }}

1.5 Object类型

JSON天生具有层级关系，文档会包含嵌套的对象：

PUT my_index/my_type/1{   "region": "US",  "manager": {     "age":     30,    "name": {       "first": "John",      "last":  "Smith"    }  }}

上面的文档中，整体是一个JSON，JSON中包含一个manager,manager又包含一个name。最终，文档会被索引成一平的key-value对：

{  "region":             "US",  "manager.age":        30,  "manager.name.first": "John",  "manager.name.last":  "Smith"}

上面文档结构的Mapping如下：

PUT my_index{  "mappings": {    "my_type": {       "properties": {        "region": {          "type": "keyword"        },        "manager": {           "properties": {            "age":  { "type": "integer" },            "name": {               "properties": {                "first": { "type": "text" },                "last":  { "type": "text" }              }            }          }        }      }    }  }}

1.6 date类型

JSON中没有日期类型，所以在ELasticsearch中，日期类型可以是以下几种：

日期格式的字符串：e.g. “2015-01-01” or “2015/01/01 12:10:30”.
	long类型的毫秒数( milliseconds-since-the-epoch)
	integer的秒数(seconds-since-the-epoch)
日期格式可以自定义，如果没有自定义，默认格式如下：

"strict_date_optional_time||epoch_millis"

例子:

PUT my_index{  "mappings": {    "my_type": {      "properties": {        "date": {          "type": "date"         }      }    }  }} PUT my_index/my_type/1{ "date": "2015-01-01" }  PUT my_index/my_type/2{ "date": "2015-01-01T12:10:30Z" }  PUT my_index/my_type/3{ "date": 1420070400001 }  GET my_index/_search{  "sort": { "date": "asc"} }

查看三个日期类型：

{  "took": 0,  "timed_out": false,  "_shards": {    "total": 5,    "successful": 5,    "failed": 0  },  "hits": {    "total": 3,    "max_score": 1,    "hits": [      {        "_index": "my_index",        "_type": "my_type",        "_id": "2",        "_score": 1,        "_source": {          "date": "2015-01-01T12:10:30Z"        }      },      {        "_index": "my_index",        "_type": "my_type",        "_id": "1",        "_score": 1,        "_source": {          "date": "2015-01-01"        }      },      {        "_index": "my_index",        "_type": "my_type",        "_id": "3",        "_score": 1,        "_source": {          "date": 1420070400001        }      }    ]  }}

排序结果：

{  "took": 2,  "timed_out": false,  "_shards": {    "total": 5,    "successful": 5,    "failed": 0  },  "hits": {    "total": 3,    "max_score": null,    "hits": [      {        "_index": "my_index",        "_type": "my_type",        "_id": "1",        "_score": null,        "_source": {          "date": "2015-01-01"        },        "sort": [          1420070400000        ]      },      {        "_index": "my_index",        "_type": "my_type",        "_id": "3",        "_score": null,        "_source": {          "date": 1420070400001        },        "sort": [          1420070400001        ]      },      {        "_index": "my_index",        "_type": "my_type",        "_id": "2",        "_score": null,        "_source": {          "date": "2015-01-01T12:10:30Z"        },        "sort": [          1420114230000        ]      }    ]  }}

1.7 Array类型

ELasticsearch没有专用的数组类型，默认情况下任何字段都可以包含一个或者多个值，但是一个数组中的值要是同一种类型。例如：

字符数组: [ “one”, “two” ]
	整型数组：[1,3]
	嵌套数组：[1,[2,3]],等价于[1,2,3]
	对象数组：[ { “name”: “Mary”, “age”: 12 }, { “name”: “John”, “age”: 10 }]
注意事项：

动态添加数据时，数组的第一个值的类型决定整个数组的类型
	混合数组类型是不支持的，比如：[1,”abc”]
	数组可以包含null值，空数组[ ]会被当做missing field对待。
1.8 binary类型

binary类型接受base64编码的字符串，默认不存储也不可搜索。

PUT my_index{  "mappings": {    "my_type": {      "properties": {        "name": {          "type": "text"        },        "blob": {          "type": "binary"        }      }    }  }} PUT my_index/my_type/1{  "name": "Some binary blob",  "blob": "U29tZSBiaW5hcnkgYmxvYg==" }

搜索blog字段：

GET my_index/_search{  "query": {    "match": {      "blob": "test"     }  }} 返回结果：{  "error": {    "root_cause": [      {        "type": "query_shard_exception",        "reason": "Binary fields do not support searching",        "index_uuid": "fgA7UM5XSS-56JO4F4fYug",        "index": "my_index"      }    ],    "type": "search_phase_execution_exception",    "reason": "all shards failed",    "phase": "query",    "grouped": true,    "failed_shards": [      {        "shard": 0,        "index": "my_index",        "node": "3dQd1RRVTMiKdTckM68nPQ",        "reason": {          "type": "query_shard_exception",          "reason": "Binary fields do not support searching",          "index_uuid": "fgA7UM5XSS-56JO4F4fYug",          "index": "my_index"        }      }    ]  },  "status": 400}

Base64加密、解码工具：http://www1.tc711.com/tool/BASE64.htm

1.9 ip类型

ip类型的字段用于存储IPV4或者IPV6的地址。

PUT my_index{  "mappings": {    "my_type": {      "properties": {        "ip_addr": {          "type": "ip"        }      }    }  }} PUT my_index/my_type/1{  "ip_addr": "192.168.1.1"} GET my_index/_search{  "query": {    "term": {      "ip_addr": "192.168.0.0/16"    }  }}

1.10 range类型

range类型支持以下几种：

类型
			范围
		integer_range
			-2^31至2^31-1
		float_range
			32-bit IEEE 754
		long_range
			-2^63至2^63-1
		double_range
			64-bit IEEE 754
		date_range
			64位整数，毫秒计时
		range类型的使用场景：比如前端的时间选择表单、年龄范围选择表单等。 
例子：

PUT range_index{  "mappings": {    "my_type": {      "properties": {        "expected_attendees": {          "type": "integer_range"        },        "time_frame": {          "type": "date_range",           "format": "yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis"        }      }    }  }} PUT range_index/my_type/1{  "expected_attendees" : {     "gte" : 10,    "lte" : 20  },  "time_frame" : {     "gte" : "2015-10-31 12:00:00",     "lte" : "2015-11-01"  }}

上面代码创建了一个range_index索引，expected_attendees的人数为10到20，时间是2015-10-31 12:00:00至2015-11-01。

查询：

POST range_index/_search{  "query" : {    "range" : {      "time_frame" : {         "gte" : "2015-08-01",        "lte" : "2015-12-01",        "relation" : "within"       }    }  }}

查询结果：

{  "took": 2,  "timed_out": false,  "_shards": {    "total": 5,    "successful": 5,    "failed": 0  },  "hits": {    "total": 1,    "max_score": 1,    "hits": [      {        "_index": "range_index",        "_type": "my_type",        "_id": "1",        "_score": 1,        "_source": {          "expected_attendees": {            "gte": 10,            "lte": 20          },          "time_frame": {            "gte": "2015-10-31 12:00:00",            "lte": "2015-11-01"          }        }      }    ]  }}

1.11 nested类型

nested嵌套类型是object中的一个特例，可以让array类型的Object独立索引和查询。 使用Object类型有时会出现问题，比如文档 my_index/my_type/1的结构如下：

PUT my_index/my_type/1{  "group" : "fans",  "user" : [     {      "first" : "John",      "last" :  "Smith"    },    {      "first" : "Alice",      "last" :  "White"    }  ]}

user字段会被动态添加为Object类型。 
最后会被转换为以下平整的形式：

{  "group" :        "fans",  "user.first" : [ "alice", "john" ],  "user.last" :  [ "smith", "white" ]}

user.first和user.last会被平铺为多值字段，Alice和White之间的关联关系会消失。上面的文档会不正确的匹配以下查询(虽然能搜索到,实际上不存在Alice Smith)：

GET my_index/_search{  "query": {    "bool": {      "must": [        { "match": { "user.first": "Alice" }},        { "match": { "user.last":  "Smith" }}      ]    }  }}

使用nested字段类型解决Object类型的不足：

PUT my_index{  "mappings": {    "my_type": {      "properties": {        "user": {          "type": "nested"         }      }    }  }} PUT my_index/my_type/1{  "group" : "fans",  "user" : [    {      "first" : "John",      "last" :  "Smith"    },    {      "first" : "Alice",      "last" :  "White"    }  ]} GET my_index/_search{  "query": {    "nested": {      "path": "user",      "query": {        "bool": {          "must": [            { "match": { "user.first": "Alice" }},            { "match": { "user.last":  "Smith" }}           ]        }      }    }  }} GET my_index/_search{  "query": {    "nested": {      "path": "user",      "query": {        "bool": {          "must": [            { "match": { "user.first": "Alice" }},            { "match": { "user.last":  "White" }}           ]        }      },      "inner_hits": {         "highlight": {          "fields": {            "user.first": {}          }        }      }    }  }}

1.12token_count类型

token_count用于统计词频：

 PUT my_index{  "mappings": {    "my_type": {      "properties": {        "name": {           "type": "text",          "fields": {            "length": {               "type":     "token_count",              "analyzer": "standard"            }          }        }      }    }  }} PUT my_index/my_type/1{ "name": "John Smith" } PUT my_index/my_type/2{ "name": "Rachel Alice Williams" } GET my_index/_search{  "query": {    "term": {      "name.length": 3     }  }}

1.13 geo point 类型

地理位置信息类型用于存储地理位置信息的经纬度：

PUT my_index{  "mappings": {    "my_type": {      "properties": {        "location": {          "type": "geo_point"        }      }    }  }} PUT my_index/my_type/1{  "text": "Geo-point as an object",  "location": {     "lat": 41.12,    "lon": -71.34  }} PUT my_index/my_type/2{  "text": "Geo-point as a string",  "location": "41.12,-71.34" } PUT my_index/my_type/3{  "text": "Geo-point as a geohash",  "location": "drm3btev3e86" } PUT my_index/my_type/4{  "text": "Geo-point as an array",  "location": [ -71.34, 41.12 ] } GET my_index/_search{  "query": {    "geo_bounding_box": {       "location": {        "top_left": {          "lat": 42,          "lon": -72        },        "bottom_right": {          "lat": 40,          "lon": -74        }      }    }  }}

二、Meta-Fields(元数据)

2.1 _all

_all字段是把其它字段拼接在一起的超级字段，所有的字段用空格分开，_all字段会被解析和索引，但是不存储。当你只想返回包含某个关键字的文档但是不明确地搜某个字段的时候就需要使用_all字段。 
例子：

PUT my_index/blog/1 {  "title":    "Master Java",  "content":     "learn java",  "author": "Tom"}

_all字段包含:[ “Master”, “Java”, “learn”, “Tom” ]

搜索：

GET my_index/_search{  "query": {    "match": {      "_all": "Java"    }  }}

返回结果：

{  "took": 1,  "timed_out": false,  "_shards": {    "total": 5,    "successful": 5,    "failed": 0  },  "hits": {    "total": 1,    "max_score": 0.39063013,    "hits": [      {        "_index": "my_index",        "_type": "blog",        "_id": "1",        "_score": 0.39063013,        "_source": {          "title": "Master Java",          "content": "learn java",          "author": "Tom"        }      }    ]  }}

使用copy_to自定义_all字段：

PUT myindex{  "mappings": {    "mytype": {      "properties": {        "title": {          "type":    "text",          "copy_to": "full_content"         },        "content": {          "type":    "text",          "copy_to": "full_content"         },        "full_content": {          "type":    "text"        }      }    }  }} PUT myindex/mytype/1{  "title": "Master Java",  "content": "learn Java"} GET myindex/_search{  "query": {    "match": {      "full_content": "java"    }  }}

2.2 _field_names

_field_names字段用来存储文档中的所有非空字段的名字，这个字段常用于exists查询。例子如下:

PUT my_index/my_type/1{  "title": "This is a document"} PUT my_index/my_type/2?refresh=true{  "title": "This is another document",  "body": "This document has a body"} GET my_index/_search{  "query": {    "terms": {      "_field_names": [ "body" ]     }  }}

结果会返回第二条文档，因为第一条文档没有title字段。 
同样，可以使用exists查询：

GET my_index/_search{    "query": {        "exists" : { "field" : "body" }    }}

2.3 _id

每条被索引的文档都有一个_type和_id字段，_id可以用于term查询、temrs查询、match查询、query_string查询、simple_query_string查询，但是不能用于聚合、脚本和排序。例子如下：

PUT my_index/my_type/1{  "text": "Document with ID 1"} PUT my_index/my_type/2{  "text": "Document with ID 2"} GET my_index/_search{  "query": {    "terms": {      "_id": [ "1", "2" ]     }  }}

2.4 _index

多索引查询时，有时候只需要在特地索引名上进行查询，_index字段提供了便利，也就是说可以对索引名进行term查询、terms查询、聚合分析、使用脚本和排序。

_index是一个虚拟字段，不会真的加到Lucene索引中，对_index进行term、terms查询(也包括match、query_string、simple_query_string)，但是不支持prefix、wildcard、regexp和fuzzy查询。

举例，2个索引2条文档

 PUT index_1/my_type/1{  "text": "Document in index 1"} PUT index_2/my_type/2{  "text": "Document in index 2"}

对索引名做查询、聚合、排序并使用脚本新增字段：

GET index_1,index_2/_search{  "query": {    "terms": {      "_index": ["index_1", "index_2"]     }  },  "aggs": {    "indices": {      "terms": {        "field": "_index",         "size": 10      }    }  },  "sort": [    {      "_index": {         "order": "asc"      }    }  ],  "script_fields": {    "index_name": {      "script": {        "lang": "painless",        "inline": "doc['_index']"       }    }  }}

2.4 _meta

忽略

2.5 _parent

_parent用于指定同一索引中文档的父子关系。下面例子中现在mapping中指定文档的父子关系，然后索引父文档，索引子文档时指定父id，最后根据子文档查询父文档。

PUT my_index{  "mappings": {    "my_parent": {},    "my_child": {      "_parent": {        "type": "my_parent"       }    }  }}  PUT my_index/my_parent/1 {  "text": "This is a parent document"} PUT my_index/my_child/2?parent=1 {  "text": "This is a child document"} PUT my_index/my_child/3?parent=1&refresh=true {  "text": "This is another child document"}  GET my_index/my_parent/_search{  "query": {    "has_child": {       "type": "my_child",      "query": {        "match": {          "text": "child document"        }      }    }  }}

2.6 _routing

路由参数，ELasticsearch通过以下公式计算文档应该分到哪个分片上：

shard_num = hash(_routing) % num_primary_shards

默认的_routing值是文档的_id或者_parent，通过_routing参数可以设置自定义路由。例如，想把user1发布的博客存储到同一个分片上，索引时指定routing参数，查询时在指定路由上查询：

PUT my_index/my_type/1?routing=user1&refresh=true {  "title": "This is a document"} GET my_index/my_type/1?routing=user1

在查询的时候通过routing参数查询：

GET my_index/_search{  "query": {    "terms": {      "_routing": [ "user1" ]     }  }} GET my_index/_search?routing=user1,user2 {  "query": {    "match": {      "title": "document"    }  }}

在Mapping中指定routing为必须的：

PUT my_index2{  "mappings": {    "my_type": {      "_routing": {        "required": true       }    }  }} PUT my_index2/my_type/1 {  "text": "No routing value provided"}

2.7 _source

存储的文档的原始值。默认_source字段是开启的，也可以关闭：

PUT tweets{  "mappings": {    "tweet": {      "_source": {        "enabled": false      }    }  }}

但是一般情况下不要关闭，除法你不想做一些操作：

使用update、update_by_query、reindex
	使用高亮
	数据备份、改变mapping、升级索引
	通过原始字段debug查询或者聚合
2.8 _type

每条被索引的文档都有一个_type和_id字段，可以根据_type进行查询、聚合、脚本和排序。例子如下：

PUT my_index/type_1/1{  "text": "Document with type 1"} PUT my_index/type_2/2?refresh=true{  "text": "Document with type 2"} GET my_index/_search{  "query": {    "terms": {      "_type": [ "type_1", "type_2" ]     }  },  "aggs": {    "types": {      "terms": {        "field": "_type",         "size": 10      }    }  },  "sort": [    {      "_type": {         "order": "desc"      }    }  ],  "script_fields": {    "type": {      "script": {        "lang": "painless",        "inline": "doc['_type']"       }    }  }}

2.9 _uid

_uid和_type和_index的组合。和_type一样，可用于查询、聚合、脚本和排序。例子如下：

PUT my_index/my_type/1{  "text": "Document with ID 1"} PUT my_index/my_type/2?refresh=true{  "text": "Document with ID 2"} GET my_index/_search{  "query": {    "terms": {      "_uid": [ "my_type#1", "my_type#2" ]     }  },  "aggs": {    "UIDs": {      "terms": {        "field": "_uid",         "size": 10      }    }  },  "sort": [    {      "_uid": {         "order": "desc"      }    }  ],  "script_fields": {    "UID": {      "script": {         "lang": "painless",         "inline": "doc['_uid']"       }    }  }}

三、Mapping参数

3.1 analyzer

指定分词器(分析器更合理)，对索引和查询都有效。如下，指定ik分词的配置：

PUT my_index{  "mappings": {    "my_type": {      "properties": {        "content": {          "type": "text",          "analyzer": "ik_max_word",          "search_analyzer": "ik_max_word"        }      }    }  }}

3.2 normalizer

normalizer用于解析前的标准化配置，比如把所有的字符转化为小写等。例子：

PUT index{  "settings": {    "analysis": {      "normalizer": {        "my_normalizer": {          "type": "custom",          "char_filter": [],          "filter": ["lowercase", "asciifolding"]        }      }    }  },  "mappings": {    "type": {      "properties": {        "foo": {          "type": "keyword",          "normalizer": "my_normalizer"        }      }    }  }} PUT index/type/1{  "foo": "BÀR"} PUT index/type/2{  "foo": "bar"} PUT index/type/3{  "foo": "baz"} POST index/_refresh GET index/_search{  "query": {    "match": {      "foo": "BAR"    }  }}

BÀR经过normalizer过滤以后转换为bar，文档1和文档2会被搜索到。

3.3 boost

boost字段用于设置字段的权重，比如，关键字出现在title字段的权重是出现在content字段中权重的2倍，设置mapping如下，其中content字段的默认权重是1.

PUT my_index{  "mappings": {    "my_type": {      "properties": {        "title": {          "type": "text",          "boost": 2         },        "content": {          "type": "text"        }      }    }  }}

同样，在查询时指定权重也是一样的：

POST _search{    "query": {        "match" : {            "title": {                "query": "quick brown fox",                "boost": 2            }        }    }}

推荐在查询时指定boost，第一中在mapping中写死，如果不重新索引文档，权重无法修改，使用查询可以实现同样的效果。

3.4 coerce

coerce属性用于清除脏数据，coerce的默认值是true。整型数字5有可能会被写成字符串“5”或者浮点数5.0.coerce属性可以用来清除脏数据：

字符串会被强制转换为整数
	浮点数被强制转换为整数
 PUT my_index{  "mappings": {    "my_type": {      "properties": {        "number_one": {          "type": "integer"        },        "number_two": {          "type": "integer",          "coerce": false        }      }    }  }} PUT my_index/my_type/1{  "number_one": "10" } PUT my_index/my_type/2{  "number_two": "10" }

mapping中指定number_one字段是integer类型，虽然插入的数据类型是String，但依然可以插入成功。number_two字段关闭了coerce，因此插入失败。

3.5 copy_to

copy_to属性用于配置自定义的_all字段。换言之，就是多个字段可以合并成一个超级字段。比如，first_name和last_name可以合并为full_name字段。

PUT my_index{  "mappings": {    "my_type": {      "properties": {        "first_name": {          "type": "text",          "copy_to": "full_name"         },        "last_name": {          "type": "text",          "copy_to": "full_name"         },        "full_name": {          "type": "text"        }      }    }  }} PUT my_index/my_type/1{  "first_name": "John",  "last_name": "Smith"} GET my_index/_search{  "query": {    "match": {      "full_name": {         "query": "John Smith",        "operator": "and"      }    }  }}

3.6 doc_values

doc_values是为了加快排序、聚合操作，在建立倒排索引的时候，额外增加一个列式存储映射，是一个空间换时间的做法。默认是开启的，对于确定不需要聚合或者排序的字段可以关闭。

PUT my_index{  "mappings": {    "my_type": {      "properties": {        "status_code": {           "type":       "keyword"        },        "session_id": {           "type":       "keyword",          "doc_values": false        }      }    }  }}

注:text类型不支持doc_values。

3.7 dynamic

dynamic属性用于检测新发现的字段，有三个取值：

true:新发现的字段添加到映射中。（默认）
	flase:新检测的字段被忽略。必须显式添加新字段。
	strict:如果检测到新字段，就会引发异常并拒绝文档。
例子：

PUT my_index{  "mappings": {    "my_type": {      "dynamic": false,       "properties": {        "user": {           "properties": {            "name": {              "type": "text"            },            "social_networks": {               "dynamic": true,              "properties": {}            }          }        }      }    }  }}

PS：取值为strict，非布尔值要加引号。

3.8 enabled

ELasticseaech默认会索引所有的字段，enabled设为false的字段，es会跳过字段内容，该字段只能从_source中获取，但是不可搜。而且字段可以是任意类型。

PUT my_index{  "mappings": {    "session": {      "properties": {        "user_id": {          "type":  "keyword"        },        "last_updated": {          "type": "date"        },        "session_data": {           "enabled": false        }      }    }  }} PUT my_index/session/session_1{  "user_id": "kimchy",  "session_data": {     "arbitrary_object": {      "some_array": [ "foo", "bar", { "baz": 2 } ]    }  },  "last_updated": "2015-12-06T18:20:22"} PUT my_index/session/session_2{  "user_id": "jpountz",  "session_data": "none",   "last_updated": "2015-12-06T18:22:13"}

3.9 fielddata

搜索要解决的问题是“包含查询关键词的文档有哪些？”，聚合恰恰相反，聚合要解决的问题是“文档包含哪些词项”，大多数字段再索引时生成doc_values，但是text字段不支持doc_values。

取而代之，text字段在查询时会生成一个fielddata的数据结构，fielddata在字段首次被聚合、排序、或者使用脚本的时候生成。ELasticsearch通过读取磁盘上的倒排记录表重新生成文档词项关系，最后在Java堆内存中排序。

text字段的fielddata属性默认是关闭的，开启fielddata非常消耗内存。在你开启text字段以前，想清楚为什么要在text类型的字段上做聚合、排序操作。大多数情况下这么做是没有意义的。

“New York”会被分析成“new”和“york”，在text类型上聚合会分成“new”和“york”2个桶，也许你需要的是一个“New York”。这是可以加一个不分析的keyword字段：

PUT my_index{  "mappings": {    "my_type": {      "properties": {        "my_field": {           "type": "text",          "fields": {            "keyword": {               "type": "keyword"            }          }        }      }    }  }}

上面的mapping中实现了通过my_field字段做全文搜索，my_field.keyword做聚合、排序和使用脚本。

3.10 format

format属性主要用于格式化日期：

PUT my_index{  "mappings": {    "my_type": {      "properties": {        "date": {          "type":   "date",          "format": "yyyy-MM-dd"        }      }    }  }}

更多内置的日期格式：https://www.elastic.co/guide/en/elasticsearch/reference/current/mapping-date-format.html

3.11 ignore_above

ignore_above用于指定字段索引和存储的长度最大值，超过最大值的会被忽略：

PUT my_index{  "mappings": {    "my_type": {      "properties": {        "message": {          "type": "keyword",          "ignore_above": 15        }      }    }  }} PUT my_index/my_type/1 {  "message": "Syntax error"} PUT my_index/my_type/2 {  "message": "Syntax error with some long stacktrace"} GET my_index/_search {  "size": 0,   "aggs": {    "messages": {      "terms": {        "field": "message"      }    }  }}

mapping中指定了ignore_above字段的最大长度为15，第一个文档的字段长小于15，因此索引成功，第二个超过15，因此不索引，返回结果只有”Syntax error”,结果如下：

{  "took": 2,  "timed_out": false,  "_shards": {    "total": 5,    "successful": 5,    "failed": 0  },  "hits": {    "total": 2,    "max_score": 0,    "hits": []  },  "aggregations": {    "messages": {      "doc_count_error_upper_bound": 0,      "sum_other_doc_count": 0,      "buckets": []    }  }}

3.12 ignore_malformed

ignore_malformed可以忽略不规则数据，对于login字段，有人可能填写的是date类型，也有人填写的是邮件格式。给一个字段索引不合适的数据类型发生异常，导致整个文档索引失败。如果ignore_malformed参数设为true，异常会被忽略，出异常的字段不会被索引，其它字段正常索引。

PUT my_index{  "mappings": {    "my_type": {      "properties": {        "number_one": {          "type": "integer",          "ignore_malformed": true        },        "number_two": {          "type": "integer"        }      }    }  }} PUT my_index/my_type/1{  "text":       "Some text value",  "number_one": "foo" } PUT my_index/my_type/2{  "text":       "Some text value",  "number_two": "foo" }

上面的例子中number_one接受integer类型，ignore_malformed属性设为true，因此文档一种number_one字段虽然是字符串但依然能写入成功；number_two接受integer类型，默认ignore_malformed属性为false，因此写入失败。

3.13 include_in_all

include_in_all属性用于指定字段是否包含在_all字段里面，默认开启，除索引时index属性为no。 
例子如下，title和content字段包含在_all字段里，date不包含。

PUT my_index{  "mappings": {    "my_type": {      "properties": {        "title": {           "type": "text"        },        "content": {           "type": "text"        },        "date": {           "type": "date",          "include_in_all": false        }      }    }  }}

include_in_all也可用于字段级别，如下my_type下的所有字段都排除在_all字段之外，author.first_name 和author.last_name 包含在in _all中：

PUT my_index{  "mappings": {    "my_type": {      "include_in_all": false,       "properties": {        "title":          { "type": "text" },        "author": {          "include_in_all": true,           "properties": {            "first_name": { "type": "text" },            "last_name":  { "type": "text" }          }        },        "editor": {          "properties": {            "first_name": { "type": "text" },             "last_name":  { "type": "text", "include_in_all": true }           }        }      }    }  }}

3.14 index

index属性指定字段是否索引，不索引也就不可搜索，取值可以为true或者false。

3.15 index_options

index_options控制索引时存储哪些信息到倒排索引中，接受以下配置：

参数
			作用
		docs
			只存储文档编号
		freqs
			存储文档编号和词项频率
		positions
			文档编号、词项频率、词项的位置被存储，偏移位置可用于临近搜索和短语查询
		offsets
			文档编号、词项频率、词项的位置、词项开始和结束的字符位置都被存储，offsets设为true会使用Postings highlighter
		3.16 fields

fields可以让同一文本有多种不同的索引方式，比如一个String类型的字段，可以使用text类型做全文检索，使用keyword类型做聚合和排序。

PUT my_index{  "mappings": {    "my_type": {      "properties": {        "city": {          "type": "text",          "fields": {            "raw": {               "type":  "keyword"            }          }        }      }    }  }} PUT my_index/my_type/1{  "city": "New York"} PUT my_index/my_type/2{  "city": "York"} GET my_index/_search{  "query": {    "match": {      "city": "york"     }  },  "sort": {    "city.raw": "asc"   },  "aggs": {    "Cities": {      "terms": {        "field": "city.raw"       }    }  }}

3.17 norms

norms参数用于标准化文档，以便查询时计算文档的相关性。norms虽然对评分有用，但是会消耗较多的磁盘空间，如果不需要对某个字段进行评分，最好不要开启norms。

3.18 null_value

值为null的字段不索引也不可以搜索，null_value参数可以让值为null的字段显式的可索引、可搜索。例子：

PUT my_index{  "mappings": {    "my_type": {      "properties": {        "status_code": {          "type":       "keyword",          "null_value": "NULL"         }      }    }  }} PUT my_index/my_type/1{  "status_code": null} PUT my_index/my_type/2{  "status_code": [] } GET my_index/_search{  "query": {    "term": {      "status_code": "NULL"     }  }}

文档1可以被搜索到，因为status_code的值为null，文档2不可以被搜索到，因为status_code为空数组，但是不是null。

3.19 position_increment_gap

为了支持近似或者短语查询，text字段被解析的时候会考虑此项的位置信息。举例，一个字段的值为数组类型：

 "names": [ "John Abraham", "Lincoln Smith"]

为了区别第一个字段和第二个字段，Abraham和Lincoln在索引中有一个间距，默认是100。例子如下，这是查询”Abraham Lincoln”是查不到的：

PUT my_index/groups/1{    "names": [ "John Abraham", "Lincoln Smith"]} GET my_index/groups/_search{    "query": {        "match_phrase": {            "names": {                "query": "Abraham Lincoln"             }        }    }}

指定间距大于100可以查询到：

GET my_index/groups/_search{    "query": {        "match_phrase": {            "names": {                "query": "Abraham Lincoln",                "slop": 101             }        }    }}

在mapping中通过position_increment_gap参数指定间距：

PUT my_index{  "mappings": {    "groups": {      "properties": {        "names": {          "type": "text",          "position_increment_gap": 0         }      }    }  }}

3.20 properties

Object或者nested类型，下面还有嵌套类型，可以通过properties参数指定。

PUT my_index{  "mappings": {    "my_type": {       "properties": {        "manager": {           "properties": {            "age":  { "type": "integer" },            "name": { "type": "text"  }          }        },        "employees": {           "type": "nested",          "properties": {            "age":  { "type": "integer" },            "name": { "type": "text"  }          }        }      }    }  }}

对应的文档结构：

PUT my_index/my_type/1 {  "region": "US",  "manager": {    "name": "Alice White",    "age": 30  },  "employees": [    {      "name": "John Smith",      "age": 34    },    {      "name": "Peter Brown",      "age": 26    }  ]}

可以对manager.name、manager.age做搜索、聚合等操作。

GET my_index/_search{  "query": {    "match": {      "manager.name": "Alice White"     }  },  "aggs": {    "Employees": {      "nested": {        "path": "employees"      },      "aggs": {        "Employee Ages": {          "histogram": {            "field": "employees.age",             "interval": 5          }        }      }    }  }}

3.21 search_analyzer

大多数情况下索引和搜索的时候应该指定相同的分析器，确保query解析以后和索引中的词项一致。但是有时候也需要指定不同的分析器，例如使用edge_ngram过滤器实现自动补全。

默认情况下查询会使用analyzer属性指定的分析器，但也可以被search_analyzer覆盖。例子：

PUT my_index{  "settings": {    "analysis": {      "filter": {        "autocomplete_filter": {          "type": "edge_ngram",          "min_gram": 1,          "max_gram": 20        }      },      "analyzer": {        "autocomplete": {           "type": "custom",          "tokenizer": "standard",          "filter": [            "lowercase",            "autocomplete_filter"          ]        }      }    }  },  "mappings": {    "my_type": {      "properties": {        "text": {          "type": "text",          "analyzer": "autocomplete",           "search_analyzer": "standard"         }      }    }  }} PUT my_index/my_type/1{  "text": "Quick Brown Fox" } GET my_index/_search{  "query": {    "match": {      "text": {        "query": "Quick Br",         "operator": "and"      }    }  }}

3.22 similarity

similarity参数用于指定文档评分模型，参数有三个：

BM25 ：ES和Lucene默认的评分模型
	classic ：TF/IDF评分
	boolean：布尔模型评分 
	例子：
PUT my_index{  "mappings": {    "my_type": {      "properties": {        "default_field": {           "type": "text"        },        "classic_field": {          "type": "text",          "similarity": "classic"         },        "boolean_sim_field": {          "type": "text",          "similarity": "boolean"         }      }    }  }}

default_field自动使用BM25评分模型，classic_field使用TF/IDF经典评分模型，boolean_sim_field使用布尔评分模型。

3.23 store

默认情况下，自动是被索引的也可以搜索，但是不存储，这也没关系，因为_source字段里面保存了一份原始文档。在某些情况下，store参数有意义，比如一个文档里面有title、date和超大的content字段，如果只想获取title和date，可以这样：

PUT my_index{  "mappings": {    "my_type": {      "properties": {        "title": {          "type": "text",          "store": true         },        "date": {          "type": "date",          "store": true         },        "content": {          "type": "text"        }      }    }  }} PUT my_index/my_type/1{  "title":   "Some short title",  "date":    "2015-01-01",  "content": "A very long content field..."} GET my_index/_search{  "stored_fields": [ "title", "date" ] }

查询结果：

{  "took": 1,  "timed_out": false,  "_shards": {    "total": 5,    "successful": 5,    "failed": 0  },  "hits": {    "total": 1,    "max_score": 1,    "hits": [      {        "_index": "my_index",        "_type": "my_type",        "_id": "1",        "_score": 1,        "fields": {          "date": [            "2015-01-01T00:00:00.000Z"          ],          "title": [            "Some short title"          ]        }      }    ]  }}

Stored fields返回的总是数组，如果想返回原始字段，还是要从_source中取。

3.24 term_vector

词向量包含了文本被解析以后的以下信息：

词项集合
	词项位置
	词项的起始字符映射到原始文档中的位置。
term_vector参数有以下取值：

参数取值
			含义
		no
			默认值，不存储词向量
		yes
			只存储词项集合
		with_positions
			存储词项和词项位置
		with_offsets
			词项和字符偏移位置
		with_positions_offsets
			存储词项、词项位置、字符偏移位置
		例子：

PUT my_index{  "mappings": {    "my_type": {      "properties": {        "text": {          "type":        "text",          "term_vector": "with_positions_offsets"        }      }    }  }} PUT my_index/my_type/1{  "text": "Quick brown fox"} GET my_index/_search{  "query": {    "match": {      "text": "brown fox"    }  },  "highlight": {    "fields": {      "text": {}     }  }}

四、动态Mapping

4.1 default mapping

在mapping中使用default字段，那么其它字段会自动继承default中的设置。

PUT my_index{  "mappings": {    "_default_": {       "_all": {        "enabled": false      }    },    "user": {},     "blogpost": {       "_all": {        "enabled": true      }    }  }}

上面的mapping中，default中关闭了all字段，user会继承_default中的配置，因此user中的all字段也是关闭的，blogpost中开启_all，覆盖了_default的默认配置。

当default被更新以后，只会对后面新加的文档产生作用。

4.2 Dynamic field mapping

文档中有一个之前没有出现过的字段被添加到ELasticsearch之后，文档的type mapping中会自动添加一个新的字段。这个可以通过dynamic属性去控制，dynamic属性为false会忽略新增的字段、dynamic属性为strict会抛出异常。如果dynamic为true的话，ELasticsearch会自动根据字段的值推测出来类型进而确定mapping：

JSON格式的数据
			自动推测的字段类型
		null
			没有字段被添加
		true or false
			boolean类型
		floating类型数字
			floating类型
		integer
			long类型
		JSON对象
			object类型
		数组
			由数组中第一个非空值决定
		string
			有可能是date类型（开启日期检测)、double或long类型、text类型、keyword类型
		日期检测默认是检测符合以下日期格式的字符串：

[ "strict_date_optional_time","yyyy/MM/dd HH:mm:ss Z||yyyy/MM/dd Z"]

例子:

PUT my_index/my_type/1{  "create_date": "2015/09/02"} GET my_index/_mapping

mapping 如下，可以看到create_date为date类型：

{  "my_index": {    "mappings": {      "my_type": {        "properties": {          "create_date": {            "type": "date",            "format": "yyyy/MM/dd HH:mm:ss||yyyy/MM/dd||epoch_millis"          }        }      }    }  }}

关闭日期检测：

PUT my_index{  "mappings": {    "my_type": {      "date_detection": false    }  }} PUT my_index/my_type/1 {  "create": "2015/09/02"}

再次查看mapping，create字段已不再是date类型：

GET my_index/_mapping返回结果：{  "my_index": {    "mappings": {      "my_type": {        "date_detection": false,        "properties": {          "create": {            "type": "text",            "fields": {              "keyword": {                "type": "keyword",                "ignore_above": 256              }            }          }        }      }    }  }}

自定义日期检测的格式：

PUT my_index{  "mappings": {    "my_type": {      "dynamic_date_formats": ["MM/dd/yyyy"]    }  }} PUT my_index/my_type/1{  "create_date": "09/25/2015"}

开启数字类型自动检测：

PUT my_index{  "mappings": {    "my_type": {      "numeric_detection": true    }  }} PUT my_index/my_type/1{  "my_float":   "1.0",   "my_integer": "1" }

4.3 Dynamic templates

动态模板可以根据字段名称设置mapping，如下对于string类型的字段，设置mapping为：

  "mapping": { "type": "long"}

但是匹配字段名称为long_*格式的，不匹配*_text格式的：

PUT my_index{  "mappings": {    "my_type": {      "dynamic_templates": [        {          "longs_as_strings": {            "match_mapping_type": "string",            "match":   "long_*",            "unmatch": "*_text",            "mapping": {              "type": "long"            }          }        }      ]    }  }} PUT my_index/my_type/1{  "long_num": "5",   "long_text": "foo" }

写入文档以后，long_num字段为long类型，long_text扔为string类型。

4.4 Override default template

可以通过default字段覆盖所有索引的mapping配置，例子：

PUT _template/disable_all_field{  "order": 0,  "template": "*",   "mappings": {    "_default_": {       "_all": {         "enabled": false      }    }  }}
