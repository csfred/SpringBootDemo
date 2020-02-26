GET /student/_search
{
  "query": {
    "match_all": {}
  }
}

GET /student/_search
{
  "query" : { "match" : { "detail_info" : "乒乓球" }},
    "highlight" : {
        "pre_tags" : ["<tag1>", "<tag2>"],
        "post_tags" : ["</tag1>", "</tag2>"],
        "fields" : {
            "detail_info" : {}
        }
    }
}

GET /student/_search
{
  "query" : { "match" : { "detail_info" : "乒乓球" }},
    "highlight" : {
        "pre_tags" : "<span style='color:green'>",
        "post_tags" : "</span>",
        "fields" : {
            "detail_info" : {}
        }
    }
}

DELETE /student
POST /student/_doc
{
  "id": "610111198801151423",
  "name": "李天佑",
  "age": 21,
  "sex": "男",
  "phone": "13159135678",
  "address": "辽宁省沈阳市浑南新区新秀街108号",
  "detail_info": "性格活泼，擅长游泳，乒乓球，爬山等，成绩中等，喜欢结交朋友，乐于助人，曾获得过计算机竞赛三等奖。家中有父母和一个姐姐，中等家庭",
  "create_time": "2020-02-19 14:30:25"
}



PUT /student/
{
  "settings": {
    "analysis": {
      "filter": {
        "edge_ngram_filter": {
          "type": "edge_ngram",
          "min_gram": 1,
          "max_gram": 50
        }
      },
      "char_filter": {
        "tsconvert": {
          "type": "stconvert",
          "convert_type": "t2s"
        }
      },
      "analyzer": {
        "ik_analyzer": {
          "type": "custom",
          "tokenizer": "ik_max_word",
          "char_filter": [
            "tsconvert"
          ]
        },
        "pinyin_analyzer": {
          "tokenizer": "token_pinyin",
          "filter": [
            "edge_ngram_filter",
            "lowercase"
          ]
        }
      },
      "tokenizer": {
        "token_pinyin": {
          "type": "pinyin",
          "keep_separate_first_letter": false,
          "keep_full_pinyin": true,
          "keep_original": true,
          "limit_first_letter_length": 16,
          "lowercase": true,
          "remove_duplicated_term": true
        }
      }
    }
  },
  "mappings": {
    "properties": {
      "id": {
        "type": "keyword"
      },
      "name": {
        "type": "text",
        "fields": {
          "raw": {
            "type": "keyword"
          }
        },
        "analyzer": "pinyin_analyzer",
        "search_analyzer": "pinyin_analyzer"
      },
      "age": {
        "type": "integer"
      },
      "sex": {
        "type": "keyword",
        "ignore_above": 4
      },
      "address": {
        "type": "text",
        "analyzer": "ik_analyzer",
        "search_analyzer": "ik_smart"
      },
      "phone": {
        "type": "keyword",
        "ignore_above": 13
      },
      "detail_info": {
        "type": "text",
        "analyzer": "ik_analyzer",
        "search_analyzer": "ik_smart"
      },
      "created_time": {
        "type": "date",
        "format": "yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis",
        "ignore_malformed": true
      }
    }
  }
}
