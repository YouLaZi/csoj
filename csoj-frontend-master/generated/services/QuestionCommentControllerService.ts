/* generated using openapi-typescript-codegen -- do no edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { BaseResponse_boolean_ } from '../models/BaseResponse_boolean_';
import type { BaseResponse_List_CommentVO_ } from '../models/BaseResponse_List_CommentVO_';
import type { BaseResponse_long_ } from '../models/BaseResponse_long_';
import type { BaseResponse_Page_CommentVO_ } from '../models/BaseResponse_Page_CommentVO_';
import type { CommentAddRequest } from '../models/CommentAddRequest';
import type { CommentQueryRequest } from '../models/CommentQueryRequest';
import type { DeleteRequest } from '../models/DeleteRequest';

import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';

export class QuestionCommentControllerService {

    /**
     * addComment
     * @param requestBody 
     * @returns BaseResponse_long_ OK
     * @returns any Created
     * @throws ApiError
     */
    public static addCommentUsingPost1(
requestBody?: CommentAddRequest,
): CancelablePromise<BaseResponse_long_ | any> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/api/question/comment/add',
            body: requestBody,
            mediaType: 'application/json',
            errors: {
                401: `Unauthorized`,
                403: `Forbidden`,
                404: `Not Found`,
            },
        });
    }

    /**
     * deleteComment
     * @param requestBody 
     * @returns BaseResponse_boolean_ OK
     * @returns any Created
     * @throws ApiError
     */
    public static deleteCommentUsingPost(
requestBody?: DeleteRequest,
): CancelablePromise<BaseResponse_boolean_ | any> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/api/question/comment/delete',
            body: requestBody,
            mediaType: 'application/json',
            errors: {
                401: `Unauthorized`,
                403: `Forbidden`,
                404: `Not Found`,
            },
        });
    }

    /**
     * listCommentByPage
     * @param requestBody 
     * @returns BaseResponse_Page_CommentVO_ OK
     * @returns any Created
     * @throws ApiError
     */
    public static listCommentByPageUsingPost(
requestBody?: CommentQueryRequest,
): CancelablePromise<BaseResponse_Page_CommentVO_ | any> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/api/question/comment/list/page',
            body: requestBody,
            mediaType: 'application/json',
            errors: {
                401: `Unauthorized`,
                403: `Forbidden`,
                404: `Not Found`,
            },
        });
    }

    /**
     * getCommentsByQuestionId
     * @param questionId questionId
     * @returns BaseResponse_List_CommentVO_ OK
     * @throws ApiError
     */
    public static getCommentsByQuestionIdUsingGet(
questionId: number,
): CancelablePromise<BaseResponse_List_CommentVO_> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/api/question/comment/list/question/{questionId}',
            path: {
                'questionId': questionId,
            },
            errors: {
                401: `Unauthorized`,
                403: `Forbidden`,
                404: `Not Found`,
            },
        });
    }

}
