/* generated using openapi-typescript-codegen -- do no edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';

export class TeamControllerService {

    /**
     * 创建团队
     */
    public static createTeamUsingPost(
        requestBody?: any,
    ): CancelablePromise<any> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/api/team/create',
            body: requestBody,
            mediaType: 'application/json',
        });
    }

    /**
     * 更新团队
     */
    public static updateTeamUsingPut(
        requestBody?: any,
    ): CancelablePromise<any> {
        return __request(OpenAPI, {
            method: 'PUT',
            url: '/api/team/update',
            body: requestBody,
            mediaType: 'application/json',
        });
    }

    /**
     * 获取团队详情
     */
    public static getTeamUsingGet(
        teamId: number,
    ): CancelablePromise<any> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/api/team/{teamId}',
            path: {
                'teamId': teamId,
            },
        });
    }

    /**
     * 分页查询团队列表
     */
    public static listTeamsUsingGet(
        query?: any,
    ): CancelablePromise<any> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/api/team/list',
            query: query,
        });
    }

    /**
     * 加入团队
     */
    public static joinTeamUsingPost(
        requestBody?: any,
    ): CancelablePromise<any> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/api/team/join',
            body: requestBody,
            mediaType: 'application/json',
        });
    }

    /**
     * 退出团队
     */
    public static quitTeamUsingPost(
        teamId: number,
    ): CancelablePromise<any> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/api/team/{teamId}/quit',
            path: {
                'teamId': teamId,
            },
        });
    }

    /**
     * 移除成员
     */
    public static kickMemberUsingPost(
        teamId: number,
        userId: number,
    ): CancelablePromise<any> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/api/team/{teamId}/kick',
            path: {
                'teamId': teamId,
            },
            query: {
                'userId': userId,
            },
        });
    }

    /**
     * 转让队长
     */
    public static transferLeaderUsingPost(
        teamId: number,
        newLeaderId: number,
    ): CancelablePromise<any> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/api/team/{teamId}/transfer',
            path: {
                'teamId': teamId,
            },
            query: {
                'newLeaderId': newLeaderId,
            },
        });
    }

    /**
     * 生成邀请码
     */
    public static generateInviteCodeUsingPost(
        teamId: number,
    ): CancelablePromise<any> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/api/team/{teamId}/invite',
            path: {
                'teamId': teamId,
            },
        });
    }

    /**
     * 获取我的团队
     */
    public static getMyTeamsUsingGet(): CancelablePromise<any> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/api/team/my',
        });
    }

    /**
     * 团队排行榜
     */
    public static getTeamRankingUsingGet(
        query?: any,
    ): CancelablePromise<any> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/api/team/ranking',
            query: query,
        });
    }

}
