/* generated using openapi-typescript-codegen -- do no edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { BaseResponse_Array_string_ } from '../models/BaseResponse_Array_string_';
import type { BaseResponse_boolean_ } from '../models/BaseResponse_boolean_';
import type { BaseResponse_UserPermissionVO_ } from '../models/BaseResponse_UserPermissionVO_';

import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';

export class UserPermissionControllerService {

    /**
     * checkPermission
     * @param permission permission
     * @returns BaseResponse_boolean_ OK
     * @throws ApiError
     */
    public static checkPermissionUsingGet(
permission?: string,
): CancelablePromise<BaseResponse_boolean_> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/api/user/permission/check',
            query: {
                'permission': permission,
            },
            errors: {
                401: `Unauthorized`,
                403: `Forbidden`,
                404: `Not Found`,
            },
        });
    }

    /**
     * getCurrentUserPermission
     * @returns BaseResponse_UserPermissionVO_ OK
     * @throws ApiError
     */
    public static getCurrentUserPermissionUsingGet(): CancelablePromise<BaseResponse_UserPermissionVO_> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/api/user/permission/current',
            errors: {
                401: `Unauthorized`,
                403: `Forbidden`,
                404: `Not Found`,
            },
        });
    }

    /**
     * getAllPermissions
     * @returns BaseResponse_Array_string_ OK
     * @throws ApiError
     */
    public static getAllPermissionsUsingGet(): CancelablePromise<BaseResponse_Array_string_> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/api/user/permission/list',
            errors: {
                401: `Unauthorized`,
                403: `Forbidden`,
                404: `Not Found`,
            },
        });
    }

}
