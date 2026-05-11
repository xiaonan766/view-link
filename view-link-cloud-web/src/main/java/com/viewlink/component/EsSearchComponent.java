package com.viewlink.component;

import com.viewlink.constants.Constants;
import com.viewlink.entity.config.AppConfig;

import com.viewlink.entity.dto.VideoInfoEsDTO;
import com.viewlink.entity.enums.PageSize;
import com.viewlink.entity.enums.SearchOrderTypeEnum;
import com.viewlink.entity.po.UserInfo;
import com.viewlink.entity.po.VideoInfo;
import com.viewlink.entity.query.SimplePage;
import com.viewlink.entity.query.UserInfoQuery;
import com.viewlink.entity.vo.PaginationResultVO;
import com.viewlink.exception.BusinessException;
import com.viewlink.mappers.UserInfoMapper;
import com.viewlink.utils.CopyTools;
import com.viewlink.utils.JsonUtils;
import com.viewlink.utils.StringTools;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.xcontent.XContentType;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component("esSearchComponent")
@Slf4j
public class EsSearchComponent {
    @Resource
    private AppConfig appConfig;
    @Resource
    private RestHighLevelClient restHighLevelClient;

    @Resource
    private UserInfoMapper userInfoMapper;

    /**
     * 判断是否存在索引
     */
    public Boolean isExistIndex() throws IOException {
        //调用esSearchClient中的获取索引请求
        GetIndexRequest getIndexRequest = new GetIndexRequest(appConfig.getEsIndexVideoName());
        return restHighLevelClient.indices().exists(getIndexRequest, RequestOptions.DEFAULT);
    }

    /**
     * 创建索引
     */
    public void createIndex() {
        try {
            if (isExistIndex()) {
                //存在该索引，则直接返回
                return;
            }
            //不存在，则创建索引
            CreateIndexRequest request = new CreateIndexRequest(appConfig.getEsIndexVideoName());
            request.settings("{\"analysis\": {\n" +
                    "      \"analyzer\": {\n" +
                    "        \"comma\": {\n" +
                    "          \"type\": \"pattern\",\n" +
                    "          \"pattern\": \",\"\n" +
                    "          }\n" +
                    "        }\n" +
                    "     } }", XContentType.JSON);
            request.mapping("{\"properties\": {\n" +
                    "      \"videoId\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"index\": false\n" +
                    "      },\n" +
                    "      \"userId\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"index\": false\n" +
                    "      },\n" +
                    "      \"videoCover\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"index\": false\n" +
                    "      },\n" +
                    "      \"videoName\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"analyzer\": \"ik_max_word\"\n" +
                    "      },\n" +
                    "      \"tags\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"analyzer\": \"comma\"\n" +
                    "      },\n" +
                    "      \"playCount\": {\n" +
                    "        \"type\": \"integer\",\n" +
                    "        \"index\": false\n" +
                    "      },\n" +
                    "      \"danmuCount\": {\n" +
                    "        \"type\": \"integer\",\n" +
                    "        \"index\": false\n" +
                    "      },\n" +
                    "      \"collectCount\": {\n" +
                    "        \"type\": \"integer\",\n" +
                    "        \"index\": false\n" +
                    "      },\n" +
                    "      \"createTime\": {\n" +
                    "        \"type\": \"date\",\n" +
                    "        \"format\": \"yyyy-MM-dd HH:mm:ss\",\n" +
                    "        \"index\": false\n" +
                    "      }\n" +
                    "     } }", XContentType.JSON);
            CreateIndexResponse createIndexResponse = restHighLevelClient.indices().create(request, RequestOptions.DEFAULT);
            Boolean acknowledged = createIndexResponse.isAcknowledged();
            if (!acknowledged) {
                throw new BusinessException("初始化es失败");
            }
        } catch (Exception e) {
            log.error("初始化es失败", e);
            throw new BusinessException("初始化es失败");
        }
    }

