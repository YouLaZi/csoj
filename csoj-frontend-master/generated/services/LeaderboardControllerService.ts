/* generated using openapi-typescript-codegen -- do no edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { BaseResponse_List_UserPointsRankVO_ } from '../models/BaseResponse_List_UserPointsRankVO_';

import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';

export class LeaderboardControllerService {

    /**
     * getPointsRanking
     * @param timeRange timeRange
     * @param count count
     * @returns BaseResponse_List_UserPointsRankVO_ OK
     * @throws ApiError
     */
    public static getPointsRankingUsingGet(
timeRange?: string,
count?: number,
): CancelablePromise<BaseResponse_List_UserPointsRankVO_> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/api/leaderboard/points',
            query: {
                'timeRange': timeRange,
                'count': count,
            },
            errors: {
                401: `Unauthorized`,
                403: `Forbidden`,
                404: `Not Found`,
            },
        });
    }

}
