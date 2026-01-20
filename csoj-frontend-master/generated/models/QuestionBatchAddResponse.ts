/* generated using openapi-typescript-codegen -- do no edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */

import type { QuestionImportResult } from './QuestionImportResult';

export type QuestionBatchAddResponse = {
    failCount?: number;
    failList?: Array<QuestionImportResult>;
    successCount?: number;
    successList?: Array<QuestionImportResult>;
    totalCount?: number;
};
