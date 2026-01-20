package com.oj.cs.model.enums;

import java.util.*;

import lombok.Getter;

/** 算法知识点枚举 用于题目标签和学习路径规划 */
@Getter
public enum KnowledgePointEnum {

  // ===== 基础数据结构 =====
  ARRAY("array", "数组", 1, Category.DATA_STRUCTURE, null),
  STRING("string", "字符串", 1, Category.DATA_STRUCTURE, null),
  LINKED_LIST("linkedlist", "链表", 2, Category.DATA_STRUCTURE, null),
  STACK("stack", "栈", 2, Category.DATA_STRUCTURE, null),
  QUEUE("queue", "队列", 2, Category.DATA_STRUCTURE, null),
  HASH_TABLE("hashtable", "哈希表", 2, Category.DATA_STRUCTURE, null),
  TREE("tree", "树", 3, Category.DATA_STRUCTURE, null),
  BINARY_TREE("binarytree", "二叉树", 3, Category.DATA_STRUCTURE, "tree"),
  BST("bst", "二叉搜索树", 4, Category.DATA_STRUCTURE, "binarytree"),
  HEAP("heap", "堆", 3, Category.DATA_STRUCTURE, "tree"),
  GRAPH("graph", "图", 4, Category.DATA_STRUCTURE, null),

  // ===== 基础算法 =====
  SORT("sort", "排序", 1, Category.ALGORITHM, null),
  BINARY_SEARCH("binarysearch", "二分查找", 2, Category.ALGORITHM, null),
  TWO_POINTERS("twopointers", "双指针", 2, Category.ALGORITHM, null),
  SLIDING_WINDOW("slidingwindow", "滑动窗口", 3, Category.ALGORITHM, "twopointers"),
  RECURSION("recursion", "递归", 2, Category.ALGORITHM, null),
  BACKTRACKING("backtracking", "回溯", 3, Category.ALGORITHM, "recursion"),
  DFS("dfs", "深度优先搜索", 3, Category.ALGORITHM, "graph"),
  BFS("bfs", "广度优先搜索", 3, Category.ALGORITHM, "graph"),

  // ===== 动态规划 =====
  DP("dp", "动态规划", 4, Category.DP, null),
  DP_ONE_DIM("dp1d", "一维DP", 3, Category.DP, "dp"),
  DP_TWO_DIM("dp2d", "二维DP", 4, Category.DP, "dp"),
  KNAPSACK("knapsack", "背包问题", 4, Category.DP, "dp"),
  LIS("lis", "最长递增子序列", 4, Category.DP, "dp"),
  LCS("lcs", "最长公共子序列", 4, Category.DP, "dp"),

  // ===== 数学 =====
  MATH("math", "数学", 2, Category.MATH, null),
  GCD("gcd", "最大公约数", 2, Category.MATH, "math"),
  PRIME("prime", "质数", 2, Category.MATH, "math"),
  COMBINATION("combination", "组合数学", 3, Category.MATH, "math"),

  // ===== 字符串算法 =====
  STRING_MATCH("stringmatch", "字符串匹配", 3, Category.STRING, "string"),
  KMP("kmp", "KMP算法", 4, Category.STRING, "stringmatch"),

  // ===== 设计题 =====
  DESIGN("design", "设计题", 3, Category.DESIGN, null),
  LRU("lru", "LRU缓存", 3, Category.DESIGN, "design");

  /** 知识点标识（英文） */
  private final String code;

  /** 知识点名称（中文） */
  private final String name;

  /** 难度等级（1-5） */
  private final int difficulty;

  /** 所属分类 */
  private final Category category;

  /** 父知识点（前置知识） */
  private final String parentCode;

  /** 知识点分类 */
  @Getter
  public enum Category {
    /** 数据结构 */
    DATA_STRUCTURE("数据结构"),
    /** 算法 */
    ALGORITHM("算法"),
    /** 动态规划 */
    DP("动态规划"),
    /** 数学 */
    MATH("数学"),
    /** 字符串 */
    STRING("字符串"),
    /** 设计题 */
    DESIGN("设计题");

    private final String description;

    Category(String description) {
      this.description = description;
    }
  }

  KnowledgePointEnum(
      String code, String name, int difficulty, Category category, String parentCode) {
    this.code = code;
    this.name = name;
    this.difficulty = difficulty;
    this.category = category;
    this.parentCode = parentCode;
  }

  /** 根据code获取知识点 */
  public static KnowledgePointEnum fromCode(String code) {
    if (code == null || code.isEmpty()) {
      return null;
    }
    for (KnowledgePointEnum kp : values()) {
      if (kp.code.equalsIgnoreCase(code)) {
        return kp;
      }
    }
    return null;
  }

  /** 获取所有知识点列表（按难度排序） */
  public static List<KnowledgePointEnum> getAllSorted() {
    List<KnowledgePointEnum> list = Arrays.asList(values());
    list.sort(
        Comparator.comparingInt(KnowledgePointEnum::getDifficulty)
            .thenComparing(KnowledgePointEnum::getCategory));
    return list;
  }

  /** 根据分类获取知识点 */
  public static List<KnowledgePointEnum> getByCategory(Category category) {
    List<KnowledgePointEnum> result = new ArrayList<>();
    for (KnowledgePointEnum kp : values()) {
      if (kp.category == category) {
        result.add(kp);
      }
    }
    result.sort(Comparator.comparingInt(KnowledgePointEnum::getDifficulty));
    return result;
  }

  /** 获取前置知识点 */
  public KnowledgePointEnum getParent() {
    if (parentCode == null) {
      return null;
    }
    return fromCode(parentCode);
  }

  /** 检查是否有前置知识点 */
  public boolean hasParent() {
    return parentCode != null;
  }
}
