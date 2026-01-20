/* generated using openapi-typescript-codegen -- do no edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */

import type { UserVO } from './UserVO';

export type CommentVO = {
    content?: string;
    createTime?: string;
    id?: number;
    likeCount?: number;
    objectId?: number;
    objectType?: string;
    parentId?: number;
    replyUserId?: number;
    replyUserVO?: UserVO;
    rootId?: number;
    updateTime?: string;
    userId?: number;
    userVO?: UserVO;
};
