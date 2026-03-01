package com.oj.cs.model.dto.post;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Field;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.oj.cs.model.entity.Post;

import lombok.Data;

/**
 * 帖子 ES 包装类
 *
 * <p>Spring Data Elasticsearch 5.x 注解说明： - @Document: indexName 属性保持不变 - @Field: type 属性已废弃，由
 * Elasticsearch 自动检测; format 改为 dateFormatter
 */
@org.springframework.data.elasticsearch.annotations.Document(indexName = "post")
@Data
public class PostEsDTO implements Serializable {

  private static final String DATE_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

  /** id */
  @Id private Long id;

  /** 标题 */
  private String title;

  /** 内容 */
  private String content;

  /** 标签列表 */
  private List<String> tags;

  /** 点赞数 */
  private Integer thumbNum;

  /** 收藏数 */
  private Integer favourNum;

  /** 创建用户 id */
  private Long userId;

  /** 创建时间 */
  @Field(index = false, store = true)
  private Date createTime;

  /** 更新时间 */
  @Field(index = false, store = true)
  private Date updateTime;

  /** 是否删除 */
  private Integer isDelete;

  private static final long serialVersionUID = 1L;

  private static final Gson GSON = new Gson();

  /**
   * 对象转包装类
   *
   * @param post
   * @return
   */
  public static PostEsDTO objToDto(Post post) {
    if (post == null) {
      return null;
    }
    PostEsDTO postEsDTO = new PostEsDTO();
    BeanUtils.copyProperties(post, postEsDTO);
    String tagsStr = post.getTags();
    if (StringUtils.isNotBlank(tagsStr)) {
      postEsDTO.setTags(GSON.fromJson(tagsStr, new TypeToken<List<String>>() {}.getType()));
    }
    return postEsDTO;
  }

  /**
   * 包装类转对象
   *
   * @param postEsDTO
   * @return
   */
  public static Post dtoToObj(PostEsDTO postEsDTO) {
    if (postEsDTO == null) {
      return null;
    }
    Post post = new Post();
    BeanUtils.copyProperties(postEsDTO, post);
    List<String> tagList = postEsDTO.getTags();
    if (CollectionUtils.isNotEmpty(tagList)) {
      post.setTags(GSON.toJson(tagList));
    }
    return post;
  }
}
