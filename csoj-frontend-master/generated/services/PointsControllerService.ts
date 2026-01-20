/* generated using openapi-typescript-codegen -- do no edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { BaseResponse_boolean_ } from '../models/BaseResponse_boolean_';
import type { BaseResponse_List_date_time_ } from '../models/BaseResponse_List_date_time_';
import type { BaseResponse_List_PointsRecord_ } from '../models/BaseResponse_List_PointsRecord_';
import type { BaseResponse_Page_PointsRecord_ } from '../models/BaseResponse_Page_PointsRecord_';
import type { BaseResponse_UserPointsVO_ } from '../models/BaseResponse_UserPointsVO_';

import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';

export class PointsControllerService {

    /**
     * userCheckin
     * @returns BaseResponse_boolean_ OK
     * @returns any Created
     * @throws ApiError
     */
    public static userCheckinUsingPost(): CancelablePromise<BaseResponse_boolean_ | any> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/api/points/checkin',
            errors: {
                401: `Unauthorized`,
                403: `Forbidden`,
                404: `Not Found`,
            },
        });
    }

    /**
     * getUserMonthlyCheckins
     * @returns BaseResponse_List_date_time_ OK
     * @throws ApiError
     */
    public static getUserMonthlyCheckinsUsingGet(): CancelablePromise<BaseResponse_List_date_time_> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/api/points/checkin/monthly',
            errors: {
                401: `Unauthorized`,
                403: `Forbidden`,
                404: `Not Found`,
            },
        });
    }

    /**
     * checkUserCheckinStatus
     * @returns BaseResponse_boolean_ OK
     * @throws ApiError
     */
    public static checkUserCheckinStatusUsingGet(): CancelablePromise<BaseResponse_boolean_> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/api/points/checkin/status',
            errors: {
                401: `Unauthorized`,
                403: `Forbidden`,
                404: `Not Found`,
            },
        });
    }

    /**
     * getUserPointsInfo
     * @returns BaseResponse_UserPointsVO_ OK
     * @throws ApiError
     */
    public static getUserPointsInfoUsingGet(): CancelablePromise<BaseResponse_UserPointsVO_> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/api/points/info',
            errors: {
                401: `Unauthorized`,
                403: `Forbidden`,
                404: `Not Found`,
            },
        });
    }

    /**
     * getUserPointsRecords
     * @returns BaseResponse_List_PointsRecord_ OK
     * @throws ApiError
     */
    public static getUserPointsRecordsUsingGet(): CancelablePromise<BaseResponse_List_PointsRecord_> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/api/points/records',
            errors: {
                401: `Unauthorized`,
                403: `Forbidden`,
                404: `Not Found`,
            },
        });
    }

    /**
     * getPointsRecordsByPage
     * @param current 
     * @param endTime 
     * @param pageSize 
     * @param sortField 
     * @param sortOrder 
     * @param startTime 
     * @param timeRange 
     * @param type 
     * @param userId 
     * @returns BaseResponse_Page_PointsRecord_ OK
     * @throws ApiError
     */
    public static getPointsRecordsByPageUsingGet(
current?: number,
endTime?: string,
pageSize?: number,
sortField?: string,
sortOrder?: string,
startTime?: string,
timeRange?: string,
type?: string,
userId?: number,
): CancelablePromise<BaseResponse_Page_PointsRecord_> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/api/points/records/page',
            query: {
                'current': current,
                'endTime': endTime,
                'pageSize': pageSize,
                'sortField': sortField,
                'sortOrder': sortOrder,
                'startTime': startTime,
                'timeRange': timeRange,
                'type': type,
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
