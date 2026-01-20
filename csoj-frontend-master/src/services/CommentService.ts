import { CommentControllerService } from "../../generated/services/CommentControllerService";
import { BaseResponse_long_ } from "../../generated/models/BaseResponse_long_";
import { BaseResponse_boolean_ } from "../../generated/models/BaseResponse_boolean_";
import { CancelablePromise } from "../../generated/core/CancelablePromise";
import { OpenAPI } from "../../generated/core/OpenAPI";
import { request } from "../../generated/core/request";

/**
 * 评论相关服务扩展
 */
export class CommentService {
  /**
   * 根据对象ID和类型获取评论列表
   * @param objectId 对象ID
   * @param objectType 对象类型 (post/question)
   * @param current 当前页码
   * @param pageSize 每页大小
   * @returns 评论列表
   */
  public static listCommentsByObjectIdUsingGet(params: {
    objectId: number;
    objectType: string;
    current?: number;
    pageSize?: number;
  }): CancelablePromise<{
    code?: number;
    data?: {
      records?: Array<{
        id?: number;
        content?: string;
        userId?: number;
        userName?: string;
        createTime?: string;
        likeCount?: number;
        hasLike?: boolean;
        parentId?: number;
        rootId?: number;
        replyUserId?: number;
        replyUserName?: string;
        children?: Array<{
          id?: number;
          content?: string;
          userId?: number;
          userName?: string;
          createTime?: string;
          likeCount?: number;
          hasLike?: boolean;
          parentId?: number;
          rootId?: number;
          replyUserId?: number;
          replyUserName?: string;
        }>;
      }>;
      total?: number;
      size?: number;
      current?: number;
    };
    message?: string;
  }> {
    return request(OpenAPI, {
      method: "GET",
      url: "/api/comment/list",
      query: {
        objectId: params.objectId,
        objectType: params.objectType,
        current: params.current,
        pageSize: params.pageSize,
      },
    });
  }

  /**
   * 添加评论
   * @param requestBody 评论请求
   * @returns BaseResponse_long_ 成功返回评论ID
   */
  public static addCommentUsingPost(requestBody: {
    objectId: number;
    objectType: string;
    content: string;
    parentId?: number;
    rootId?: number;
    replyUserId?: number;
  }): CancelablePromise<BaseResponse_long_> {
    return request(OpenAPI, {
      method: "POST",
      url: "/api/comment/add",
      body: requestBody,
    });
  }
}

/**
 * 评论点赞相关服务
 */
export class CommentLikeService {
  /**
   * 点赞/取消点赞评论
   * @param commentId 评论ID
   * @returns BaseResponse_boolean_ 是否成功
   */
  public static likeCommentUsingPost(
    commentId: number
  ): CancelablePromise<BaseResponse_boolean_> {
    return request(OpenAPI, {
      method: "POST",
      url: "/api/comment/like",
      query: {
        commentId: commentId,
      },
    });
  }

  /**
   * 获取用户是否点赞了评论
   * @param commentId 评论ID
   * @returns BaseResponse_boolean_ 是否点赞
   */
  public static hasLikedCommentUsingGet(
    commentId: number
  ): CancelablePromise<BaseResponse_boolean_> {
    return request(OpenAPI, {
      method: "GET",
      url: "/api/comment/like/check",
      query: {
        commentId: commentId,
      },
    });
  }
}
