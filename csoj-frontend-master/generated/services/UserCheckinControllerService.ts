/* generated using openapi-typescript-codegen -- do no edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { BaseResponse_Map_string_object_ } from '../models/BaseResponse_Map_string_object_';
import type { BaseResponse_Page_UserCheckin_ } from '../models/BaseResponse_Page_UserCheckin_';

import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';

export class UserCheckinControllerService {

    /**
     * userCheckin
     * @returns BaseResponse_Map_string_object_ OK
     * @returns any Created
     * @throws ApiError
     */
    public static userCheckinUsingPost1(): CancelablePromise<BaseResponse_Map_string_object_ | any> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/api/user/checkin',
            errors: {
                401: `Unauthorized`,
                403: `Forbidden`,
                404: `Not Found`,
            },
        });
    }

    /**
     * getUserCheckinRecords
     * @param page page
     * @param pageSize pageSize
     * @returns BaseResponse_Page_UserCheckin_ OK
     * @throws ApiError
     */
    public static getUserCheckinRecordsUsingGet(
page?: number,
pageSize?: number,
): CancelablePromise<BaseResponse_Page_UserCheckin_> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/api/user/checkin/records',
            query: {
                'page': page,
                'pageSize': pageSize,
            },
            errors: {
                401: `Unauthorized`,
                403: `Forbidden`,
                404: `Not Found`,
            },
        });
    }

}