    /**
     * 保存文档
     */
    public void saveDoc(VideoInfo videoInfo) {
        try {
            //判断是否存在，如果存在则为更新文档操作
            if (docExist(videoInfo.getVideoId())) {
                updateDoc(videoInfo);
            }
            //将传递过来的videoInfo拷贝为dto
            VideoInfoEsDTO videoInfoEsDTO = CopyTools.copy(videoInfo, VideoInfoEsDTO.class);
            //初始化播放数、弹幕数、收藏数
            videoInfoEsDTO.setPlayCount(0);
            videoInfoEsDTO.setDanmuCount(0);
            videoInfoEsDTO.setCollectCount(0);
            //创建添加或更新文档的请求
            IndexRequest request = new IndexRequest(appConfig.getEsIndexVideoName());
            //给请求设置id与源数据
            request.id(videoInfo.getVideoId()).source(JsonUtils.convertObj2Json(videoInfoEsDTO), XContentType.JSON);
            //调用客户端的index方法向Elasticsearch索引中添加或更新一个文档
            restHighLevelClient.index(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            log.error("保存到es失败");
            throw new BusinessException("保存到es失败");
        }
    }

    /**
     * 更新文档
     */
    private void updateDoc(VideoInfo videoInfo) {
        try {
            videoInfo.setLastUpdateTime(null);
            videoInfo.setCreateTime(null);
            //由于更新时只对部分属性进行更新，可以通过反射
            //dataMap用于存储属性名和属性值
            Map<String, Object> dataMap = new HashMap<>();
            //获取videoInfo的所有属性
            Field[] fields = videoInfo.getClass().getDeclaredFields();
            for (Field field : fields) {
                //将属性名的首字母转为大写
                String fieldWithUpperFirstLetter = StringTools.upperCaseFirstLetter(field.getName());
                //拼接即get方法
                String getMethodName = "get" + fieldWithUpperFirstLetter;
                Method method = videoInfo.getClass().getMethod(getMethodName);
                //invoke用于调用该 Method 对象所代表的方法
                Object valueByGet = method.invoke(videoInfo);
                //判断get方法获取的值是否为非空字符串
                if (valueByGet != null && valueByGet instanceof String && !StringTools.isEmpty(valueByGet.toString())
                        //或者非空object
                        || valueByGet != null && !(valueByGet instanceof String)
                ) {
                    //若为非空字符串或者非空object，则添加到dataMap中
                    dataMap.put(field.getName(), valueByGet);
                }
            }
            //判断dataMap中是否有值
            if (dataMap.isEmpty()) {
                return;
            }
            //es更新请求
            UpdateRequest updateRequest = new UpdateRequest(appConfig.getEsIndexVideoName(), videoInfo.getVideoId());
            //数据源
            updateRequest.doc(dataMap);
            //通过es客户端执行更新请求
            restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);
        } catch (Exception e) {
            log.error("es更新视频失败", e);
            throw new BusinessException("es更新视频失败");
        }
    }

    /**
     * 判断es文档是否存在
     */
    private Boolean docExist(String id) throws IOException {
        //创建获取文档的请求
        GetRequest getRequest = new GetRequest(appConfig.getEsIndexVideoName(), id);
        //发送请求并获取相应
        GetResponse response = restHighLevelClient.get(getRequest, RequestOptions.DEFAULT);
        return response.isExists();
    }

