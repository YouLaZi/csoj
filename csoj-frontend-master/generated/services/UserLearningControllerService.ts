/* generated using openapi-typescript-codegen -- do no edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { BaseResponse_List_Question_ } from '../models/BaseResponse_List_Question_';

import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';

export class UserLearningControllerService {

    /**
     * getCompletedProblems
     * @returns BaseResponse_List_Question_ OK
     * @throws ApiError
     */
    public static getCompletedProblemsUsingGet(): CancelablePromise<BaseResponse_List_Question_> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/api/learning/completed-problems',
            errors: {
                401: `Unauthorized`,
                403: `Forbidden`,
                404: `Not Found`,
            },
        });
    }

}
