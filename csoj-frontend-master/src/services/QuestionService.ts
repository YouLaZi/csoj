import { OpenAPI } from "../../generated/core/OpenAPI";
import { request } from "../../generated/core/request";
import { QuestionControllerService } from "../../generated/services/QuestionControllerService";
import { Message } from "@arco-design/web-vue";

/**
 * 问题服务类
 */
export class QuestionService {
  /**
   * 获取热门题目列表
   * @param limit 限制数量
   * @returns 热门题目列表
   */
  public static async getHotQuestions(limit = 5) {
    try {
      // 使用问题查询接口，按照热度排序获取热门题目
      const res = await QuestionControllerService.listQuestionVoByPageUsingPost(
        {
          pageSize: limit,
          current: 1,
          sortField: "thumbNum", // 按点赞数排序
          sortOrder: "descend", // 降序排列
        }
      );
      return res;
    } catch (error) {
      console.error("获取热门题目失败", error);
      Message.error("获取热门题目失败");
      return {
        code: -1,
        message: "获取热门题目失败",
        data: null,
      };
    }
  }

  /**
   * 获取题目详情
   * @param id 题目ID
   * @returns 题目详情
   */
  public static async getQuestionDetail(id: number) {
    try {
      const res = await QuestionControllerService.getQuestionVoByIdUsingGet(id);
      return res;
    } catch (error) {
      console.error("获取题目详情失败", error);
      Message.error("获取题目详情失败");
      return {
        code: -1,
        message: "获取题目详情失败",
        data: null,
      };
    }
  }
}

export default QuestionService;