    /**
     * 更新es文档count字段数量
     */
    public void updateDocCount(String videoId, String fieldName, Integer count) {
        try {
            UpdateRequest updateRequest = new UpdateRequest(appConfig.getEsIndexVideoName(), videoId);
            Script script = new Script(ScriptType.INLINE,
                    "painless",
                    "ctx._source." + fieldName + "+=params.count",
                    Collections.singletonMap("count", count));
            updateRequest.script(script);
            restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);
        } catch (Exception e) {
            log.error("es更新数量失败", e);
            throw new BusinessException("es更新数量失败");
        }
    }

    /**
     * 删除es文档
     */
    public void delDoc(String videoId) {
        try {
            DeleteRequest deleteRequest = new DeleteRequest(appConfig.getEsIndexVideoName(), videoId);
            restHighLevelClient.delete(deleteRequest, RequestOptions.DEFAULT);
        } catch (Exception e) {
            log.error("es删除文档失败", e);
            throw new BusinessException("es删除文档失败");
        }
    }

    /**
     * 搜索
     */
    public PaginationResultVO<VideoInfo> search(Boolean highlight, String keyword, Integer orderType, Integer pageNo, Integer pageSize) {
        try {
            SearchOrderTypeEnum searchOrderTypeEnum = SearchOrderTypeEnum.getByType(orderType);
            //构建搜索请求体
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            //设置多字段查询，在videoName(视频名称)和tags（标签关键字）字段中搜索keyword，匹配任意字段即可返回结果。
            searchSourceBuilder.query(QueryBuilders.multiMatchQuery(keyword, "videoName", "tags"));
            //判断是否需要高亮显示
            if (highlight) {
                //配置高亮显示
                HighlightBuilder highlightBuilder = new HighlightBuilder();
                //为videoName字段的匹配内容添加HTML标签，便于前端渲染高亮效果。
                highlightBuilder.field("videoName");
                //对span标签的highlight内容进行高亮
                highlightBuilder.preTags("<span class='highlight'>");
                highlightBuilder.postTags("</span>");
                searchSourceBuilder.highlighter(highlightBuilder);
            }
            //默认排序：按相关性评分_score排序
            searchSourceBuilder.sort("_score", SortOrder.DESC);
            //如果指定了orderType，则按照指定orderType排序
            if (orderType != null) {
                searchSourceBuilder.sort(searchOrderTypeEnum.getField(), SortOrder.DESC);
            }
            //默认分页
            pageNo = pageNo == null ? Constants.ONE : pageNo;
            pageSize = pageSize == null ? PageSize.SIZE10.getSize() : pageSize;
            searchSourceBuilder.size(pageSize);
            searchSourceBuilder.from((pageNo - 1) * pageSize);
            SearchRequest searchRequest = new SearchRequest(appConfig.getEsIndexVideoName());
            searchRequest.source(searchSourceBuilder);
            SearchResponse response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            //从响应中提取命中结果和总数
            SearchHits searchHits = response.getHits();
            Integer totalCount = (int) searchHits.getTotalHits().value;
            List<VideoInfo> videoInfoList = new ArrayList<>();
            List<String> userIdList = new ArrayList<>();
            //searchHits包含所有命中搜索词的视频，hit中是每个视频的相关信息
            for (SearchHit hit : searchHits) {
                //SearchHit对象的getSourceAsString()方法返回一个字符串，该字符串是当前命中视频的原始JSON数据
                String videoJsonData = hit.getSourceAsString();
                //将json形式的视频数据转换为videoInfo
                VideoInfo videoInfo = JsonUtils.convertJson2Obj(videoJsonData, VideoInfo.class);
                //获取保存高亮字段信息的Map，键为字段名（videoName或者tags）
                Map<String, HighlightField> highlightFields = hit.getHighlightFields();
                //通过键获取map的值是否为空，来判断视频名称是否高亮
                if (highlightFields.get("videoName") != null) {
                    //视频名称被高亮显示,fragments()返回一个包含高亮片段的数组。这里取数组的第一个元素（通常是整个高亮的视频名称），并调用 string() 方法将其转换为字符串。
                    videoInfo.setVideoName(highlightFields.get("videoName").fragments()[0].string());
                }
                    //把符合搜索词的视频添加到结果集中
                    videoInfoList.add(videoInfo);
                    //把高亮视频的userId添加到userIdList中
                    userIdList.add(videoInfo.getUserId());
                }
                //在数据库中，查询命中搜索词的视频的用户相关信息
                UserInfoQuery userInfoQuery=new UserInfoQuery();
                userInfoQuery.setUserIdList(userIdList);
                List<UserInfo> userInfoList = userInfoMapper.selectList(userInfoQuery);
                //将用户数据搜集到Map中，userId作为键，userInfo作为值，出现重复的键时保留第二个值
                Map<String, UserInfo> userInfoMap = userInfoList.stream()
                        .collect(Collectors.toMap(
                                item -> item.getUserId(), Function.identity(), (data1, data2) -> data2
                        )
                );
                //由于添加时videoInfo和userInfo是一一对应的，即在集合中的索引相同
                videoInfoList.forEach(
                        item-> {
                            UserInfo userInfo = userInfoMap.get(item.getUserId());
                            item.setNickName(userInfo==null?"":userInfo.getNickName());
                        }
                );
                SimplePage simplePage=new SimplePage(pageNo,totalCount,pageSize);
                PaginationResultVO resultVO=new PaginationResultVO(totalCount,simplePage.getPageSize(),simplePage.getPageNo(),videoInfoList);
                return resultVO;
        } catch (Exception e) {
            throw new RuntimeException("查询失败");
        }

    }
}
