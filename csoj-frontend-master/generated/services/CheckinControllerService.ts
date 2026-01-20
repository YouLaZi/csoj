/* generated using openapi-typescript-codegen -- do no edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { BaseResponse_boolean_ } from '../models/BaseResponse_boolean_';
import type { BaseResponse_List_date_time_ } from '../models/BaseResponse_List_date_time_';
import type { BaseResponse_Map_string_object_ } from '../models/BaseResponse_Map_string_object_';
import type { BaseResponse_Page_UserCheckin_ } from '../models/BaseResponse_Page_UserCheckin_';

import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';

export class CheckinControllerService {

    /**
     * checkinUsingPost
     * @returns BaseResponse_Map_string_object_ OK
     * @returns any Created
     * @throws ApiError
     */
    public static checkinUsingPostUsingPost(): CancelablePromise<BaseResponse_Map_string_object_ | any> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/api/checkin',
            errors: {
                401: `Unauthorized`,
                403: `Forbidden`,
                404: `Not Found`,
            },
        });
    }

    /**
     * doCheckin
     * @returns BaseResponse_Map_string_object_ OK
     * @returns any Created
     * @throws ApiError
     */
    public static doCheckinUsingPost(): CancelablePromise<BaseResponse_Map_string_object_ | any> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/api/checkin/do',
            errors: {
                401: `Unauthorized`,
                403: `Forbidden`,
                404: `Not Found`,
            },
        });
    }

    /**
     * getMonthlyCheckins
     * @returns BaseResponse_List_date_time_ OK
     * @throws ApiError
     */
    public static getMonthlyCheckinsUsingGet(): CancelablePromise<BaseResponse_List_date_time_> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/api/checkin/monthly',
            errors: {
                401: `Unauthorized`,
                403: `Forbidden`,
                404: `Not Found`,
            },
        });
    }

    /**
     * getCheckinRecords
     * @param page page
     * @param pageSize pageSize
     * @returns BaseResponse_Page_UserCheckin_ OK
     * @throws ApiError
     */
    public static getCheckinRecordsUsingGet(
page?: number,
pageSize?: number,
): CancelablePromise<BaseResponse_Page_UserCheckin_> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/api/checkin/records',
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

    /**
     * checkCheckinStatus
     * @returns BaseResponse_boolean_ OK
     * @throws ApiError
     */
    public static checkCheckinStatusUsingGet(): CancelablePromise<BaseResponse_boolean_> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/api/checkin/status',
            errors: {
                401: `Unauthorized`,
                403: `Forbidden`,
                404: `Not Found`,
            },
        });
    }

    /**
     * getUserCheckinRecordsUsingGet
     * @param page page
     * @param pageSize pageSize
     * @returns BaseResponse_Page_UserCheckin_ OK
     * @throws ApiError
     */
    public static getUserCheckinRecordsUsingGetUsingGet(
page?: number,
pageSize?: number,
): CancelablePromise<BaseResponse_Page_UserCheckin_> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/api/checkin/user/records',
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
