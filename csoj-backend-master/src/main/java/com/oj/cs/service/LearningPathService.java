package com.oj.cs.service;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.oj.cs.model.enums.KnowledgePointEnum;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/** 学习路径服务 为学生推荐个性化的算法学习路径 */
@Slf4j
@Service
public class LearningPathService {

  /** 获取完整的学习路径（按知识点依赖关系排序） */
  public List<PathNode> getCompleteLearningPath() {
    List<PathNode> path = new ArrayList<>();

    // 按难度和依赖关系构建学习路径
    for (KnowledgePointEnum kp : KnowledgePointEnum.getAllSorted()) {
      PathNode node = new PathNode();
      node.setCode(kp.getCode());
      node.setName(kp.getName());
      node.setDifficulty(kp.getDifficulty());
      node.setCategory(kp.getCategory().getDescription());

      // 添加前置知识点
      if (kp.hasParent()) {
        KnowledgePointEnum parent = kp.getParent();
        if (parent != null) {
          node.setPrerequisites(Collections.singletonList(parent.getCode()));
        }
      }

      // 推荐题目数量（难度越高，推荐题目越多）
      int recommendCount = kp.getDifficulty() * 3 + 2;
      node.setRecommendedQuestions(recommendCount);

      path.add(node);
    }

    return path;
  }

  /**
   * 根据用户水平获取学习路径
   *
   * @param userLevel 用户水平（1-5）
   * @return 学习路径
   */
  public List<PathNode> getLearningPathByLevel(int userLevel) {
    // Ensure level is in valid range
    int adjustedLevel = Math.max(1, Math.min(5, userLevel));

    return getCompleteLearningPath().stream()
        .filter(node -> node.getDifficulty() <= adjustedLevel + 1)
        .collect(Collectors.toList());
  }

  /**
   * 根据目标获取学习路径
   *
   * @param targetCode 目标知识点代码
   * @return 到达目标所需的学习路径
   */
  public List<PathNode> getPathToTarget(String targetCode) {
    KnowledgePointEnum target = KnowledgePointEnum.fromCode(targetCode);
    if (target == null) {
      return new ArrayList<>();
    }

    List<PathNode> path = new ArrayList<>();

    // 递归获取所有前置知识点
    Set<KnowledgePointEnum> prerequisites = new HashSet<>();
    collectPrerequisites(target, prerequisites);

    // 转换为路径节点
    for (KnowledgePointEnum kp : prerequisites) {
      PathNode node = convertToPathNode(kp);
      path.add(node);
    }

    // 添加目标知识点
    path.add(convertToPathNode(target));

    // 按难度排序
    path.sort(Comparator.comparingInt(PathNode::getDifficulty));

    return path;
  }

  /** 获取新手入门路径 */
  public List<PathNode> getBeginnerPath() {
    return getCompleteLearningPath().stream()
        .filter(node -> node.getDifficulty() <= 2)
        .limit(10)
        .collect(Collectors.toList());
  }

  /** 获取面试突击路径 */
  public List<PathNode> getInterviewPath() {
    // 重点: 数组、链表、树、DP、哈希表
    String[] focus = {
      "array", "linkedlist", "tree", "dp", "hashtable", "binarysearch", "twopointers"
    };

    return getCompleteLearningPath().stream()
        .filter(node -> Arrays.stream(focus).anyMatch(f -> node.getCode().contains(f)))
        .collect(Collectors.toList());
  }

  /** 获取知识点统计信息 */
  public KnowledgeStats getKnowledgeStats() {
    KnowledgeStats stats = new KnowledgeStats();

    int total = KnowledgePointEnum.values().length;
    stats.setTotalCount(total);

    Map<String, Long> categoryCount =
        Arrays.stream(KnowledgePointEnum.values())
            .collect(Collectors.groupingBy(kp -> kp.getCategory().name(), Collectors.counting()));
    stats.setCategoryCount(categoryCount);

    Map<Integer, Long> difficultyCount =
        Arrays.stream(KnowledgePointEnum.values())
            .collect(
                Collectors.groupingBy(KnowledgePointEnum::getDifficulty, Collectors.counting()));
    stats.setDifficultyCount(difficultyCount);

    return stats;
  }

  /** 递归收集前置知识点 */
  private void collectPrerequisites(KnowledgePointEnum kp, Set<KnowledgePointEnum> result) {
    if (kp.hasParent()) {
      KnowledgePointEnum parent = kp.getParent();
      if (parent != null) {
        result.add(parent);
        collectPrerequisites(parent, result);
      }
    }
  }

  /** 转换为路径节点 */
  private PathNode convertToPathNode(KnowledgePointEnum kp) {
    PathNode node = new PathNode();
    node.setCode(kp.getCode());
    node.setName(kp.getName());
    node.setDifficulty(kp.getDifficulty());
    node.setCategory(kp.getCategory().getDescription());

    if (kp.hasParent()) {
      KnowledgePointEnum parent = kp.getParent();
      if (parent != null) {
        node.setPrerequisites(Collections.singletonList(parent.getCode()));
      }
    }

    int recommendCount = kp.getDifficulty() * 3 + 2;
    node.setRecommendedQuestions(recommendCount);

    return node;
  }

  /** 学习路径节点 */
  @Data
  public static class PathNode {
    /** 知识点代码 */
    private String code;

    /** 知识点名称 */
    private String name;

    /** 难度等级 */
    private Integer difficulty;

    /** 所属分类 */
    private String category;

    /** 前置知识点列表 */
    private List<String> prerequisites;

    /** 推荐练习题目数量 */
    private Integer recommendedQuestions;

    /** 学习状态（用户相关） */
    private LearningStatus status = LearningStatus.NOT_STARTED;

    /** 掌握程度（0-100） */
    private Integer masteryLevel = 0;
  }

  /** 知识点统计信息 */
  @Data
  public static class KnowledgeStats {
    /** 总知识点数 */
    private Integer totalCount;

    /** 各分类知识点数量 */
    private Map<String, Long> categoryCount;

    /** 各难度知识点数量 */
    private Map<Integer, Long> difficultyCount;
  }

  /** 学习状态 */
  public enum LearningStatus {
    /** 未开始 */
    NOT_STARTED,
    /** 学习中 */
    IN_PROGRESS,
    /** 已掌握 */
    MASTERED
  }
}
