/* generated using openapi-typescript-codegen -- do no edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { Announcement } from '../models/Announcement';
import type { BaseResponse_boolean_ } from '../models/BaseResponse_boolean_';
import type { BaseResponse_List_Announcement_ } from '../models/BaseResponse_List_Announcement_';
import type { BaseResponse_long_ } from '../models/BaseResponse_long_';
import type { BaseResponse_Page_Announcement_ } from '../models/BaseResponse_Page_Announcement_';

import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';

export class AnnouncementControllerService {

    /**
     * addAnnouncement
     * @param requestBody 
     * @returns BaseResponse_long_ OK
     * @returns any Created
     * @throws ApiError
     */
    public static addAnnouncementUsingPost(
requestBody?: Announcement,
): CancelablePromise<BaseResponse_long_ | any> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/api/announcement/add',
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
     * deleteAnnouncement
     * @param id id
     * @returns BaseResponse_boolean_ OK
     * @returns any Created
     * @throws ApiError
     */
    public static deleteAnnouncementUsingPost(
id: number,
): CancelablePromise<BaseResponse_boolean_ | any> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/api/announcement/delete',
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
     * getLatestAnnouncements
     * @param count count
     * @returns BaseResponse_List_Announcement_ OK
     * @throws ApiError
     */
    public static getLatestAnnouncementsUsingGet(
count?: number,
): CancelablePromise<BaseResponse_List_Announcement_> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/api/announcement/latest',
            query: {
                'count': count,
            },
            errors: {
                401: `Unauthorized`,
                403: `Forbidden`,
                404: `Not Found`,
            },
        });
    }

    /**
     * getAnnouncementList
     * @param page page
     * @param pageSize pageSize
     * @returns BaseResponse_Page_Announcement_ OK
     * @throws ApiError
     */
    public static getAnnouncementListUsingGet(
page?: number,
pageSize?: number,
): CancelablePromise<BaseResponse_Page_Announcement_> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/api/announcement/list',
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
     * updateAnnouncement
     * @param requestBody 
     * @returns BaseResponse_boolean_ OK
     * @returns any Created
     * @throws ApiError
     */
    public static updateAnnouncementUsingPost(
requestBody?: Announcement,
): CancelablePromise<BaseResponse_boolean_ | any> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/api/announcement/update',
            body: requestBody,
            mediaType: 'application/json',
            errors: {
                401: `Unauthorized`,
                403: `Forbidden`,
                404: `Not Found`,
            },
        });
    }

}
