/* generated using openapi-typescript-codegen -- do no edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { BaseResponse_boolean_ } from '../models/BaseResponse_boolean_';
import type { BaseResponse_long_ } from '../models/BaseResponse_long_';
import type { BaseResponse_Page_Comment_ } from '../models/BaseResponse_Page_Comment_';
import type { CommentAddRequest } from '../models/CommentAddRequest';

import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';

export class CommentControllerService {

    /**
     * addComment
     * @param requestBody 
     * @returns BaseResponse_long_ OK
     * @returns any Created
     * @throws ApiError
     */
    public static addCommentUsingPost(
requestBody?: CommentAddRequest,
): CancelablePromise<BaseResponse_long_ | any> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/api/comment/add',
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
     * likeComment
     * @param id id
     * @returns BaseResponse_boolean_ OK
     * @returns any Created
     * @throws ApiError
     */
    public static likeCommentUsingPost(
id: number,
): CancelablePromise<BaseResponse_boolean_ | any> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/api/comment/like',
            query: {
                'id': id,
            },
            errors: {
                401: `Unauthorized`,
                403: `Forbidden`,
                404: `Not Found`,
            },
        });
    }

    /**
     * listComments
     * @param content 
     * @param current 
     * @param objectId 
     * @param objectType 
     * @param pageSize 
     * @param parentId 
     * @param rootId 
     * @param sortField 
     * @param sortOrder 
     * @param userId 
     * @returns BaseResponse_Page_Comment_ OK
     * @throws ApiError
     */
    public static listCommentsUsingGet(
content?: string,
current?: number,
objectId?: number,
objectType?: string,
pageSize?: number,
parentId?: number,
rootId?: number,
sortField?: string,
sortOrder?: string,
userId?: number,
): CancelablePromise<BaseResponse_Page_Comment_> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/api/comment/list',
            query: {
                'content': content,
                'current': current,
                'objectId': objectId,
                'objectType': objectType,
                'pageSize': pageSize,
                'parentId': parentId,
                'rootId': rootId,
                'sortField': sortField,
                'sortOrder': sortOrder,
                'userId': userId,
            },
            errors: {
                401: `Unauthorized`,
                403: `Forbidden`,
                404: `Not Found`,
            },
        });
    }

}
