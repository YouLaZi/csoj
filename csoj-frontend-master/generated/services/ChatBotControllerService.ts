/* generated using openapi-typescript-codegen -- do no edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { BaseResponse_ChatMessageResponse_ } from '../models/BaseResponse_ChatMessageResponse_';
import type { BaseResponse_List_ChatMessage_ } from '../models/BaseResponse_List_ChatMessage_';
import type { BaseResponse_List_Map_string_object_ } from '../models/BaseResponse_List_Map_string_object_';
import type { BaseResponse_Map_string_object_ } from '../models/BaseResponse_Map_string_object_';
import type { ChatMessageRequest } from '../models/ChatMessageRequest';

import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';

export class ChatBotControllerService {

    /**
     * getChatHistory
     * @param questionId questionId
     * @returns BaseResponse_List_ChatMessage_ OK
     * @throws ApiError
     */
    public static getChatHistoryUsingGet(
questionId?: number,
): CancelablePromise<BaseResponse_List_ChatMessage_> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/api/chat/history',
            query: {
                'questionId': questionId,
            },
            errors: {
                401: `Unauthorized`,
                403: `Forbidden`,
                404: `Not Found`,
            },
        });
    }

    /**
     * sendChatMessage
     * @param requestBody 
     * @returns BaseResponse_ChatMessageResponse_ OK
     * @returns any Created
     * @throws ApiError
     */
    public static sendChatMessageUsingPost(
requestBody?: ChatMessageRequest,
): CancelablePromise<BaseResponse_ChatMessageResponse_ | any> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/api/chat/message',
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
     * getRecommendedProblems
     * @returns BaseResponse_List_Map_string_object_ OK
     * @throws ApiError
     */
    public static getRecommendedProblemsUsingGet(): CancelablePromise<BaseResponse_List_Map_string_object_> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/api/problems/recommended',
            errors: {
                401: `Unauthorized`,
                403: `Forbidden`,
                404: `Not Found`,
            },
        });
    }

    /**
     * getLearningProgress
     * @returns BaseResponse_Map_string_object_ OK
     * @throws ApiError
     */
    public static getLearningProgressUsingGet(): CancelablePromise<BaseResponse_Map_string_object_> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/api/user/learning-progress',
            errors: {
                401: `Unauthorized`,
                403: `Forbidden`,
                404: `Not Found`,
            },
        });
    }

}
